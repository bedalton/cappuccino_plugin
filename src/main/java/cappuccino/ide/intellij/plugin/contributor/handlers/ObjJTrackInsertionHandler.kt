package cappuccino.ide.intellij.plugin.contributor.handlers

import cappuccino.ide.intellij.plugin.contributor.ObjJInsertionTracker
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement

object ObjJTrackInsertionHandler : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, lookupElement: LookupElement) {
        ObjJInsertionTracker.hit(lookupElement.lookupString)
    }

}