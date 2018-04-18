package org.cappuccino_project.ide.intellij.plugin.fixes;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodHeader;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJProtocolDeclarationPsiUtil.ProtocolMethods;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ObjJMissingProtocolMethodFix extends BaseIntentionAction {

    private final ProtocolMethods methodHeaders;

    public ObjJMissingProtocolMethodFix(ProtocolMethods methodHeaders) {
        this.methodHeaders = methodHeaders;
    }

    @NotNull
    @Override
    public String getText() {
        return "Implement missing protocol methodHeaders";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Protocol methodHeaders";
    }

    @Override
    public boolean isAvailable(
            @NotNull
                    Project project, Editor editor, PsiFile psiFile) {
        return true;
    }

    @Override
    public void invoke(
            @NotNull
                    Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {

    }

    private void addMethods(final Project project, final VirtualFile file, List<ObjJMethodHeader> methodHeaders) {



    }
}
