package org.cappuccino_project.ide.intellij.plugin.contributor.handlers;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import org.cappuccino_project.ide.intellij.plugin.utils.EditorUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils;

public class ObjJSelectorInsertHandler implements InsertHandler<LookupElement> {

    private static final ObjJSelectorInsertHandler INSTANCE = new ObjJSelectorInsertHandler();

    private ObjJSelectorInsertHandler() {
    }

    @Override
    public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
        insertColon(insertionContext, lookupElement);
    }

    private void insertColon(InsertionContext insertionContext, @SuppressWarnings("unused")
            LookupElement lookupElement) {
        if (!EditorUtil.isTextAtOffset(insertionContext, ObjJMethodPsiUtils.SELECTOR_SYMBOL)) {
            EditorUtil.insertText(insertionContext, ObjJMethodPsiUtils.SELECTOR_SYMBOL, true);
        }
    }

    public static ObjJSelectorInsertHandler getInstance() {
        return INSTANCE;
    }

}
