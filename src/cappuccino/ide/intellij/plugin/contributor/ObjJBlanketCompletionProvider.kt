package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.ObjJClassNamesCompletionProvider.getClassNameCompletions
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJClassNameInsertHandler
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJFunctionNameInsertHandler
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJTrackInsertionHandler
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJVariableInsertHandler
import cappuccino.ide.intellij.plugin.contributor.utils.ObjJCompletionElementProviderUtil.addCompletionElementsSimple
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListArrayType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.collapseWithSuperType
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByPartialNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefPropertiesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefClassElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.toJsClassDefinition
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.*
import cappuccino.ide.intellij.plugin.utils.ArrayUtils.EMPTY_STRING_ARRAY
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.util.ProcessingContext
import java.util.logging.Logger

/**
 * Completion provider providing the heavy lifting for all completions
 * @todo check if there is a better way
 */
object ObjJBlanketCompletionProvider : CompletionProvider<CompletionParameters>() {

    // Caret indicator for use in completion
    const val CARET_INDICATOR = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED

    //LOGGER.warning(@Suppress("unused")
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
        val caretIndex = element.text.indexOf(CARET_INDICATOR)
        if (caretIndex < 0)
            return;
        val queryString = element.text.substring(0, caretIndex)

        when {
            element.hasParentOfType(ObjJTypeDef::class.java) -> {
                resultSet.stopHere()
                return
            }
            element.hasParentOfType(ObjJStringLiteral::class.java) -> {
                val stringLiteral = element.getParentOfType(ObjJStringLiteral::class.java)
                    ?: return
                addStringCompletions(stringLiteral, resultSet)
            }
            element.getPreviousNonEmptySibling(false)?.elementType in ObjJTokenSets.IMPORT_BLOCKS -> {
                resultSet.stopHere()
                return
            }
            element.elementType in ObjJTokenSets.STRING_COMPLETION_LITERALS && element.hasParentOfType(ObjJImportStatementElement::class.java) -> {
                addCompletionsForFrameworkFiles(resultSet, element)
            }
            // Comment
            element.elementType in ObjJTokenSets.COMMENTS || element is PsiCommentImpl ->
                ObjJCommentCompletionProvider.addCommentCompletions(resultSet, element)
            // Accessor property
            element.isOrHasParentOfType(ObjJAccessorPropertyType::class.java) ->
                addCompletionElementsSimple(resultSet, ArrayUtils.search(ACCESSOR_PROPERTY_TYPES, queryString))
            // Method call
            element.elementType == ObjJTypes.ObjJ_AT_FRAGMENT || element.parent.elementType == ObjJTypes.ObjJ_AT_FRAGMENT -> {
                getAtFragmentCompletions(resultSet, element)
                resultSet.stopHere()
                return
            }
            element.hasParentOfType(ObjJImportElement::class.java) -> {
                ObjJImportContributor.addImportCompletions(resultSet, element)
                resultSet.stopHere()
                return
            }
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
            element.parent is ObjJClassDeclarationElement<*> -> {
                getClassNameCompletions(resultSet, element)
                resultSet.stopHere()
                return
            }
            element.hasParentOfType(ObjJSuperClass::class.java) -> {
                ObjJClassNamesCompletionProvider.addImplementationClassNameElements(element, resultSet)
                addCompletionElementsSimple(resultSet, ObjJPluginSettings.ignoredClassNames())
                resultSet.stopHere()
                return
            }
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
            prevSibling.elementType !in ObjJTokenSets.CAN_COMPLETE_AFTER -> {
                resultSet.stopHere()
            }
            else -> genericCompletion(element, resultSet)
        }
        resultSet.stopHere()
    }

    /**
     * Provides completions results for less specific elements or cases
     */
    private fun genericCompletion(element: PsiElement, resultSet: CompletionResultSet) {
        val project = element.project
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
        if (element.hasParentOfType(ObjJExpr::class.java)) {
            resultSet.addElement(LookupElementBuilder.create("function").withInsertHandler(ObjJFunctionNameInsertHandler))
        }

        if (element.getContainingScope() == ReferencedInScope.FILE) {
            addFileLevelCompletions(resultSet, element)
        }

        if (ObjJVariablePsiUtil.isNewVariableDec(element)) {
            resultSet.stopHere()
            return
        }
        // If @ and in class declaration
        if (element.text.startsWith("@")) {
            if (element is ObjJClassDeclarationElement<*> || element.parent is ObjJClassDeclarationElement<*>) {
                resultSet.addElement(LookupElementBuilder.create("end").withPresentableText("@end"))
            }
            if (element.parent is ObjJExpr || element.parent.parent is ObjJExpr) {
                resultSet.addElement(LookupElementBuilder.create("selector").withPresentableText("@selector"))
                resultSet.addElement(LookupElementBuilder.create("protocol").withPresentableText("@protocol"))
            }
        }

        // Add class name completions if applicable
        getClassNameCompletions(resultSet, element)

        // Add variable name completions if applicable
        addVariableNameCompletionElements(resultSet, element)


        if (shouldAddJsClassNames(element)) {
            JsTypeDefClassesByNameIndex.instance.getByPatternFlat(element.text.toIndexPatternString(), project)
                    .filterNot {
                        it.isSilent || (it.isQuiet && (element.textWithoutCaret.length > 5))
                    }
                    .mapNotNull {
                        (it as? JsTypeDefClassElement)?.className
                    }.forEach {
                        val lookupElement = LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler)
                        val prioritizedLookupElement = PrioritizedLookupElement.withPriority(lookupElement, ObjJInsertionTracker.getPoints(it, ObjJCompletionContributor.TYPEDEF_PRIORITY))
                        resultSet.addElement(prioritizedLookupElement)
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
        addCompletionElementsSimple(resultSet, listOf(
                "YES",
                "NO",
                "Nil",
                "nil",
                "true",
                "false",
                "null",
                "undefined"
        ), 200.0)
        if (element.hasParentOfType(ObjJMethodHeader::class.java)) {
            return
        }
        val variableName = element as? ObjJVariableName ?: element.parent as? ObjJVariableName
        val results: List<ObjJVariableName> = if (variableName != null) {
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
        var properties = JsTypeDefPropertiesByNameIndex.instance.getByPatternFlat(element.text.toIndexPatternString(), element.project).filterNot { it.isSilent }
        properties = properties.filter {
            it.enclosingNamespace.isEmpty()
        }

        if (shouldIgnoreIgnoredGlobals) {
            addCompletionElementsSimple(resultSet, properties.filterNot { it.isSilent }.map { it.propertyNameString }, -200.0)
        } else {
            addCompletionElementsSimple(resultSet, properties.filterNot { it.isSilent || it.isQuiet }.map { it.propertyNameString }, -200.0)
        }
    }


    private fun addVariableNameCompletionElementsWithPriority(resultSet: CompletionResultSet, variables: List<ObjJVariableName>) {
        variables.forEach {
            val type = inferQualifiedReferenceType(it.previousSiblings + it, createTag())?.toClassListString()?.replace("(\\?\\s*\\||\\|\\s*\\?)".toRegex(), "")
            var lookupElement = LookupElementBuilder.create(it.text)
            if (type.isNotNullOrBlank())
                lookupElement = lookupElement.withPresentableText("${it.text} : $type")
            val asFunctionDeclaration = it.parentFunctionDeclaration
            val priority = ObjJCompletionContributor.TARGETTED_VARIABLE_SUGGESTION_PRIORITY
            lookupElement = if (asFunctionDeclaration != null) {
                lookupElement
                        .withInsertHandler(ObjJFunctionNameInsertHandler)
                        .withTailText("(" + asFunctionDeclaration.parameterNames.joinToString(", ") + ")")
            } else {
                lookupElement.withInsertHandler(ObjJVariableInsertHandler)
            }
            lookupElement = lookupElement.withBoldness(true)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, ObjJInsertionTracker.getPoints(it.text,priority)))
        }
    }

    private fun addGlobalVariableCompletions(resultSet: CompletionResultSet, variableName: PsiElement) {
        ObjJGlobalVariableNamesIndex.instance.getByPatternFlat(variableName.text.toIndexPatternString(), variableName.project).forEach {
            ProgressIndicatorProvider.checkCanceled()
            val parameters = it.variableName.parentFunctionDeclaration?.parameterNames
                    ?: inferQualifiedReferenceType(listOf(it.variableName), createTag())?.functionTypes?.maxBy { it.parameters.size }?.parameters?.map { it.name }
            if (parameters != null) {
                ObjJFunctionNameCompletionProvider.addGlobalFunctionName(resultSet, it.variableNameString, parameters)
            } else {
                val lookupElement = LookupElementBuilder.create(it.variableNameString)
                        .withInsertHandler(ObjJVariableInsertHandler)
                resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, ObjJInsertionTracker.getPoints(it.variableNameString, ObjJCompletionContributor.GENERIC_INSTANCE_VARIABLE_SUGGESTION_PRIORITY)))
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
        val previousSiblingText = element.getPreviousNonEmptySibling(false)?.text
        when {
            // At @ fragment keywords
            element.isType(ObjJTypes.ObjJ_AT_FRAGMENT) || previousSiblingText == "@" -> {
                resultsTemp = AT_KEYWORDS
                prefix = "@"
            }
            // Add #(preproc) fragments
            element.isType(ObjJTypes.ObjJ_PP_FRAGMENT) || previousSiblingText == "#" -> {
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
    private fun getKeywordCompletions(resultSet: CompletionResultSet, element: PsiElement?) {
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
            var lookupElement = LookupElementBuilder.create(it)
                    .withPresentableText("@$it")
            if (it == "selector") {
                lookupElement = lookupElement.withInsertHandler(ObjJFunctionNameInsertHandler)
            }
            resultSet.addElement(lookupElement)
        }
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    private fun getPreProcFragmentCompletions(resultSet: CompletionResultSet, element: PsiElement) {

    }

    private fun addCompletionsForFrameworkFiles(resultSet: CompletionResultSet, element: PsiElement) {
        val framework = element.enclosingFrameworkName
        val alreadyImported = (element.containingFile as? ObjJFile)
                ?.getImportedFiles(recursive = false, cache = true)
                .orEmpty()
                .map { it.name }
        val files = ObjJFrameworkUtils.getFrameworkFileNames(element.project, framework).ifEmpty {
            ObjJFrameworkUtils.getFileNamesInEnclosingDirectory(element.containingFile, true)
        }.filter { it !in alreadyImported }
        val quoteType = element.getPreviousNonEmptySibling(false).elementType
        val quoteChar = when (quoteType) {
            ObjJTypes.ObjJ_SINGLE_QUO -> "'"
            ObjJTypes.ObjJ_DOUBLE_QUO -> "\""
            else -> ""
        }
        val isNextTokenClosingQuote = element.getNextNonEmptySibling(false).elementType == quoteType
        val completionHandler = if (!isNextTokenClosingQuote) {
            InsertHandler { context: InsertionContext, lookupElement: LookupElement ->
                EditorUtil.insertText(context.editor, quoteChar, (lookupElement.psiElement?:element).textRange.startOffset + lookupElement.lookupString.length, true)
            }
        } else null
        for (file in files) {
            val lookupElement = if (completionHandler != null) {
                LookupElementBuilder.create(file)
                        .withInsertHandler(completionHandler)
            } else {
                LookupElementBuilder.create(file)
            }
            resultSet.addElement(lookupElement)
        }
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
        val index = qualifiedNameComponent.indexInQualifiedReference
        if (index <= 0) {
            return
        }
        val project = element.project
        val previousComponents = qualifiedNameComponent.previousSiblings
        var isStatic = false
        if (previousComponents.isNotEmpty()) {
            val namespace = previousComponents.joinToString(".") { it.text }
            val nextIndex = previousComponents.size
            val classNamespaceNames = JsTypeDefClassesByPartialNamespaceIndex.instance[namespace, project].filterIsInstance<JsTypeDefClassElement>().mapNotNull {
                val namespaceComponents = it.namespaceComponents
                if (namespaceComponents.size > nextIndex)
                    it.namespaceComponents[nextIndex]
                else
                    null
            }
            classNamespaceNames.forEach {
                val lookupElement = LookupElementBuilder.create(it).withInsertHandler(ObjJTrackInsertionHandler)
                val prioritizedLookupElement = PrioritizedLookupElement.withPriority(lookupElement, ObjJInsertionTracker.getPoints(it, ObjJCompletionContributor.TYPEDEF_PRIORITY))
                resultSet.addElement(prioritizedLookupElement)
            }
            isStatic = classNamespaceNames.isNotEmpty()

        }
        val inferred = inferQualifiedReferenceType(previousComponents, createTag()) ?: return
        var classes = inferred.classes.toMutableSet() + (if (inferred.types.any { it is JsTypeListArrayType }) listOf("Array") else emptyList())
        if (("CPString" in classes || "string" in classes) && "String" !in classes) {
            classes = classes + "String"
        }
        val collapsedClass = classes.flatMap { className ->
            JsTypeDefClassesByNameIndex.instance[className, project].map {
                it.toJsClassDefinition()
            }
        }.toSet().collapseWithSuperType(project)
        val firstItem = previousComponents[0].text.orEmpty()
        val includeStatic = isStatic || (index == 1 && classes.any { it == firstItem }) || JsTypeDefClassesByNamespaceIndex.instance.containsKey(previousComponents.joinToString(".") { it.text }, project)
        val functions = if (includeStatic)
            collapsedClass.staticFunctions
        else
            collapsedClass.functions

        functions.forEach { classFunction ->
            val functionName = classFunction.name ?: return@forEach
            val lookupElementBuilder = LookupElementBuilder
                    .create(functionName)
                    .withTailText("(" + ArrayUtils.join(classFunction.parameters.map { it.name }, ",") + ")")
                    .withInsertHandler(ObjJFunctionNameInsertHandler)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, ObjJInsertionTracker.getPoints(functionName, ObjJCompletionContributor.FUNCTIONS_NOT_IN_FILE_PRIORITY)))
        }

        val properties = if (includeStatic)
            collapsedClass.staticProperties
        else
            collapsedClass.properties

        properties.forEach {
            val classStrings = it.types.withoutAnyType().joinToString("|")
            var lookupElementBuilder = LookupElementBuilder
                    .create(it.name)
            if (classStrings.isNotNullOrBlank()) {
                lookupElementBuilder = lookupElementBuilder
                        .withTailText(":" + classStrings)
            }
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, ObjJInsertionTracker.getPoints(it.name, ObjJCompletionContributor.FUNCTIONS_NOT_IN_FILE_PRIORITY)))
        }
        inferred.functionTypes.forEach { jsFunction ->
            val lookupElementBuilder = LookupElementBuilder
                    .create("()")
                    .withTailText("(" + jsFunction.parameters.joinToString(", ") {
                        val classStrings = it.types.withoutAnyType().joinToString("|").ifEmptyNull() ?: return@joinToString it.name
                        it.name + ":" +  classStrings
                    } + ")")
                    .withInsertHandler(ObjJFunctionNameInsertHandler)
            resultSet.addElement(lookupElementBuilder)
        }
        inferred.properties.forEach {
            val propertyName = it.name ?: return@forEach
            val propertyType = when (it) {
                is JsTypeDefNamedProperty -> it.types
                is JsTypeListType.JsTypeListFunctionType -> it.returnType
                else -> INFERRED_ANY_TYPE
            }
            val classStrings = propertyType?.toClassListString(null)
            var lookupElementBuilder = LookupElementBuilder
                    .create(propertyName)
                    .withBoldness(true)
                    .withInsertHandler(ObjJTrackInsertionHandler)
            if (classStrings.isNotNullOrBlank())
                lookupElementBuilder = lookupElementBuilder
                    .withTailText(":" + classStrings)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, ObjJInsertionTracker.getPoints(propertyName, ObjJCompletionContributor.TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY)))

        }
    }
}

internal val PsiElement.textWithoutCaret: String
    get() = this.text?.replace(ObjJBlanketCompletionProvider.CARET_INDICATOR.toRegex(), "") ?: ""

internal fun String.toIndexPatternString(): String {
    val queryBody = "([^A-Z_]*?)"
    val stringBuilder = StringBuilder("[_]?[A-Z_$]*?")
    this.replace(ObjJCompletionContributor.CARET_INDICATOR, "(.*)").split("(?<=[A-Z_])".toRegex()).forEach {
        stringBuilder.append(queryBody).append(it)
    }
    return stringBuilder.append(queryBody).append(".*").toString()
    //return "[_]?"+this.replace(ObjJCompletionContributor.CARET_INDICATOR, "([^:]*?)")+"?(.*)"
}