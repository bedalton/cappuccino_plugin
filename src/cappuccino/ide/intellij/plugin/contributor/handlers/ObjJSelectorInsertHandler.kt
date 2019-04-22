package cappuccino.ide.intellij.plugin.contributor.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils

/**
 * Handles completion insertion of selectors
 */
class ObjJSelectorInsertHandler(private val insertSpace:Boolean) : InsertHandler<LookupElement> {

    /**
     * Handle insertion entry point
     */
    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        insertColon(insertionContext, lookupElement)
        if (insertSpace)
            insertSpaceBefore(insertionContext) // must be after colon is inserted, otherwise the colon would be inserted in the wrong spot
    }

    /**
     * Insert color if necessary
     */
    private fun insertColon(insertionContext: InsertionContext, @Suppress("UNUSED_PARAMETER") lookupElement: LookupElement) {
        if (!EditorUtil.isTextAtOffset(insertionContext, ObjJMethodPsiUtils.SELECTOR_SYMBOL)) {
            EditorUtil.insertText(insertionContext, ObjJMethodPsiUtils.SELECTOR_SYMBOL, true)
        }
    }

    /**
     * Insert space if necessary
     */
    private fun insertSpaceBefore(insertionContext: InsertionContext) {
        if (!EditorUtil.isTextAtOffset(insertionContext.document, insertionContext.startOffset-1, " ")) {
            EditorUtil.insertText(insertionContext.editor, " ", insertionContext.startOffset, false)
        }
    }
}