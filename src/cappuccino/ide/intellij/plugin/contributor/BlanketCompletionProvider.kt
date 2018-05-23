package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJClassNameInsertHandler
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ASTNode
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJFunctionNameInsertHandler
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJVariableInsertHandler
import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.PRIMITIVE_VAR_NAMES
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.utils.ArrayUtils

import java.util.ArrayList
import java.util.Arrays
import java.util.logging.Logger

import cappuccino.ide.intellij.plugin.utils.ArrayUtils.EMPTY_STRING_ARRAY
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import java.util.logging.Level

class BlanketCompletionProvider : CompletionProvider<CompletionParameters>() {

    private val CARET_INDICATOR = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED

    override fun addCompletions(
            parameters: CompletionParameters, context: ProcessingContext,
            resultSet: CompletionResultSet) {
        //LOGGER.log(Level.INFO, "Trying to get completion parameters.");
        val element = parameters.position
        val parent = element.parent
        /*LOGGER.log(Level.INFO,
                "Parent is of type: <"+parent.getNode().getElementType().toString()+"> with value: <"+parent.getText()+">\n"
                        +   "Child is of type <"+element.getNode().getElementType()+"> with text <"+element.getText()+">"
        );*/
        val results: MutableList<String>
        val queryString = element.text.substring(0, element.text.indexOf(CARET_INDICATOR))
        if (element is ObjJAccessorPropertyType || element.getParentOfType( ObjJAccessorPropertyType::class.java) != null) {
            results = ArrayUtils.search(ACCESSSOR_PROPERTY_TYPES, queryString) as MutableList<String>
        } else if (element is ObjJVariableName || parent is ObjJVariableName) {
            if (element.hasParentOfType(ObjJInstanceVariableList::class.java)) {
                resultSet.stopHere()
                return
            }
            if (queryString.trim { it <= ' ' }.isEmpty()) {
                //LOGGER.log(Level.INFO, "Query string is empty");
                resultSet.stopHere()
                return
            }
            if (isNewVarDec(element)) {
                resultSet.stopHere()
                return
            }
            val variableName = (element as? ObjJVariableName ?: parent) as ObjJVariableName
            results = ObjJVariableNameCompletionContributorUtil.getVariableNameCompletions(variableName) as MutableList<String>
            appendFunctionCompletions(resultSet, element)
            results.addAll(getKeywordCompletions(variableName))
            results.addAll(getInClassKeywords(variableName))
            results.addAll(Arrays.asList("YES", "yes", "NO", "no", "true", "false"))
        } else if (PsiTreeUtil.getParentOfType(element, ObjJMethodCall::class.java) != null) {
            //LOGGER.log(Level.INFO, "Searching for selector completions.");
            ObjJMethodCallCompletionContributorUtil.addSelectorLookupElementsFromSelectorList(resultSet, element)
            return
        } else if (PsiTreeUtil.getParentOfType(element, ObjJInheritedProtocolList::class.java) != null) {
            results = ObjJProtocolDeclarationsIndex.instance.getKeysByPattern("$queryString(.+)", element.project) as MutableList<String>
        } else if (element.getContainingScope() == ReferencedInScope.FILE) {
            val prefix:String
            when {
                element.isType(ObjJTypes.ObjJ_AT_FRAGMENT) -> {
                    results = mutableListOf(
                            "import",
                            "typedef",
                            "class",
                            "implementation",
                            "protocol"
                    )
                    prefix = "@"
                }
                element.isType(ObjJTypes.ObjJ_PP_FRAGMENT) -> {
                    results = mutableListOf(
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
                    LOGGER.log(Level.INFO, "File level completion for token type ${element.getElementType().toString()} failed.")
                    results = mutableListOf()
                    prefix = ""
                }
            }
            results.forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withPresentableText(prefix+it).withInsertHandler({
                    context, _ -> if (!EditorUtil.isTextAtOffset(context, " ")) {
                        EditorUtil.insertText(context.editor, " ", true)
                    }
                }))
            }
            if (results.isNotEmpty()) {
                resultSet.stopHere()
                return
            }
        } else {
            LOGGER.log(Level.INFO, "Completion provider for element in scope: "+element.getContainingScope())
            results = mutableListOf()
        }
        getClassNameCompletions(resultSet, element)
        if (results.isEmpty()) {
            resultSet.stopHere()
        }
        addCompletionElementsSimple(resultSet, results)
    }

    private fun getClassNameCompletions(resultSet: CompletionResultSet, element: PsiElement?) {
        if (element == null) {
            return
        }
        if (element.hasParentOfType(ObjJInheritedProtocolList::class.java) || element.hasParentOfType(ObjJFormalVariableType::class.java)) {
            ObjJProtocolDeclarationsIndex.instance.getAllKeys(element.project).forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler.instance))
            }
        }
        if (element.hasParentOfType(ObjJFormalVariableType::class.java)) {
            PRIMITIVE_VAR_NAMES.forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler.instance))
            }
        }
        if(element.hasParentOfType( ObjJCallTarget::class.java) || element.hasParentOfType(ObjJFormalVariableType::class.java)) {
            ObjJImplementationDeclarationsIndex.instance.getAllKeys(element.project).forEach{
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler.instance))
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
        for (functionName in functions.keys) {
            for (function in functions.get(functionName)!!) {
                ProgressIndicatorProvider.checkCanceled()
                val priority = if (PsiTreeUtil.findCommonContext(function, element) != null) ObjJCompletionContributor.FUNCTIONS_IN_FILE_PRIORITY else ObjJCompletionContributor.FUNCTIONS_NOT_IN_FILE_PRIORITY

                val lookupElementBuilder = LookupElementBuilder
                        .create(functionName)
                        .withTailText("(" + ArrayUtils.join(function.paramNames as List<String>, ",") + ") in " + ObjJPsiImplUtil.getFileName(function))
                        .withInsertHandler(ObjJFunctionNameInsertHandler.instance)
                resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, priority))
            }
        }
    }

    companion object {

        private val LOGGER = Logger.getLogger(BlanketCompletionProvider::class.java.name)

        private val ACCESSSOR_PROPERTY_TYPES = Arrays.asList("property", "getter", "setter", "readonly", "copy")
    }
}