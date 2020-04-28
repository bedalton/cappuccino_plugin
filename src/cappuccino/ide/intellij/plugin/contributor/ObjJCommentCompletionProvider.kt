package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.comments.psi.api.ObjJDocCommentTagLine
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJTrackInsertionHandler
import cappuccino.ide.intellij.plugin.contributor.utils.ObjJCompletionElementProviderUtil.addCompletionElementsSimple
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.psi.ObjJBodyVariableAssignment
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.ObjJVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameAggregatorUtil
import cappuccino.ide.intellij.plugin.psi.utils.elementType
import cappuccino.ide.intellij.plugin.psi.utils.getNextNonEmptySiblingIgnoringComments
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.utils.orFalse
import cappuccino.ide.intellij.plugin.utils.trimFromBeginning
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import java.util.logging.Logger

/**
 * Adds completion results to comment elements
 */
object ObjJCommentCompletionProvider {

    val stripFromCommentTokenBeginning = listOf("*/", "*", "//", " ", "/**", "/*")

    private val LOGGER: Logger by lazy {
        Logger.getLogger("#${ObjJCommentCompletionProvider::class.java.canonicalName}")
    }

    /**
     * Comments completion result set entry point
     */
    fun addCommentCompletions(resultSet: CompletionResultSet, element: PsiElement) {
        val text = element.text?.substringBefore(ObjJBlanketCompletionProvider.CARET_INDICATOR, "") ?: return
        // Divide text by line, and add completion results for it
        for (commentLine in text.split("\\n".toRegex())) {
            addCommentCompletionsForLine(resultSet, element, commentLine)
        }
    }

    /**
     * Adds comment completions for a give line
     */
    private fun addCommentCompletionsForLine(resultSet: CompletionResultSet, element: PsiElement, textIn: String) {
        val text = (element.getParentOfType(ObjJDocCommentTagLine::class.java)?.text ?: textIn).trim()
        if (!text.contains("@v") && !text.contains("@i") && !text.contains("@p") && !text.contains("@r")) {
            return
        }
        // Divide comment line into tokens
        val commentTokenParts: List<String> = splitCommentIntoTokenParts(text)
        val nextSibling = element.node.getNextNonEmptySiblingIgnoringComments()
        val nextSiblingHasParams = nextSibling?.elementType in ObjJTokenSets.HAS_PARAMS || nextSibling?.text?.contains("function\\s*\\(".toRegex()).orFalse()

        // Add completion results for a given statement type
        when {
            // @var completions
            text.contains("@var") ->
                appendVariableCompletions(resultSet, element, commentTokenParts)
            // @Ignore completions
            text.contains("@ignore") ->
                getIgnoreCompletions(resultSet, commentTokenParts)
            text.contains("@param") ->
                appendParameterCompletions(resultSet, element, nextSibling?.psi, commentTokenParts)
            text.contains("@return") -> appendReturnCompletions(resultSet, element, commentTokenParts)
            // Default completions
            else -> addDefaults(resultSet, commentTokenParts, nextSiblingHasParams)
        }
    }

    /**
     * Splits comment line into tokens, trimming and stripping spaces and comment characters
     */
    private fun splitCommentIntoTokenParts(text: String): List<String> {
        return text.split("\\s+".toRegex())
                // Remove comment characters, and whitespaces after comment characters
                .map { it.trim().trimFromBeginning(stripFromCommentTokenBeginning) }
                // Filter out empty strings
                .filterNot { it.isEmpty() }
    }

    /**
     * Append @var completion items
     */
    private fun appendVariableCompletions(resultSet: CompletionResultSet, element: PsiElement, commentTokenParts: List<String>) {
        var afterVar = false
        var indexAfter = -1
        var currentIndex = 0
        val project: Project = element.project
        var lastPart: String? = null
        for (part in commentTokenParts) {
            // Increment index at start to allow for simpler index calculations
            currentIndex++

            // Check if is @var keyword
            if (!afterVar && part == "@var") {
                afterVar = true
                indexAfter = currentIndex
                continue
            }
            // Skip anything before @var
            if (!afterVar) {
                continue
            }
            // Find place within comment tokens after @var start
            when (commentTokenParts.size - indexAfter) {
                // Add class names if first token after @var
                0, 1 -> {
                    if (!isPrevSiblingClassName(lastPart)) {
                        ObjJClassNamesCompletionProvider.getClassNameCompletions(resultSet, element)
                        JsTypeDefClassesByNameIndex.instance.getAllKeys(project).forEach {
                            resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJTrackInsertionHandler))
                        }
                    } else {
                        val variableNames = ObjJVariableNameAggregatorUtil.getSiblingVariableAssignmentNameElements(element, 0).map {
                            it.text
                        }
                        addCompletionElementsSimple(resultSet, variableNames)
                    }
                    lastPart = part
                }
                // Add variable name completions if second token
                2 -> {
                    val variableNames = ObjJVariableNameAggregatorUtil.getSiblingVariableAssignmentNameElements(element, 0).map {
                        it.text
                    }
                    addCompletionElementsSimple(resultSet, variableNames)
                }
                // index too far out, quit
                else -> {
                    return
                }
            }
        }
    }

    /**
     * Append @var completion items
     */
    private fun appendParameterCompletions(resultSet: CompletionResultSet, element: PsiElement, nextSibling: PsiElement?, commentTokenParts: List<String>) {
        var afterVar = false
        var indexAfter = -1
        var currentIndex = 0
        for (part in commentTokenParts) {
            // Increment index at start to allow for simpler index calculations
            currentIndex++

            // Check if is @var keyword
            if (!afterVar && (part == "@param")) {
                afterVar = true
                indexAfter = currentIndex
                continue
            }
            // Skip anything before @var
            if (!afterVar) {
                continue
            }
            // Find place within comment tokens after @var start
            when (commentTokenParts.size - indexAfter) {
                // Add class names if first token after @var
                0, 1 -> {
                    ObjJClassNamesCompletionProvider.getClassNameCompletions(resultSet, element)
                }
                // Add variable name completions if second token
                2 -> {
                    val variableNames = getParameterNames(nextSibling)
                    addCompletionElementsSimple(resultSet, variableNames)
                }
                // index too far out, quit
                else -> {
                    return
                }
            }
        }
    }

    /**
     * Append @var completion items
     */
    private fun appendReturnCompletions(resultSet: CompletionResultSet, element: PsiElement, commentTokenParts: List<String>) {
        var afterVar = false
        var indexAfter = -1
        var currentIndex = 0

        var lastPart: String? = null
        for (part in commentTokenParts) {
            // Increment index at start to allow for simpler index calculations
            currentIndex++

            // Check if is @var keyword
            if (!afterVar && (part == "@return" || part == "@returns")) {
                afterVar = true
                indexAfter = currentIndex
                continue
            }
            // Skip anything before @var
            if (!afterVar) {
                continue
            }
            // Find place within comment tokens after @var start
            when (commentTokenParts.size - indexAfter) {
                // Add class names if first token after @var
                0, 1 -> {
                    if (!isPrevSiblingClassName(lastPart))
                        ObjJClassNamesCompletionProvider.getClassNameCompletions(resultSet, element)
                    lastPart = part
                }
                // index too far out, quit
                else -> {
                    return
                }
            }
        }
    }

    /**
     * Add @ignore completion parameters
     */
    private fun getIgnoreCompletions(resultSet: CompletionResultSet, commentTokenParts: List<String>) {
        var afterIgnoreKeyword = false
        var indexAfter = -1
        var currentIndex = 0
        var precededByComma = false
        loop@ for (part in commentTokenParts) {
            // Increment index at start to allow for simpler index calculations
            currentIndex++

            // Check if current token is @ignore keyword
            if (part == "@ignore") {
                afterIgnoreKeyword = true
                indexAfter = currentIndex
                continue
            }
            // If not after @ignore keyword, continue
            if (!afterIgnoreKeyword) {
                continue
            }
            // store if previous token was comma
            // used to determine if in list of class names
            if (part.trim() == ",") {
                precededByComma = true
                continue
            }
            val place = commentTokenParts.size - indexAfter
            when {
                place <= 1 || precededByComma -> {
                    addCompletionElementsSimple(resultSet, ObjJSuppressInspectionFlags.values().map { it.flag })
                }
                else -> break@loop
            }

            // Update status of if preceded by comma
            precededByComma = part.trim() == ","
        }
    }

    /**
     * Adds default completions to result set
     */
    private fun addDefaults(resultSet: CompletionResultSet, commentTokenParts: List<String>, nextChildIsClassOrMethodOrFunction: Boolean) {
        // Add DO_NOT_RESOLVE completion if there is no other text
        if (commentTokenParts.isEmpty()) {
            addCompletionElementsSimple(resultSet, listOf(
                    ObjJCommentEvaluatorUtil.DO_NOT_RESOLVE
            ))
        }
        // Add @var and @ignore keywords to completion, if first character is @
        if (commentTokenParts.firstOrNull { it.startsWith("@") } != null) {
            resultSet.addElement(LookupElementBuilder.create("ignore").withPresentableText("@ignore").withInsertHandler { insertionContext: InsertionContext, _: LookupElement ->
                insertionContext.document.insertString(insertionContext.selectionEndOffset, " ")
            })
            resultSet.addElement(LookupElementBuilder.create("var").withPresentableText("@var").withInsertHandler { insertionContext: InsertionContext, _: LookupElement ->
                insertionContext.document.insertString(insertionContext.selectionEndOffset, " ")
            })
            if (nextChildIsClassOrMethodOrFunction) {
                resultSet.addElement(LookupElementBuilder.create("param").withPresentableText("@param").withInsertHandler { insertionContext: InsertionContext, _: LookupElement ->
                    insertionContext.document.insertString(insertionContext.selectionEndOffset, " ")
                })
            }
        }
    }


    private fun getParameterNames(element: PsiElement?): List<String> {
        return when (element) {
            is ObjJFunctionDeclarationElement<*> -> element.parameterNames
            is ObjJMethodHeader -> element.methodDeclarationSelectorList.mapNotNull { it.variableName?.text }
            is ObjJMethodDeclaration -> element.methodHeader.methodDeclarationSelectorList.mapNotNull { it.variableName?.text }
            is ObjJBodyVariableAssignment -> element.variableDeclarationList?.variableDeclarationList?.mapNotNull {
                it.expr?.leftExpr?.functionDeclaration as? ObjJFunctionDeclarationElement<*>
                        ?: it.expr?.leftExpr?.functionLiteral  as? ObjJFunctionDeclarationElement<*>
            }?.flatMap { it.parameterNames }.orEmpty()
            is ObjJVariableDeclaration -> (element.expr?.leftExpr?.functionDeclaration as? ObjJFunctionDeclarationElement<*>
                    ?: element.expr?.leftExpr?.functionLiteral  as? ObjJFunctionDeclarationElement<*>)
                    ?.parameterNames.orEmpty()
            else -> {
                LOGGER.severe("Unexpected element type <${element.elementType}> encountered for get Parameter names")
                emptyList()
            }
        }
    }

    private fun isPrevSiblingClassName(prevSiblingText: String?): Boolean {
        return prevSiblingText != null && prevSiblingText == "|"
    }

}