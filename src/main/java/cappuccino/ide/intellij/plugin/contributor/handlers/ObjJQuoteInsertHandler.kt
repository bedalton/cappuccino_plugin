package cappuccino.ide.intellij.plugin.contributor.handlers

import cappuccino.ide.intellij.plugin.contributor.ObjJInsertionTracker
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.util.TextRange

/**
 * Handler for completion insertion of function names
 */
object ObjJQuoteInsertHandler : InsertHandler<LookupElement> {

    /**
     * Actually handle the insertion
     */
    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        ObjJInsertionTracker.hit(lookupElement.lookupString)
        if (EditorUtil.isTextAtOffset(insertionContext, "\"")) {
            EditorUtil.deleteText(insertionContext.document, TextRange.create(insertionContext.selectionEndOffset, insertionContext.selectionEndOffset + 1))
        }
    }

}
