package cappuccino.ide.intellij.plugin.formatting;

import cappuccino.ide.intellij.plugin.lang.ObjJFile;
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage;
import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.DocumentBasedFormattingModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJFormattingModelBuilder implements FormattingModelBuilder {
    @NotNull
    @Override
    public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
        return createModel(element, settings, FormattingMode.REFORMAT);
    }

    @NotNull
    public FormattingModel createModel(@NotNull PsiElement element, @NotNull CodeStyleSettings settings, @NotNull FormattingMode mode) {
        // element can be DartFile, DartEmbeddedContent, DartExpressionCodeFragment
        final PsiFile psiFile = element.getContainingFile();
        final ASTNode rootNode = psiFile instanceof ObjJFile ? psiFile.getNode() : element.getNode();
        final ObjJBlockContext context = new ObjJBlockContext(settings, mode);
        final ObjJFormattedBlock rootBlock = new ObjJFormattedBlock(rootNode, null, null, settings, context);
        return new DocumentBasedFormattingModel(rootBlock, element.getProject(), settings, psiFile.getFileType(), psiFile);
    }

    @Nullable
    @Override
    public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
        return null;
    }

    private static SpacingBuilder createSpaceBuilder(CodeStyleSettings settings) {
        final SpacingBuilder sb = new SpacingBuilder(settings, ObjJLanguage.getInstance());
        return sb;
    }
}