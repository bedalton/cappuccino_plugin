package cappuccino.ide.intellij.plugin.contributor.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils

class ObjJSelectorInsertHandler private constructor() : InsertHandler<LookupElement> {

    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        insertColon(insertionContext, lookupElement)
        insertSpaceBefore(insertionContext) // must be after colon is inserted, otherwise the colon would be inserted in the wrong spot
    }

    private fun insertColon(insertionContext: InsertionContext, lookupElement: LookupElement) {
        if (!EditorUtil.isTextAtOffset(insertionContext, ObjJMethodPsiUtils.SELECTOR_SYMBOL)) {
            EditorUtil.insertText(insertionContext, ObjJMethodPsiUtils.SELECTOR_SYMBOL, true)
        }
    }

    private fun insertSpaceBefore(insertionContext: InsertionContext) {
        if (!EditorUtil.isTextAtOffset(insertionContext.document, insertionContext.startOffset-1, " ")) {
            EditorUtil.insertText(insertionContext.editor, " ", insertionContext.startOffset, false)
        }
    }

    companion object {

        val instance = ObjJSelectorInsertHandler()
    }

}