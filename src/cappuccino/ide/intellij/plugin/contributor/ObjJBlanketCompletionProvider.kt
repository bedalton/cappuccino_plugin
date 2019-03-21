package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJClassNameInsertHandler
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import cappuccino.ide.intellij.plugin.contributor.utils.ObjJCompletionElementProviderUtil.addCompletionElementsSimple
import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.PRIMITIVE_VAR_NAMES
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.NoIndex
import cappuccino.ide.intellij.plugin.references.ObjJIgnoreEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.utils.ArrayUtils

import java.util.ArrayList
import java.util.Arrays
import java.util.logging.Logger

import cappuccino.ide.intellij.plugin.utils.ArrayUtils.EMPTY_STRING_ARRAY
import cappuccino.ide.intellij.plugin.utils.EditorUtil

object ObjJBlanketCompletionProvider : CompletionProvider<CompletionParameters>() {

    const val CARET_INDICATOR = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED

    @Suppress("unused")
    private val LOGGER by lazy {
        Logger.getLogger(ObjJBlanketCompletionProvider::class.java.name)
    }
    private val ACCESSOR_PROPERTY_TYPES = Arrays.asList("property", "getter", "setter", "readonly", "copy")


    override fun addCompletions(
            parameters: CompletionParameters, context: ProcessingContext,
            resultSet: CompletionResultSet) {
        val element = parameters.position
        val results: MutableList<String>
        val queryString = element.text.substring(0, element.text.indexOf(CARET_INDICATOR))

        when {
            element.getElementType() in ObjJTokenSets.COMMENTS -> {
                ObjJCommentCompletionProvider.addCommentCompletions(resultSet, element)
                resultSet.stopHere()
            }
            element is ObjJAccessorPropertyType || element.getParentOfType( ObjJAccessorPropertyType::class.java) != null -> {
                results = ArrayUtils.search(ACCESSOR_PROPERTY_TYPES, queryString) as MutableList<String>
                addCompletionElementsSimple(resultSet, results)
            }
            isMethodCallSelector(element) -> {
                ObjJMethodCallCompletionContributor.addSelectorLookupElementsFromSelectorList(resultSet, element)
            }
            PsiTreeUtil.getParentOfType(element, ObjJInheritedProtocolList::class.java) != null -> {
                addProtocolNameCompletionElements(resultSet, element, queryString)
            }
            element is ObjJFormalVariableType || element.hasParentOfType(ObjJFormalVariableType::class.java) -> {
                getClassNameCompletions(resultSet, element)
            }
            element.hasParentOfType(ObjJInstanceVariableList::class.java) -> {
                if (element.getElementType() == ObjJTypes.ObjJ_AT_FRAGMENT) {
                    addCompletionElementsSimple(resultSet, listOf("accessors"));
                }
                resultSet.stopHere()
            }
            else -> {
                if (element.getContainingScope() == ReferencedInScope.FILE) {
                    addFileLevelCompletions(resultSet, element)
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
                addVariableNameCompletionElements(resultSet, element)
                getClassNameCompletions(resultSet, element)
            }
        }
    }

    private fun addVariableNameCompletionElements(resultSet: CompletionResultSet, element:PsiElement) {
        val variableName = element as? ObjJVariableName ?: element.parent as? ObjJVariableName
        val results = if (variableName != null) {
            ObjJVariableNameCompletionContributorUtil.getVariableNameCompletions(variableName) as MutableList<String>
        } else {
            mutableListOf()
        }

        if (variableName?.getParentOfType(ObjJMethodHeaderDeclaration::class.java) == null && (variableName?.indexInQualifiedReference ?: 0) < 1 && (variableName?.text?.replace(ObjJCompletionContributor.CARET_INDICATOR, "")?.trim()?.length ?: 0) > 0) {
            ObjJFunctionNameCompletionProvider.appendCompletionResults(resultSet, element)
            results.addAll(getKeywordCompletions(variableName))
            results.addAll(getInClassKeywords(variableName))
            results.addAll(Arrays.asList("YES", "yes", "NO", "no", "true", "false"))
        }
        addCompletionElementsSimple(resultSet, results)
    }

    private fun addProtocolNameCompletionElements(resultSet:CompletionResultSet, element:PsiElement, queryString:String) {
        val results = ObjJProtocolDeclarationsIndex.instance.getKeysByPattern("$queryString(.+)", element.project) as MutableList<String>
        addCompletionElementsSimple(resultSet, results)

    }

    internal fun getClassNameCompletions(resultSet: CompletionResultSet, element: PsiElement?) {
        if (element == null) {
            return
        }
        if (element.hasParentOfType(ObjJProtocolLiteral::class.java) || element.hasParentOfType(ObjJInheritedProtocolList::class.java) || element.hasParentOfType(ObjJFormalVariableType::class.java) || element.getElementType() in ObjJTokenSets.COMMENTS) {
            ObjJProtocolDeclarationsIndex.instance.getAllKeys(element.project).forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler))
            }
        }
        if (element.hasParentOfType(ObjJFormalVariableType::class.java)) {
            PRIMITIVE_VAR_NAMES.forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler))
            }
        }
        if(element.hasParentOfType( ObjJCallTarget::class.java) || element.hasParentOfType(ObjJFormalVariableType::class.java) || element.getElementType() in ObjJTokenSets.COMMENTS) {
            ObjJImplementationDeclarationsIndex.instance.getAll(element.project).forEach {
                if (ObjJIgnoreEvaluatorUtil.shouldIgnoreUnderscore(element) || ObjJIgnoreEvaluatorUtil.isIgnored(element, ObjJSuppressInspectionFlags.IGNORE_CLASS)) {
                    return@forEach
                }
                if (ObjJIgnoreEvaluatorUtil.isIgnored(it) || ObjJIgnoreEvaluatorUtil.noIndex(it, NoIndex.CLASS) || ObjJIgnoreEvaluatorUtil.noIndex(it, NoIndex.ANY)) {
                    return@forEach
                }
                resultSet.addElement(LookupElementBuilder.create(it.getClassNameString()).withInsertHandler(ObjJClassNameInsertHandler))
            }
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
}