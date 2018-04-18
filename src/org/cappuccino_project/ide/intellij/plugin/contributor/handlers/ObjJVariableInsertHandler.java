package org.cappuccino_project.ide.intellij.plugin.contributor.handlers;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJFunctionsIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJExpr;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJFunctionCall;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodCall;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.cappuccino_project.ide.intellij.plugin.utils.EditorUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil;

import javax.annotation.Nullable;

public class ObjJVariableInsertHandler implements InsertHandler<LookupElement> {

    private static ObjJVariableInsertHandler INSTANCE = new ObjJVariableInsertHandler();

    private ObjJVariableInsertHandler() {}

    @Override
    public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
        handleInsert(lookupElement.getPsiElement(), insertionContext.getEditor());

    }

    private void handleInsert(
            @Nullable
                    PsiElement element, Editor editor) {
        if (element == null) {
            return;
        }
        if (isFunctionCompletion(element)) {
            EditorUtil.insertText(editor,"()", false);
            EditorUtil.offsetCaret(editor, 1);
        }
        if (shouldAppendFunctionParamComma(element)) {
            EditorUtil.insertText(editor, ", ", true);
        }
        if (shouldAppendClosingBracket(element)) {
            EditorUtil.insertText(editor, "]", false);
        }
    }

    public boolean shouldAppendFunctionParamComma(PsiElement element) {
        ObjJExpr parentExpression = ObjJTreeUtil.getParentOfType(element, ObjJExpr.class);
        if (parentExpression == null) {
            return false;
        }
        ASTNode nextNonEmptyNode = ObjJTreeUtil.getNextNonEmptyNode(parentExpression, true);
        return parentExpression.getParent() instanceof ObjJFunctionCall && (nextNonEmptyNode == null || nextNonEmptyNode.getElementType() != ObjJTypes.ObjJ_COMMA);
    }

    public boolean shouldAppendClosingBracket(@Nullable PsiElement element) {
        ObjJExpr parentExpression = ObjJTreeUtil.getParentOfType(element, ObjJExpr.class);
        if (parentExpression == null) {
            return false;
        }
        ObjJMethodCall methodCall = ObjJTreeUtil.getParentOfType(parentExpression, ObjJMethodCall.class);
        return methodCall != null && methodCall.getCloseBracket() == null;
    }

    public boolean isFunctionCompletion(PsiElement element) {
        return !DumbService.isDumb(element.getProject()) && ObjJVariableNameUtil.getPrecedingVariableAssignmentNameElements(element, 0).isEmpty() && !ObjJFunctionsIndex.getInstance().get(element.getText(), element.getProject()).isEmpty();
    }


    public static ObjJVariableInsertHandler getInstance() {
        return INSTANCE;
    }
}
