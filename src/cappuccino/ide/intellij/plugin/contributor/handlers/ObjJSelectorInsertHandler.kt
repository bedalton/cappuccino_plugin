package cappuccino.ide.intellij.plugin.contributor.handlers

import cappuccino.ide.intellij.plugin.contributor.ObjJInsertionTracker
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement

/**
 * Handles completion insertion of selectors
 */
class ObjJSelectorInsertHandler(private val insertSpace:Boolean) : InsertHandler<LookupElement> {

    /**
     * Handle insertion entry point
     */
    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        ObjJInsertionTracker.hit(lookupElement.lookupString)
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