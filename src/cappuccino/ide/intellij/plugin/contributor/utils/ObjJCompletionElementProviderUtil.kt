package cappuccino.ide.intellij.plugin.contributor.utils

import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJVariableInsertHandler
import com.intellij.codeInsight.completion.CompletionResultSet
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
            resultSet.addElement(LookupElementBuilder.create(completionOption).withInsertHandler(ObjJVariableInsertHandler))
        }
    }
}