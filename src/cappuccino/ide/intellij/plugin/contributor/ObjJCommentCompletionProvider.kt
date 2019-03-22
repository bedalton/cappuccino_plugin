package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.utils.ObjJCompletionElementProviderUtil.addCompletionElementsSimple
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameAggregatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJIgnoreEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement

object ObjJCommentCompletionProvider {

    fun addCommentCompletions(resultSet: CompletionResultSet, element: PsiElement) {
        val text = element.text?.substringBefore(ObjJBlanketCompletionProvider.CARET_INDICATOR, "") ?: return
        for (commentLine in text.split("\\n".toRegex())) {
            addCommentCompletionsForLine(resultSet, element, text)
        }

    }

    private fun addCommentCompletionsForLine(resultSet: CompletionResultSet, element: PsiElement, textIn:String) {
        val text = textIn.trim()
        if (!text.startsWith("@")) {
            return
        }
        val commentTokenParts:List<String> = text.split("\\s+".toRegex()).map{ it.trim() }.filterNot { it.isEmpty() || it.contains("*") || it == "//" }

        when {
            text.contains("@var") -> appendVarCompletions(resultSet, element, commentTokenParts)
            text.contains("@ignore") -> getIgnoreCompletions(resultSet, commentTokenParts)
            else -> {
                addCompletionElementsSimple(resultSet, listOf(
                    ObjJIgnoreEvaluatorUtil.DO_NOT_RESOLVE
                ))
                resultSet.addElement(LookupElementBuilder.create("ignore").withPresentableText("@ignore").withInsertHandler { insertionContext: InsertionContext, _: LookupElement ->
                    insertionContext.document.insertString(insertionContext.selectionEndOffset, " ")
                })
                resultSet.addElement(LookupElementBuilder.create("var").withPresentableText("@var").withInsertHandler { insertionContext: InsertionContext, _: LookupElement ->
                    insertionContext.document.insertString(insertionContext.selectionEndOffset, " ")
                })
            }
        }
    }

    private fun appendVarCompletions(resultSet:CompletionResultSet, element: PsiElement, commentTokenParts: List<String>) {
        var afterVar = false
        var indexAfter = -1
        var currentIndex = 0
        for (part in commentTokenParts) {
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
                0,1 -> ObjJBlanketCompletionProvider.getClassNameCompletions(resultSet, element)
                2 -> {
                    val variableNames = ObjJVariableNameAggregatorUtil.getSiblingVariableAssignmentNameElements(element, 0).map {
                        it.text
                    }
                    addCompletionElementsSimple(resultSet, variableNames)
                }
                else -> return
            }
        }
    }

    private fun getIgnoreCompletions(resultSet:CompletionResultSet, commentTokenParts:List<String>) {
        var afterVar = false
        var indexAfter = -1
        var currentIndex = 0
        var precededByComma: Boolean
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
    }

}