package cappuccino.ide.intellij.plugin.formatting;

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage;
import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJFormattingModelBuilder implements FormattingModelBuilder {
    @NotNull
    @Override
    public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
        final ObjJFormattedBlock fileBlock = new ObjJFormattedBlock(
                element.getNode(),Alignment.createAlignment(), Wrap.createWrap(WrapType.NONE, false), null, createSpaceBuilder(settings), null, null);
        return FormattingModelProvider.createFormattingModelForPsiFile(element.getContainingFile(), fileBlock, settings);
    }

    @Nullable
    @Override
    public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
        return null;
    }

    private static SpacingBuilder createSpaceBuilder(CodeStyleSettings settings) {
        return new SpacingBuilder(settings, ObjJLanguage.getInstance());
    }
}