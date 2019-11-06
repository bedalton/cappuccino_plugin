package cappuccino.ide.intellij.plugin.contributor.handlers

import cappuccino.ide.intellij.plugin.contributor.ObjJInsertionTracker
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement

/**
 * Handler for completion insertion of function names
 */
object ObjJFunctionNameInsertHandler : InsertHandler<LookupElement> {

    /**
     * Actually handle the insertion
     */
    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        ObjJInsertionTracker.hit(lookupElement.lookupString)
        if (!EditorUtil.isTextAtOffset(insertionContext, "(")) {
            EditorUtil.insertText(insertionContext, "()", true)
            EditorUtil.offsetCaret(insertionContext, -1)
        }
    }
}

/**
 * Handler for completion insertion of function names
 */
object ObjJFunctionNameEmptyArgsInsertHandler : InsertHandler<LookupElement> {

    /**
     * Actually handle the insertion
     */
    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        ObjJInsertionTracker.hit(lookupElement.lookupString)
        if (!EditorUtil.isTextAtOffset(insertionContext, "(")) {
            EditorUtil.insertText(insertionContext, "()", true)
        }
    }
}