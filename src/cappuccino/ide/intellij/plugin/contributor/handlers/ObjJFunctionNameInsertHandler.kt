package cappuccino.ide.intellij.plugin.contributor.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import cappuccino.ide.intellij.plugin.utils.EditorUtil

/**
 * Handler for completion insertion of function names
 */
object ObjJFunctionNameInsertHandler : InsertHandler<LookupElement> {

    /**
     * Actually handle the insertion
     */
    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        if (!EditorUtil.isTextAtOffset(insertionContext, "(")) {
            EditorUtil.insertText(insertionContext, "()", true)
            EditorUtil.offsetCaret(insertionContext, -1)
        }
    }
}
