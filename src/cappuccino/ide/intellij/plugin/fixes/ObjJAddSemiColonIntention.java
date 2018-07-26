package cappuccino.ide.intellij.plugin.fixes;

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider;
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNeedsSemiColon;
import cappuccino.ide.intellij.plugin.utils.EditorUtil;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ObjJAddSemiColonIntention extends BaseIntentionAction {
    private final ObjJNeedsSemiColon element;

    public ObjJAddSemiColonIntention(final ObjJNeedsSemiColon element) {
        this.element = element;
    }

    @NotNull
    @Override
    public String getText() {
        return "Insert missing semi-colon";
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
        return !this.element.getLastChild().getText().equals(";");
    }

    @Override
    public void invoke(
            @NotNull
                    Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        EditorUtil.INSTANCE.insertText(editor, ";", element.getTextRange().getEndOffset(), true);
    }
}
