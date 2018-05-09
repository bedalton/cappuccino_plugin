package org.cappuccino_project.ide.intellij.plugin.contributor

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ASTNode
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.cappuccino_project.ide.intellij.plugin.contributor.handlers.ObjJFunctionNameInsertHandler
import org.cappuccino_project.ide.intellij.plugin.contributor.handlers.ObjJVariableInsertHandler
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJFunctionsIndex
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.Companion.PRIMITIVE_VAR_NAMES
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import org.cappuccino_project.ide.intellij.plugin.psi.utils.getParentOfType
import org.cappuccino_project.ide.intellij.plugin.psi.utils.getPreviousNonEmptyNode
import org.cappuccino_project.ide.intellij.plugin.psi.utils.isNewVarDec

import java.util.ArrayList
import java.util.Arrays
import java.util.logging.Logger

import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils.EMPTY_STRING_ARRAY

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
        } else {
            results = ArrayList()
        }
        results.addAll(getClassNameCompletions(element))

        if (results.isEmpty()) {
            resultSet.stopHere()
        }
        addCompletionElementsSimple(resultSet, results)
    }

    private fun getClassNameCompletions(element: PsiElement?): List<String> {
        if (element == null) {
            return EMPTY_STRING_ARRAY
        }
        var doSearch = false
        val arrayLiteral = element.getParentOfType( ObjJArrayLiteral::class.java)
        if (arrayLiteral != null && arrayLiteral.atOpenbracket == null && arrayLiteral.exprList.size == 1) {
            doSearch = true
        }
        var prev = element.getPreviousNonEmptyNode(true)
        if (prev != null && prev.text == "<") {
            prev = prev.psi.getPreviousNonEmptyNode(true)
            if (prev != null && prev.text == "id") {
                doSearch = true
            }
        }
        val callTarget = element.getParentOfType( ObjJCallTarget::class.java)
        if (callTarget != null) {
            doSearch = true
        }
        if (!doSearch) {
            return EMPTY_STRING_ARRAY
        }
        val results = ObjJClassDeclarationsIndex.instance.getKeysByPattern(element.text.replace(CARET_INDICATOR, "(.*)"), element.project) as MutableList
        results.addAll(ArrayUtils.search(PRIMITIVE_VAR_NAMES, element.text.substring(0, element.text.indexOf(CARET_INDICATOR))))
        results.addAll(ArrayUtils.search(Arrays.asList("self", "super"), element.text.substring(0, element.text.indexOf(CARET_INDICATOR))))
        return results
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