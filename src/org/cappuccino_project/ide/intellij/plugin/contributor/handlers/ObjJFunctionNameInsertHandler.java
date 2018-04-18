package org.cappuccino_project.ide.intellij.plugin.contributor.handlers;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import org.cappuccino_project.ide.intellij.plugin.utils.EditorUtil;

public class ObjJFunctionNameInsertHandler implements InsertHandler<LookupElement> {
    private static final ObjJFunctionNameInsertHandler INSTANCE = new ObjJFunctionNameInsertHandler();
    @Override
    public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
        if (!EditorUtil.isTextAtOffset(insertionContext, "(")) {
            EditorUtil.insertText(insertionContext,"()", true);
            EditorUtil.offsetCaret(insertionContext, -1);
        }
    }


    public static ObjJFunctionNameInsertHandler getInstance() {
        return INSTANCE;
    }
}
