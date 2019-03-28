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
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.PRIMITIVE_VAR_NAMES
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.NoIndex
import cappuccino.ide.intellij.plugin.references.ObjJIgnoreEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.utils.ArrayUtils

import java.util.Arrays
import java.util.logging.Logger

import cappuccino.ide.intellij.plugin.utils.ArrayUtils.EMPTY_STRING_ARRAY
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import cappuccino.ide.intellij.plugin.utils.orTrue
import com.intellij.psi.impl.source.tree.PsiCommentImpl

/**
 * Completion provider providing the heavy lifting for all completions
 * @todo check if there is a better way
 */
object ObjJBlanketCompletionProvider : CompletionProvider<CompletionParameters>() {

    // Caret indicator for use in completion
    const val CARET_INDICATOR = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED

    // Logger
    @Suppress("unused")
    private val LOGGER by lazy {
        Logger.getLogger(ObjJBlanketCompletionProvider::class.java.name)
    }
    // Possible Accessor property types
    private val ACCESSOR_PROPERTY_TYPES = listOf("property", "getter", "setter", "readonly", "copy")
    // Preproc # keywords
    private val PRE_PROC_KEYWORDS = listOf(
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

    // @ keywords
    private val AT_KEYWORDS = listOf(
            "import",
            "typedef",
            "class",
            "implementation",
            "protocol",
            "end"
    )


    /**
     * Add all possible completions to result set
     */
    override fun addCompletions(
            parameters: CompletionParameters, context: ProcessingContext,
            resultSet: CompletionResultSet) {
        val element = parameters.position
        val queryString = element.text.substring(0, element.text.indexOf(CARET_INDICATOR))
        when {
            // Comment
            element.elementType in ObjJTokenSets.COMMENTS || element is PsiCommentImpl ->
                ObjJCommentCompletionProvider.addCommentCompletions(resultSet, element)
            // Accessor property
            element.isOrHasParentOfType(ObjJAccessorPropertyType::class.java) ->
                addCompletionElementsSimple(resultSet, ArrayUtils.search(ACCESSOR_PROPERTY_TYPES, queryString))
            // Method call
            isMethodCallSelector(element) ->
                ObjJMethodCallCompletionContributor.addSelectorLookupElementsFromSelectorList(resultSet, element)
            // Inherited protocol list
            element.hasParentOfType(ObjJInheritedProtocolList::class.java) ->
                addProtocolNameCompletionElements(resultSet, element, queryString)
            // Formal Variable type
            element.isOrHasParentOfType(ObjJFormalVariableType::class.java) ->
                formalVariableTypeCompletion(element, resultSet)
            // Instance variable list
            element.hasParentOfType(ObjJInstanceVariableList::class.java) ->
                instanceVariableListCompletion(element, resultSet)
            // All others
            else -> genericCompletion(element, resultSet)
        }
    }

    /**
     * Provides completions results for less specific elements or cases
     */
    private fun genericCompletion(element: PsiElement, resultSet: CompletionResultSet) {
        if (element.getContainingScope() == ReferencedInScope.FILE) {
            addFileLevelCompletions(resultSet, element)
        }

        if (ObjJVariablePsiUtil.isNewVarDec(element)) {
            resultSet.stopHere()
            return
        }
        // If @ and in class declartion
        if (element.text.startsWith("@") && element.hasParentOfType(ObjJClassDeclarationElement::class.java)) {
            resultSet.addElement(LookupElementBuilder.create("end").withPresentableText("@end"))
        }

        // Add variable name completions if applicable
        addVariableNameCompletionElements(resultSet, element)

        // Add class name completions if applicable
        getClassNameCompletions(resultSet, element)
    }

    /**
     * Provides completions for elements inside a instance variable list
     */
    private fun instanceVariableListCompletion(element: PsiElement, resultSet: CompletionResultSet) {
        if (element.elementType == ObjJTypes.ObjJ_AT_FRAGMENT) {
            addCompletionElementsSimple(resultSet, listOf("accessors"))
        }
        resultSet.stopHere()
    }

    /**
     * Adds completions results for formal variable type elements
     */
    private fun formalVariableTypeCompletion(element: PsiElement, resultSet: CompletionResultSet) {
        getClassNameCompletions(resultSet, element)
    }

    /**
     * Adds variable name completion elements
     */
    private fun addVariableNameCompletionElements(resultSet: CompletionResultSet, element: PsiElement) {
        val variableName = element as? ObjJVariableName ?: element.parent as? ObjJVariableName
        val results = if (variableName != null) {
            ObjJVariableNameCompletionContributorUtil.getVariableNameCompletions(variableName) as MutableList<String>
        } else {
            mutableListOf()
        }

        val notInMethodHeaderDeclaration = variableName?.doesNotHaveParentOfType(ObjJMethodHeaderDeclaration::class.java).orTrue()
        val isFirstInQualifiedReference = (variableName?.indexInQualifiedReference ?: 0) < 1
        val hasLength = promptTextHasLength(variableName)
        if (notInMethodHeaderDeclaration && isFirstInQualifiedReference && hasLength) {
            ObjJFunctionNameCompletionProvider.appendCompletionResults(resultSet, element)
            results.addAll(getKeywordCompletions(variableName))
            results.addAll(getInClassKeywords(variableName))
            results.addAll(Arrays.asList("YES", "yes", "NO", "no", "true", "false"))
        }
        results.addAll(ObjJGlobalJSVariablesNames)
        addCompletionElementsSimple(resultSet, results)
    }

    /**
     * Add protocol name completions
     */
    private fun addProtocolNameCompletionElements(resultSet: CompletionResultSet, element: PsiElement, queryString: String) {
        val results = ObjJProtocolDeclarationsIndex.instance.getKeysByPattern("$queryString(.+)", element.project) as MutableList<String>
        addCompletionElementsSimple(resultSet, results)

    }

    /**
     * Get all defined class names as completions
     */
    internal fun getClassNameCompletions(resultSet: CompletionResultSet, element: PsiElement?) {
        if (element == null) {
            return
        }
        // Add protocols if allowed
        if (shouldAddProtocolNameCompletions(element)) {
            ObjJProtocolDeclarationsIndex.instance.getAllKeys(element.project).forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler))
            }
        }

        // Append primitive var types if necessary
        if (shouldAddPrimitiveTypes(element)) {
            ObjJClassType.ADDITIONAL_PREDEFINED_CLASSES.forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler))
            }
        }

        // Append implementation declaration names if in correct context
        if (shouldAddImplementationClassNameCompletions(element)) {
            addImplementationClassNameElements(element, resultSet)
        }
    }

    /**
     * Evaluates whether protocol name completions should be added to completion result
     */
    private fun shouldAddProtocolNameCompletions(element: PsiElement): Boolean {
        return element.hasParentOfType(ObjJProtocolLiteral::class.java) ||
                element.hasParentOfType(ObjJInheritedProtocolList::class.java) ||
                element.hasParentOfType(ObjJFormalVariableType::class.java) ||
                element.elementType in ObjJTokenSets.COMMENTS
    }

    /**
     * Evaluates whether or not primitive var types should be added to completion result
     */
    private fun shouldAddPrimitiveTypes(element: PsiElement): Boolean {
        return element.hasParentOfType(ObjJFormalVariableType::class.java)
    }

    /**
     * Evaluates whether implementation class names should be added to completion result
     */
    private fun shouldAddImplementationClassNameCompletions(element: PsiElement): Boolean {
        return element.hasParentOfType(ObjJCallTarget::class.java) ||
                element.hasParentOfType(ObjJFormalVariableType::class.java) ||
                element.elementType in ObjJTokenSets.COMMENTS ||
                element.parent?.elementType in ObjJTokenSets.COMMENTS

    }

    /**
     * Add implementation class names to result set
     */
    private fun addImplementationClassNameElements(element: PsiElement, resultSet: CompletionResultSet) {
        ObjJImplementationDeclarationsIndex.instance.getAll(element.project).forEach { implementationDeclaration ->
            if (isIgnoredImplementationDeclaration(element, implementationDeclaration)) {
                return@forEach
            }
            resultSet.addElement(LookupElementBuilder.create(implementationDeclaration.getClassNameString()).withInsertHandler(ObjJClassNameInsertHandler))
        }
    }

    /**
     * Determines whether or not an implementation declaration is ignored
     */
    private fun isIgnoredImplementationDeclaration(element: PsiElement, declaration: ObjJImplementationDeclaration): Boolean {
        return ObjJIgnoreEvaluatorUtil.isIgnored(declaration) ||
                ObjJIgnoreEvaluatorUtil.shouldIgnoreUnderscore(element) ||
                ObjJIgnoreEvaluatorUtil.isIgnored(element, ObjJSuppressInspectionFlags.IGNORE_CLASS) ||
                ObjJIgnoreEvaluatorUtil.noIndex(declaration, NoIndex.CLASS) ||
                ObjJIgnoreEvaluatorUtil.noIndex(declaration, NoIndex.ANY)
    }

    /**
     * Gets keywords meant to only be used inside a class declaration
     */
    private fun getInClassKeywords(element: PsiElement?): List<String> {
        if (element == null) {
            return EMPTY_STRING_ARRAY
        }
        return if (element.hasParentOfType(ObjJClassDeclarationElement::class.java)) {
            listOf("self", "super")
        } else
            emptyList()
    }

    /**
     * Adds keywords only used at top level of file to resut set
     */
    private fun addFileLevelCompletions(resultSet: CompletionResultSet, element: PsiElement) {
        val prefix: String
        val resultsTemp: List<String>
        when {
            // At @ fragment keywords
            element.isType(ObjJTypes.ObjJ_AT_FRAGMENT) -> {
                resultsTemp = AT_KEYWORDS
                prefix = "@"
            }
            // Add #(preproc) fragments
            element.isType(ObjJTypes.ObjJ_PP_FRAGMENT) -> {
                resultsTemp = PRE_PROC_KEYWORDS
                prefix = "#"
            }
            // No completions here, set prefix and results to empty
            else -> {
                resultsTemp = mutableListOf()
                prefix = ""
            }
        }
        // Add results to completion result set
        resultsTemp.forEach {
            val lookupElement = prefixedLookupElement(it, prefix)
            resultSet.addElement(lookupElement)
        }
    }

    /**
     * Creates a lookup element with a prefix such as @ or #
     */
    private fun prefixedLookupElement(keyword:String, prefix:String) : LookupElementBuilder {
        return LookupElementBuilder.create(keyword)
                .withPresentableText(prefix + keyword)
                .withInsertHandler { context, _ ->
                    if (!EditorUtil.isTextAtOffset(context, " ")) {
                        EditorUtil.insertText(context.editor, " ", true)
                    }
                }
    }

    /**
     * Gets misc keyword completions
     */
    private fun getKeywordCompletions(element: PsiElement?): List<String> {
        val expression = element.getParentOfType(ObjJExpr::class.java)
        return if (canAddBlockLevelKeywords(element, expression)) {
            EMPTY_STRING_ARRAY
        } else ObjJKeywordsList.keywords
    }

    /**
     * Determines whether or not to add block level keywords
     */
    private fun canAddBlockLevelKeywords(element:PsiElement?, expression:ObjJExpr?) : Boolean {
        if (expression == null)
            return false
        // ""expression.text == element?.text"" means that expression is only made up of the keyword
        return expression.text == element?.text && expression.parent is ObjJBlock
    }

    /**
     * Checks if element is a method call selector
     */
    private fun isMethodCallSelector(element: PsiElement?): Boolean {
        if (element == null) {
            return false
        }
        if (element.doesNotHaveParentOfType(ObjJMethodCall::class.java)) {
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

    /**
     * Checks if the text types so far for completion has any length
     */
    private fun promptTextHasLength(variableName: PsiElement?): Boolean {
        return (variableName?.text?.replace(ObjJCompletionContributor.CARET_INDICATOR, "")?.trim()?.length ?: 0) > 0
    }
}