package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJClassNameInsertHandler
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJFunctionNameInsertHandler
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJVariableInsertHandler
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.PRIMITIVE_VAR_NAMES
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.NoIndex
import cappuccino.ide.intellij.plugin.references.ObjJIgnoreEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.ArrayUtils

import java.util.ArrayList
import java.util.Arrays
import java.util.logging.Logger

import cappuccino.ide.intellij.plugin.utils.ArrayUtils.EMPTY_STRING_ARRAY
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import com.intellij.codeInsight.lookup.LookupElement

class BlanketCompletionProvider : CompletionProvider<CompletionParameters>() {

    private val CARET_INDICATOR = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED

    override fun addCompletions(
            parameters: CompletionParameters, context: ProcessingContext,
            resultSet: CompletionResultSet) {
        val element = parameters.position
        val parent = element.parent
        val results: MutableList<String>
        val queryString = element.text.substring(0, element.text.indexOf(CARET_INDICATOR))

        if (element.getElementType() in ObjJTokenSets.COMMENTS) {
            val text = element.text
            val commentText = text.substringBefore(CARET_INDICATOR, "")
            val commentTokenParts:List<String> = commentText.split("\\s+".toRegex()).map{ it.trim() }.filterNot { it.isEmpty() || it.contains("*") || it == "//" }
            var afterVar = false
            var indexAfter = -1
            var currentIndex = 0
            if (text.contains("@var")) {
                loop@ for (part in commentTokenParts) {
                    currentIndex++
                    if (part == "@var") {
                        afterVar = true
                        indexAfter = currentIndex
                        continue
                    }
                    if (!afterVar) {
                        continue
                    }
                    val place = commentTokenParts.size - indexAfter
                    when (place) {
                        0,1 -> getClassNameCompletions(resultSet, element)
                        2 -> {
                            val variableNames = ObjJVariableNameUtil.getSiblingVariableAssignmentNameElements(element, 0).map {
                                it.text
                            }
                            addCompletionElementsSimple(resultSet, variableNames)
                        }
                        else -> break@loop
                    }
                }
            } else if (text.contains("@ignore")) {
                var precededByComma = false
                loop@ for (part in commentTokenParts) {
                    currentIndex++
                    if (part == "@ignore") {
                        afterVar = true
                        indexAfter = currentIndex
                        continue
                    }
                    if (!afterVar) {
                        continue
                    }
                    precededByComma = part.trim() == ","
                    if (precededByComma) {
                        continue
                    }
                    
                    val place = commentTokenParts.size - indexAfter
                    when {
                        place <= 1 || precededByComma -> addCompletionElementsSimple(resultSet, ObjJSuppressInspectionFlags.values().map { it.flag })
                        else -> break@loop
                    }
                }
            } else {
                addCompletionElementsSimple(resultSet, listOf(
                        ObjJIgnoreEvaluatorUtil.DO_NOT_RESOLVE
                ))
                resultSet.addElement(LookupElementBuilder.create("ignore").withPresentableText("@ignore").withInsertHandler {
                    insertionContext: InsertionContext, lookupElement: LookupElement ->
                    insertionContext.document.insertString(insertionContext.selectionEndOffset, " ")
                })
                resultSet.addElement(LookupElementBuilder.create("var").withPresentableText("@var").withInsertHandler {
                    insertionContext: InsertionContext, lookupElement: LookupElement ->
                    insertionContext.document.insertString(insertionContext.selectionEndOffset, " ")
                })
            }
            resultSet.stopHere()
            return
        }

        if (element is ObjJAccessorPropertyType || element.getParentOfType( ObjJAccessorPropertyType::class.java) != null) {
            results = ArrayUtils.search(ACCESSSOR_PROPERTY_TYPES, queryString) as MutableList<String>
        } else if (isMethodCallSelector(element)) {
            ObjJMethodCallCompletionContributor2.addSelectorLookupElementsFromSelectorList(resultSet, element)
            return
        } else if (PsiTreeUtil.getParentOfType(element, ObjJInheritedProtocolList::class.java) != null) {
            results = ObjJProtocolDeclarationsIndex.instance.getKeysByPattern("$queryString(.+)", element.project) as MutableList<String>
        } else {
            if (element.getContainingScope() == ReferencedInScope.FILE) {
                addFileLevelCompletions(resultSet, element)
            }
            if (element.hasParentOfType(ObjJInstanceVariableList::class.java)) {
                resultSet.stopHere()
                return
            }
            if (element.hasParentOfType(ObjJMethodHeaderDeclaration::class.java)) {
                addMethodHeaderVariableNameCompletions(resultSet, element)
                resultSet.stopHere()
                return
            }

            if (ObjJVariablePsiUtil.isNewVarDec(element)) {
                resultSet.stopHere()
                return
            }

            if (element.text.startsWith("@") && PsiTreeUtil.findFirstParent(element) {
                it is ObjJClassDeclarationElement<*>
            } != null) {
                resultSet.addElement(LookupElementBuilder.create("end").withPresentableText("@end"))
            }

            val variableName = element as? ObjJVariableName ?: parent as? ObjJVariableName
            results = if (variableName != null) {
                ObjJVariableNameCompletionContributorUtil.getVariableNameCompletions(variableName) as MutableList<String>
            } else {
                mutableListOf()
            }

            if ((variableName?.indexInQualifiedReference ?: 0) < 1 && (variableName?.text?.replace(ObjJCompletionContributor.CARET_INDICATOR, "")?.trim()?.length ?: 0) > 0) {
                appendFunctionCompletions(resultSet, element)
                results.addAll(getKeywordCompletions(variableName))
                results.addAll(getInClassKeywords(variableName))
                results.addAll(Arrays.asList("YES", "yes", "NO", "no", "true", "false"))
            } else {
                LOGGER.info("Variable name ${variableName?.text} is index of: "+variableName?.indexInQualifiedReference)
            }
        }
        getClassNameCompletions(resultSet, element)
        addCompletionElementsSimple(resultSet, results)
    }

    private fun getClassNameCompletions(resultSet: CompletionResultSet, element: PsiElement?) {
        if (element == null) {
            return
        }
        if (element.hasParentOfType(ObjJInheritedProtocolList::class.java) || element.hasParentOfType(ObjJFormalVariableType::class.java) || element.getElementType() in ObjJTokenSets.COMMENTS) {
            ObjJProtocolDeclarationsIndex.instance.getAllKeys(element.project).forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler.instance))
            }
        }
        if (element.hasParentOfType(ObjJFormalVariableType::class.java)) {
            PRIMITIVE_VAR_NAMES.forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler.instance))
            }
        }
        if(element.hasParentOfType( ObjJCallTarget::class.java) || element.hasParentOfType(ObjJFormalVariableType::class.java) || element.getElementType() in ObjJTokenSets.COMMENTS) {
            val ignoreUnderscore = ObjJPluginSettings.ignoreUnderscoredClasses
            ObjJImplementationDeclarationsIndex.instance.getAll(element.project).forEach {
                if (ObjJIgnoreEvaluatorUtil.shouldIgnoreUnderscore(element) || ObjJIgnoreEvaluatorUtil.isIgnored(element, ObjJSuppressInspectionFlags.IGNORE_CLASS)) {
                    return@forEach
                }
                if (ObjJIgnoreEvaluatorUtil.isIgnored(it) || ObjJIgnoreEvaluatorUtil.noIndex(it, NoIndex.CLASS) || ObjJIgnoreEvaluatorUtil.noIndex(it, NoIndex.ANY)) {
                    return@forEach
                }
                resultSet.addElement(LookupElementBuilder.create(it.getClassNameString()).withInsertHandler(ObjJClassNameInsertHandler.instance))
            }
            resultSet.addElement(LookupElementBuilder.create("self").withInsertHandler(ObjJClassNameInsertHandler.instance))
            resultSet.addElement(LookupElementBuilder.create("super").withInsertHandler(ObjJClassNameInsertHandler.instance))
        }
    }

    private fun getInClassKeywords(element: PsiElement?): List<String> {
        if (element == null) {
            return EMPTY_STRING_ARRAY
        }
        val queryText = element.text.substring(0, element.text.indexOf(CARET_INDICATOR))
        val out = ArrayList<String>()
        if (element.getParentOfType( ObjJClassDeclarationElement::class.java) != null) {
            if ("self".startsWith(queryText)) {
                out.add("self")
            }
            if ("super".startsWith(queryText)) {
                out.add("super")
            }
        }
        return out
    }

    private fun addFileLevelCompletions(resultSet: CompletionResultSet, element:PsiElement) {
        val prefix: String
        val resultsTemp:List<String>
        when {
            element.isType(ObjJTypes.ObjJ_AT_FRAGMENT) -> {
                resultsTemp = mutableListOf(
                        "import",
                        "typedef",
                        "class",
                        "implementation",
                        "protocol",
                        "end"
                )
                prefix = "@"
            }
            element.isType(ObjJTypes.ObjJ_PP_FRAGMENT) -> {
                resultsTemp = mutableListOf(
                        "if",
                        "include",
                        "pragma",
                        "define",
                        "undef",
                        "ifdef",
                        "ifndef",
                        "include",
                        "error",
                        "warning"
                )
                prefix = "#"
            }
            else -> {
                //LOGGER.log(Level.INFO, "File level completion for token type ${element.getElementType().toString()} failed.")
                resultsTemp = mutableListOf()
                prefix = ""
            }
        }
        resultsTemp.forEach {
            resultSet.addElement(LookupElementBuilder.create(it).withPresentableText(prefix + it).withInsertHandler { context, _ ->
                if (!EditorUtil.isTextAtOffset(context, " ")) {
                    EditorUtil.insertText(context.editor, " ", true)
                }
            })
        }
    }

    private fun getKeywordCompletions(element: PsiElement?): List<String> {
        val expression = element.getParentOfType( ObjJExpr::class.java)
        return if (expression == null || expression.text != element!!.text || expression.parent !is ObjJBlock) {
            EMPTY_STRING_ARRAY
        } else ObjJKeywordsList.search(element.text.substring(0, element.text.indexOf(CARET_INDICATOR)))
    }

    private fun addCompletionElementsSimple(resultSet: CompletionResultSet, completionOptions: List<String>) {
        for (completionOption in completionOptions) {
            ProgressIndicatorProvider.checkCanceled()
            resultSet.addElement(LookupElementBuilder.create(completionOption).withInsertHandler(ObjJVariableInsertHandler.instance))
        }
    }

    private fun appendFunctionCompletions(resultSet: CompletionResultSet, element: PsiElement) {
        val functionNamePattern = element.text.replace(CARET_INDICATOR, "(.*)")
        val functions = ObjJFunctionsIndex.instance.getByPattern(functionNamePattern, element.project)
        val ignoreFunctionPrefixedWithUnderscore = ObjJPluginSettings.ignoreUnderscoredClasses
        for (functionName in functions.keys) {
            val shouldPossiblyIgnore = ignoreFunctionPrefixedWithUnderscore && functionName.startsWith("_")
            for (function in functions.getValue(functionName)) {
                if (shouldPossiblyIgnore && !element.containingFile.isEquivalentTo(function.containingFile))
                    continue
                ProgressIndicatorProvider.checkCanceled()
                val priority = if (PsiTreeUtil.findCommonContext(function, element) != null) ObjJCompletionContributor.FUNCTIONS_IN_FILE_PRIORITY else ObjJCompletionContributor.FUNCTIONS_NOT_IN_FILE_PRIORITY

                val lookupElementBuilder = LookupElementBuilder
                        .create(functionName)
                        .withTailText("(" + ArrayUtils.join(function.paramNames as List<String>, ",") + ") in " + ObjJPsiImplUtil.getFileName(function))
                        .withInsertHandler(ObjJFunctionNameInsertHandler.instance)
                resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, priority))
            }
        }
        addAllLocalFunctionNames(resultSet, element)
    }

    private fun addAllLocalFunctionNames(resultSet: CompletionResultSet, element: PsiElement) {
        val functions = element.getParentBlockChildrenOfType(ObjJFunctionDeclarationElement::class.java, true).toMutableList()
        functions.addAll(element.containingFile.getChildrenOfType(ObjJFunctionDeclarationElement::class.java))
        for (function in functions) {
            val functionName = function.functionNameNode?.text ?: continue
            val lookupElementBuilder = LookupElementBuilder
                    .create(functionName)
                    .withTailText("(" + ArrayUtils.join(function.paramNames, ",") + ") in " + ObjJPsiImplUtil.getFileName(function))
                    .withInsertHandler(ObjJFunctionNameInsertHandler.instance)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, ObjJCompletionContributor.FUNCTIONS_IN_FILE_PRIORITY ))
        }
    }

    private fun addMethodHeaderVariableNameCompletions(resultSet: CompletionResultSet, variableName:PsiElement) {
        val methodHeaderDeclaration:ObjJMethodDeclarationSelector = variableName.getParentOfType(ObjJMethodDeclarationSelector::class.java) ?: return
        val formalVariableType = methodHeaderDeclaration.formalVariableType?.text ?: return
    }

    private fun isMethodCallSelector(element: PsiElement?) : Boolean {
        if (element == null) {
            return false
        }
        if (PsiTreeUtil.getParentOfType(element, ObjJMethodCall::class.java) == null) {
            return false
        }
        if (element is ObjJSelector || element.parent is ObjJSelector) {
            return true
        }
        if (element.parent is ObjJMethodCall) {
            return true
        }
        return false
    }

    companion object {

        private val LOGGER = Logger.getLogger(BlanketCompletionProvider::class.java.name)

        private val ACCESSSOR_PROPERTY_TYPES = Arrays.asList("property", "getter", "setter", "readonly", "copy")
    }
}