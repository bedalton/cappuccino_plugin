package org.cappuccino_project.ide.intellij.plugin.contributor.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import org.cappuccino_project.ide.intellij.plugin.utils.EditorUtil
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils

class ObjJSelectorInsertHandler private constructor() : InsertHandler<LookupElement> {

    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        insertColon(insertionContext, lookupElement)
    }

    private fun insertColon(insertionContext: InsertionContext, lookupElement: LookupElement) {
        if (!EditorUtil.isTextAtOffset(insertionContext, ObjJMethodPsiUtils.SELECTOR_SYMBOL)) {
            EditorUtil.insertText(insertionContext, ObjJMethodPsiUtils.SELECTOR_SYMBOL, true)
        }
    }

    companion object {

        val instance = ObjJSelectorInsertHandler()
    }

}
