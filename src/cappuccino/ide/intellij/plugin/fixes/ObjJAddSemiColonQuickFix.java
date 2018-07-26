package cappuccino.ide.intellij.plugin.fixes;

import cappuccino.ide.intellij.plugin.utils.EditorUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJAddSemiColonQuickFix implements LocalQuickFix {

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Objective-J";
    }

    @Override
    public void applyFix(
            @NotNull
                    Project project,
            @NotNull
                    ProblemDescriptor problemDescriptor) {
        final PsiElement element = problemDescriptor.getPsiElement();
        Document document = getDocument(element);
        Logger.getInstance(ObjJAddSemiColonQuickFix.class).assertTrue(document != null);
        EditorUtil.INSTANCE.runWriteAction(() -> {
            document.insertString(element.getTextRange().getEndOffset(), ";");
        }, project, document);
        apply();
    }

    @Nullable
    private Document getDocument(@NotNull PsiElement psiElement) {
        PsiFile file = psiElement.getContainingFile();
        FileViewProvider viewProvider = file.getViewProvider();
        return viewProvider.getDocument();
    }

    private void apply() {

    }
}
