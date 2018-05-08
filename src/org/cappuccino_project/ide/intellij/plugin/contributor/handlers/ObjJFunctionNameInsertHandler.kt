package org.cappuccino_project.ide.intellij.plugin.contributor.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import org.cappuccino_project.ide.intellij.plugin.utils.EditorUtil

class ObjJFunctionNameInsertHandler : InsertHandler<LookupElement> {
    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        if (!EditorUtil.isTextAtOffset(insertionContext, "(")) {
            EditorUtil.insertText(insertionContext, "()", true)
            EditorUtil.offsetCaret(insertionContext, -1)
        }
    }

    companion object {
        val instance = ObjJFunctionNameInsertHandler()
    }
}
