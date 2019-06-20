package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.ObjJClassNamesCompletionProvider.getClassNameCompletions
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJClassNameInsertHandler
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJFunctionNameInsertHandler
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJVariableInsertHandler
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import cappuccino.ide.intellij.plugin.contributor.utils.ObjJCompletionElementProviderUtil.addCompletionElementsSimple
import cappuccino.ide.intellij.plugin.indices.*
import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.inference.inferQualifiedReferenceType
import cappuccino.ide.intellij.plugin.inference.parentFunctionDeclaration
import cappuccino.ide.intellij.plugin.inference.toInferenceResult
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.utils.*

import java.util.logging.Logger

import cappuccino.ide.intellij.plugin.utils.ArrayUtils.EMPTY_STRING_ARRAY
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiFile
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
            "end",
            "selector",
            "global"
    )


    /**
     * Add all possible completions to result set
     */
    override fun addCompletions(
            parameters: CompletionParameters, context: ProcessingContext,
            resultSet: CompletionResultSet) {
        val element = parameters.position
        val prevSibling = element.getPreviousNonEmptySibling(true)
        val queryString = element.text.substring(0, element.text.indexOf(CARET_INDICATOR))

        //LOGGER.info("Element<${element.text}> is ${element.tokenType()} in parent ${element.parent?.elementType}; PrevSibling: ${prevSibling?.tokenType()}")
        when {
            element.hasParentOfType(ObjJTypeDef::class.java) -> {
                resultSet.stopHere()
                return
            }
            element.getPreviousNonEmptySibling(false)?.elementType in ObjJTokenSets.IMPORT_BLOCKS -> {
                resultSet.stopHere()
                return
            }
            // Comment
            element.elementType in ObjJTokenSets.COMMENTS || element is PsiCommentImpl ->
                ObjJCommentCompletionProvider.addCommentCompletions(resultSet, element)
            // Accessor property
            element.isOrHasParentOfType(ObjJAccessorPropertyType::class.java) ->
                addCompletionElementsSimple(resultSet, ArrayUtils.search(ACCESSOR_PROPERTY_TYPES, queryString))
            // Method call
            isMethodCallSelector(element) ->
                ObjJMethodCallCompletionContributor.addSelectorLookupElementsFromSelectorList(resultSet, element)
            element.hasParentOfType(ObjJSelectorLiteral::class.java) ->
                ObjJSelectorLiteralCompletionContributor.addSelectorLookupElementsFromSelectorList(resultSet, element)
            // Inherited protocol list
            element.hasParentOfType(ObjJInheritedProtocolList::class.java) ->
                ObjJClassNamesCompletionProvider.addProtocolNameCompletionElements(resultSet, element, queryString)
            // Formal Variable type
            element.isOrHasParentOfType(ObjJFormalVariableType::class.java) ->
                formalVariableTypeCompletion(element, resultSet)
            // Function Name
            element.isOrHasParentOfType(ObjJFunctionName::class.java) ->
                ObjJFunctionNameCompletionProvider.appendCompletionResults(resultSet, element)
            // Instance variable list
            element.hasParentOfType(ObjJInstanceVariableList::class.java) ->
                instanceVariableListCompletion(element, resultSet)
            // All others
            element.parent is ObjJClassDeclarationElement<*> -> getClassNameCompletions(resultSet, element)
            element.elementType == ObjJTypes.ObjJ_AT_FRAGMENT -> getAtFragmentCompletions(resultSet, element)
            prevSibling.elementType == ObjJTypes.ObjJ_TRY_STATEMENT -> {
                if (prevSibling.getChildOfType(ObjJCatchProduction::class.java) == null) {
                    resultSet.addElement(LookupElementBuilder.create("catch").withInsertHandler(ObjJFunctionNameInsertHandler))
                }
                if (prevSibling.getChildOfType(ObjJFinallyProduction::class.java) == null) {
                    resultSet.addElement(LookupElementBuilder.create("finally"))
                }
                genericCompletion(element, resultSet)
            }
            prevSibling is ObjJSwitchStatement && element.getNextNonEmptyNodeType(true) == ObjJTypes.ObjJ_CLOSE_BRACE -> {
                resultSet.addElement(LookupElementBuilder.create("case"))
                resultSet.stopHere()
                return
            }
            element.parent is ObjJObjectLiteral && prevSibling?.text != ":" -> {
                resultSet.stopHere()
            }
            prevSibling.elementType !in ObjJTokenSets.CAN_COMPLETE_AFTER ->{
                //LOGGER.info("Cannot complete after ${prevSibling.elementType}")
                resultSet.stopHere()
            }
            else -> genericCompletion(element, resultSet)
        }
    }

    /**
     * Provides completions results for less specific elements or cases
     */
    private fun genericCompletion(element: PsiElement, resultSet: CompletionResultSet) {
        if (element.getPreviousNonEmptySibling(true)?.text?.endsWith(".") == true) {
            appendQualifiedReferenceCompletions(element, resultSet)
            return
        }
        val text = element.textWithoutCaret
        // Prevent completion when keyword is used
        if (text in ObjJKeywordsList.keywords)
            return

        val variableName = element.thisOrParentAs(ObjJVariableName::class.java)
        val parentAsVariableDeclaration = variableName?.parent as? ObjJVariableDeclarationList
                ?: variableName?.parent?.parent?.parent as? ObjJVariableDeclarationList
        if (parentAsVariableDeclaration?.getPreviousNonEmptySibling(true)?.text == "var") {
            return
        }

        ObjJFunctionNameCompletionProvider.appendCompletionResults(resultSet, element)

        if (element.hasParentOfType(ObjJExpr::class.java))
            resultSet.addElement(LookupElementBuilder.create("function").withInsertHandler(ObjJFunctionNameInsertHandler))

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

        // Add class name completions if applicable
        getClassNameCompletions(resultSet, element)

        // Add variable name completions if applicable
        addVariableNameCompletionElements(resultSet, element)


        if (shouldAddJsClassNames(element)) {
            globalJsClassNames.forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler))
            }
        }
    }

    /**
     * Provides completions for elements inside a instance variable list
     */
    private fun instanceVariableListCompletion(element: PsiElement, resultSet: CompletionResultSet) {
        if (element.elementType == ObjJTypes.ObjJ_AT_FRAGMENT) {
            addCompletionElementsSimple(resultSet, listOf("accessors", "outlet"))
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

        if (element.hasParentOfType(ObjJMethodHeader::class.java)) {
            return
        }
        val variableName = element as? ObjJVariableName ?: element.parent as? ObjJVariableName
        val results:List<ObjJVariableName> = if (variableName != null) {
            ObjJVariableNameCompletionContributorUtil.getVariableNameCompletions(variableName)
        } else {
            emptyList()
        }
        //val selectorTargets = getSelectorTargets(element)
        addVariableNameCompletionElementsWithPriority(resultSet, results)

        val notInMethodHeaderDeclaration = variableName?.doesNotHaveParentOfType(ObjJMethodHeaderDeclaration::class.java).orTrue()
        val isFirstInQualifiedReference = (variableName?.indexInQualifiedReference ?: 0) < 1
        val hasLength = promptTextHasLength(variableName)
        if (notInMethodHeaderDeclaration && isFirstInQualifiedReference && hasLength) {
            ObjJFunctionNameCompletionProvider.appendCompletionResults(resultSet, element)
            addGlobalVariableCompletions(resultSet, element)
            getKeywordCompletions(resultSet, variableName)
            addCompletionElementsSimple(resultSet, getInClassKeywords(variableName), 30.0)
            addCompletionElementsSimple(resultSet, listOf("YES", "NO", "true", "false"), 30.0)
        }
        // Boolean to determine whether to add ignored property values
        val shouldIgnoreIgnoredGlobals = element.text.length - CARET_INDICATOR.length < 5 // 5 is abitrary
        if (shouldIgnoreIgnoredGlobals) {
            addCompletionElementsSimple(resultSet, ObjJGlobalVariableNamesWithoutIgnores, -200.0)
        } else {
            addCompletionElementsSimple(resultSet, ObjJGlobalJSVariablesNames, -200.0)
        }
    }


    private fun addVariableNameCompletionElementsWithPriority(resultSet: CompletionResultSet, variables:List<ObjJVariableName>) {
        variables.forEach {
            val type = inferQualifiedReferenceType(it.previousSiblings + it, createTag())?.toClassListString()?.replace("(\\?\\s*\\||\\|\\s*\\?)".toRegex(), "")
            var lookupElement = LookupElementBuilder.create(it.text)
            if (type.isNotNullOrBlank())
                lookupElement = lookupElement.withPresentableText("${it.text} : $type")
            val asFunctionDeclaration = it.parentFunctionDeclaration
            if (asFunctionDeclaration != null) {
                lookupElement = lookupElement
                        .withInsertHandler(ObjJFunctionNameInsertHandler)
                        .withTailText("(" +asFunctionDeclaration.paramNames.joinToString(", ") +")")
            } else {
                lookupElement = lookupElement.withInsertHandler(ObjJVariableInsertHandler)
            }
            lookupElement = lookupElement.withBoldness(true)
            resultSet.addElement(lookupElement)
        }
    }

    /*


    private fun getSelectorTargets(element: PsiElement) : Set<String> {
        val out = mutableSetOf<String>()
        val selector = element.getParentOfType(ObjJQualifiedMethodCallSelector::class.java) ?: return emptySet()
        val index = selector.index
        val selectorString = selector.getParentOfType(ObjJMethodCall::class.java)?.selectorStrings?.subList(0, index).orEmpty().joinToString(ObjJMethodPsiUtils.SELECTOR_SYMBOL)
        if (selectorString.isNotNullOrBlank()) {
            ObjJMethodFragmentIndex.instance[selectorString, element.project].forEach {
                val thisSelector = it.selectorList.getOrNull(index)?.getParentOfType(ObjJMethodDeclarationSelector::class.java) ?: return@forEach
                val type = thisSelector.formalVariableType?.varTypeId?.getIdType(false) ?: thisSelector.formalVariableType?.text ?: return@forEach
                if (type.toLowerCase() !in anyTypes)
                    out.add(type)
            }
        }
        return out

    }

    private fun addVariableNameCompletionElementsWithPriority(resultSet: CompletionResultSet, variables:List<ObjJVariableName>, classFilters:Set<String>) {
        LOGGER.info("Adding completions for ${variables.size} variables")
        variables.forEach {
            addVariableNameCompletionElementWithPriority(resultSet, it, classFilters)
        }
    }

    private fun addVariableNameCompletionElementWithPriority(resultSet:CompletionResultSet, variable:ObjJVariableName, classFilters: Set<String>) {
        LOGGER.info("ADDING COMPLETION FOR: " + variable.text)
        val inferredTypes = inferQualifiedReferenceType(variable.previousSiblings + variable, createTag())
        val classList = inferredTypes?.toClassList("?").orEmpty().filterNot {it == "?"}.toSet()
        val lookupElement = LookupElementBuilder.create(variable.text).withInsertHandler(ObjJVariableInsertHandler)
        if (classList.isEmpty() || classFilters.isEmpty()) {
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, ObjJCompletionContributor.GENERIC_VARIABLE_SUGGESTION_PRIORITY))
            return
        }
        val targeted = classFilters.any {targetClass ->
            targetClass in classList || classList.any {
                ObjJInheritanceUtil.isInstanceOf(variable.project, it, targetClass)
            }
        }
        if (targeted) {
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, ObjJCompletionContributor.TARGETTED_VARIABLE_SUGGESTION_PRIORITY))
        } else {
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, ObjJCompletionContributor.GENERIC_VARIABLE_SUGGESTION_PRIORITY))
        }

    }*/


    private fun addGlobalVariableCompletions(resultSet: CompletionResultSet, variableName: PsiElement) {
        ObjJGlobalVariableNamesIndex.instance.getByPatternFlat(variableName.text.toIndexPatternString(), variableName.project).forEach {
            ProgressIndicatorProvider.checkCanceled()
            if (it.variableName.parentFunctionDeclaration != null || inferQualifiedReferenceType(listOf(it.variableName), createTag())?.functionTypes.isNotNullOrEmpty()) {
                ObjJFunctionNameCompletionProvider.addGlobalFunctionName(resultSet, it.variableNameString)
            } else {
                val lookupElement = LookupElementBuilder.create(it.variableNameString).withInsertHandler(ObjJVariableInsertHandler)
                resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, ObjJCompletionContributor.GENERIC_INSTANCE_VARIABLE_SUGGESTION_PRIORITY))
            }
        }
    }


    private fun shouldAddJsClassNames(@Suppress("UNUSED_PARAMETER") element: PsiElement): Boolean {
        return false
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
    private fun prefixedLookupElement(keyword: String, prefix: String): LookupElementBuilder {
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
    private fun getKeywordCompletions(resultSet: CompletionResultSet, element: PsiElement?){
        if (element !is ObjJCompositeElement)
            return
        val expression = element.getParentOfType(ObjJExpr::class.java)
        if (expression != null && element.text != expression.text)
            return
        if (element.hasParentOfType(ObjJIterationStatement::class.java)) {
            resultSet.addElement(LookupElementBuilder.create("break"))
            resultSet.addElement(LookupElementBuilder.create("continue"))
        }
        val prevSibling = element.getPreviousNonEmptySibling(true)
        if (prevSibling?.text != "new")
            resultSet.addElement(LookupElementBuilder.create("new"))
        if (expression?.hasParentOfType(ObjJExpr::class.java).orFalse())
            return
        if (element.hasParentOfType(ObjJBlock::class.java) || element.parent is PsiFile || element.parent.parent is PsiFile) {
            listOf(
                    "return",
                    "try",
                    "var",
                    "throw",
                    "do"
            ).forEach {
                resultSet.addElement(LookupElementBuilder.create(it))
            }

            listOf(
                    "while",
                    "if",
                    "for",
                    "switch"
            ).forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJFunctionNameInsertHandler))
            }
        } else if (prevSibling is ObjJDoWhileStatement) {
            resultSet.addElement(LookupElementBuilder.create("while").withInsertHandler(ObjJFunctionNameInsertHandler))
        }
        if (element.hasParentOfType(ObjJSwitchStatement::class.java)) {
            resultSet.addElement(LookupElementBuilder.create("case"))
        }
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

    private fun getAtFragmentCompletions(resultSet: CompletionResultSet, element: PsiElement) {
        val parent = element.parent as? ObjJClassDeclarationElement<*>
                ?: element.parent?.parent as? ObjJClassDeclarationElement<*>
        val toAdd = mutableListOf<String>()
        when (parent) {
            is ObjJProtocolDeclaration -> {
                if (parent.atEnd == null) {
                    toAdd.add("end")
                }
                toAdd.add("optional")
                toAdd.add("required")
            }
            is ObjJImplementationDeclaration -> {
                if (parent.atEnd == null) {
                    toAdd.add("end")
                }
            }
            else -> toAdd.addAll(AT_KEYWORDS.filterNot { it == "end" })
        }
        toAdd.forEach {
            val lookupElement = LookupElementBuilder.create(it)
                    .withPresentableText("@$it")
            resultSet.addElement(lookupElement)
        }
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    private fun getPreProcFragmentCompletions(resultSet: CompletionResultSet, element: PsiElement) {

    }

    /**
     * Checks if the text types so far for completion has any length
     */
    private fun promptTextHasLength(variableName: PsiElement?): Boolean {
        return (variableName?.text?.replace(ObjJCompletionContributor.CARET_INDICATOR, "")?.trim()?.length ?: 0) > 0
    }

    private fun appendQualifiedReferenceCompletions(element: PsiElement, resultSet: CompletionResultSet) {
        val qualifiedNameComponent = element as? ObjJQualifiedReferenceComponent
                ?: element.parent as? ObjJQualifiedReferenceComponent ?: return
        //LOGGER.info("Appending Qualified reference completions")
        val index = qualifiedNameComponent.indexInQualifiedReference
        if (index <= 0) {
            //LOGGER.info("Qualified reference is zero indexed")
            return
        }
        val previousComponents = qualifiedNameComponent.previousSiblings
        val inferred = inferQualifiedReferenceType(previousComponents, createTag()) ?: return
        val classes = inferred.classes
        val firstItem = previousComponents[0].text.orEmpty()
        val includeStatic = index == 1 && classes.any { it == firstItem}
        val types = classes.toInferenceResult().jsClasses(element.project).flatten("???")
        val functions = if (includeStatic) types.staticFunctions else types.functions
        val properties = if (includeStatic) types.staticProperties else types.properties
        functions.forEach { classFunction ->
            val lookupElementBuilder = LookupElementBuilder
                    .create(classFunction.name)
                    .withTailText("(" + ArrayUtils.join(classFunction.parameters.map { it.name }, ",") + ")")
                    .withInsertHandler(ObjJFunctionNameInsertHandler)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, ObjJCompletionContributor.FUNCTIONS_NOT_IN_FILE_PRIORITY))
        }

        properties.forEach {
            val lookupElementBuilder = LookupElementBuilder
                    .create(it.name)
                    .withTailText(":" + it.type)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, ObjJCompletionContributor.FUNCTIONS_NOT_IN_FILE_PRIORITY))
        }
        inferred.functionTypes?.forEach { jsFunction ->
            val lookupElementBuilder = LookupElementBuilder
                    .create("()")
                    .withTailText("("+ jsFunction.parameters.map{ it.key + ":" + it.value.classes.joinToString("|")}.joinToString(", ") + ")")
                    .withInsertHandler(ObjJFunctionNameInsertHandler)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, ObjJCompletionContributor.FUNCTIONS_NOT_IN_FILE_PRIORITY))
        }
        inferred.jsObjectKeys?.forEach {
            val lookupElementBuilder = LookupElementBuilder
                    .create(it.key)
                    .withTailText(":" + it.value.classes.joinToString("|") )
                    .withBoldness(true)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, ObjJCompletionContributor.TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY))

        }
    }
}

internal val PsiElement.textWithoutCaret:String get() = this.text?.replace(ObjJBlanketCompletionProvider.CARET_INDICATOR.toRegex(), "") ?: ""

internal fun String.toIndexPatternString():String  {
    val queryBody = "([^A-Z_]*?)"
    val stringBuilder = StringBuilder(queryBody)
    this.replace(ObjJCompletionContributor.CARET_INDICATOR, "(.*)").split("(?<=[A-Z])".toRegex()).forEach {
        stringBuilder.append(it).append(queryBody)
    }
    return stringBuilder.toString()
}