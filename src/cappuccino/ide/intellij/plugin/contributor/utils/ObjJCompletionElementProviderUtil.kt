package cappuccino.ide.intellij.plugin.contributor.utils

import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor
import cappuccino.ide.intellij.plugin.contributor.ObjJInsertionTracker
import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJVariableInsertHandler
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressIndicatorProvider

/**
 * Provider for completion elements
 */
object ObjJCompletionElementProviderUtil {


    /**
     * Creates completion elements using strings instead of elements
     */
    fun addCompletionElementsSimple(resultSet: CompletionResultSet, completionOptions: List<String>) {
        for (completionOption in completionOptions) {
            ProgressIndicatorProvider.checkCanceled()
            val priority = ObjJInsertionTracker.getPoints(completionOption, ObjJCompletionContributor.GENERIC_VARIABLE_SUGGESTION_PRIORITY)
            val prioritizedLookupElement = PrioritizedLookupElement.withPriority(LookupElementBuilder.create(completionOption).withInsertHandler(ObjJVariableInsertHandler), priority)
            resultSet.addElement(prioritizedLookupElement)
        }
    }
    /**
     * Creates completion elements using strings instead of elements
     */
    fun addCompletionElementsSimple(resultSet: CompletionResultSet, completionOptions: List<String>, priority:Double) {
        for (completionOption in completionOptions) {
            ProgressIndicatorProvider.checkCanceled()
            val lookupElement = LookupElementBuilder.create(completionOption).withInsertHandler(ObjJVariableInsertHandler)
            resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, ObjJInsertionTracker.getPoints(completionOption, priority)))
        }
    }
}