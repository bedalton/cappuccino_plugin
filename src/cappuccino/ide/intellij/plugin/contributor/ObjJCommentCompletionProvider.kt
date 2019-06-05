package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.utils.ObjJCompletionElementProviderUtil.addCompletionElementsSimple
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameAggregatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJIgnoreEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.utils.trimFromBeginning
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import java.util.logging.Logger

/**
 * Adds completion results to comment elements
 */
object ObjJCommentCompletionProvider {

    private val stripFromCommentTokenBeginning = listOf("*/", "*", "//", " ", "/**", "/*")

    private val LOGGER:Logger by lazy {
        Logger.getLogger("#${ObjJCommentCompletionProvider::class.java.canonicalName}")
    }

    /**
     * Comments completion result set entry point
     */
    fun addCommentCompletions(resultSet: CompletionResultSet, element: PsiElement) {
        val text = element.text?.substringBefore(ObjJBlanketCompletionProvider.CARET_INDICATOR, "") ?: return
        // Divide text by line, and add completion results for it
        for (commentLine in text.split("\\n".toRegex())) {
            addCommentCompletionsForLine(resultSet, element, text)
        }
    }

    /**
     * Adds comment completions for a give line
     */
    private fun addCommentCompletionsForLine(resultSet: CompletionResultSet, element: PsiElement, textIn:String) {
        val text = textIn.trim()
        if (!text.contains("@v") && !text.contains("@i")) {
            return
        }
        // Divide comment line into tokens
        val commentTokenParts:List<String> = splitCommentIntoTokenParts(text)

        // Add completion results for a given statement type
        when {
            // @var completions
            text.contains("@var") ->
                appendVarCompletions(resultSet, element, commentTokenParts)
            // @Ignore completions
            text.contains("@ignore") ->
                getIgnoreCompletions(resultSet, commentTokenParts)
            // Default completions
            else -> addDefaults(resultSet, commentTokenParts)
        }
    }

    /**
     * Splits comment line into tokens, trimming and stripping spaces and comment characters
     */
    private fun splitCommentIntoTokenParts(text:String) : List<String> {
        return text.split("\\s+".toRegex())
                // Remove comment characters, and whitespaces after comment characters
                .map { it.trim().trimFromBeginning(stripFromCommentTokenBeginning) }
                // Filter out empty strings
                .filterNot { it.isEmpty() }
    }

    /**
     * Append @var completion items
     */
    private fun appendVarCompletions(resultSet:CompletionResultSet, element: PsiElement, commentTokenParts: List<String>) {
        var afterVar = false
        var indexAfter = -1
        var currentIndex = 0
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
            val place = commentTokenParts.size - indexAfter
            when (place) {
                // Add class names if first token after @var
                0,1 -> {
                    ObjJClassNamesCompletionProvider.getClassNameCompletions(resultSet, element)
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
     * Add @ignore completion parameters
     */
    private fun getIgnoreCompletions(resultSet:CompletionResultSet, commentTokenParts:List<String>) {
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
    private fun addDefaults(resultSet:CompletionResultSet, commentTokenParts:List<String>) {
        // Add DO_NOT_RESOLVE completion if there is no other text
        if (commentTokenParts.isEmpty()) {
            addCompletionElementsSimple(resultSet, listOf(
                    ObjJIgnoreEvaluatorUtil.DO_NOT_RESOLVE
            ))
        }
        // Add @var and @ignore keywords to completion, if first character is @
        if (commentTokenParts.firstOrNull { it.startsWith("@")} != null) {
            resultSet.addElement(LookupElementBuilder.create("ignore").withPresentableText("@ignore").withInsertHandler { insertionContext: InsertionContext, _: LookupElement ->
                insertionContext.document.insertString(insertionContext.selectionEndOffset, " ")
            })
            resultSet.addElement(LookupElementBuilder.create("var").withPresentableText("@var").withInsertHandler { insertionContext: InsertionContext, _: LookupElement ->
                insertionContext.document.insertString(insertionContext.selectionEndOffset, " ")
            })
        }
    }

}