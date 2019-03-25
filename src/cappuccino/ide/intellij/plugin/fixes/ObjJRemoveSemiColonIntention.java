package cappuccino.ide.intellij.plugin.fixes;

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider;
import cappuccino.ide.intellij.plugin.lang.ObjJBundle;
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNeedsSemiColon;
import cappuccino.ide.intellij.plugin.utils.EditorUtil;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.ObjJ_SEMI_COLON;

public class ObjJRemoveSemiColonIntention extends BaseIntentionAction {

    private final SmartPsiElementPointer<PsiElement> elementPointer;

    public ObjJRemoveSemiColonIntention(PsiElement element) {
        element = getSemiColonElementOrDefault(element);
        elementPointer = SmartPointerManager.createPointer(element);
    }

    @NotNull
    @Override
    public String getText() {
        return ObjJBundle.message("objective-j.intentions.remove-extraneous-semi-colon.text");
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME;
    }

    @Override
    public boolean isAvailable(
            @NotNull
                    Project project, Editor editor, PsiFile psiFile) {
        PsiElement element = this.elementPointer.getElement();
        return isSemiColon(element);
    }

    @Override
    public void invoke(
            @NotNull
                    Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        final PsiElement element = this.elementPointer.getElement();
        if (!isSemiColon(element))
            return;
        EditorUtil.INSTANCE.runWriteAction(()-> element.getParent().getNode().removeChild(element.getNode()), project);
    }

    private PsiElement getSemiColonElementOrDefault(PsiElement element) {
        if (isSemiColon(element))
            return element;
        PsiElement temp = element.getLastChild();
        if (isSemiColon(temp))
            return temp;
        return element;
    }

    private boolean isSemiColon(@Nullable PsiElement element) {
        if (element == null)
            return false;
        if (element.getNode() == null)
            return false;
        return element.getNode().getElementType() == ObjJ_SEMI_COLON;
    }
}
