package cappuccino.ide.intellij.plugin.formatting;

import cappuccino.ide.intellij.plugin.lang.ObjJFile;
import com.intellij.formatting.FormattingMode;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
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

    @SuppressWarnings("SameParameterValue")
    @NotNull
    private FormattingModel createModel(
            @NotNull
                    PsiElement element,
            @NotNull
                    CodeStyleSettings settings,
            @NotNull
                    FormattingMode mode) {
        // element can be DartFile, DartEmbeddedContent, DartExpressionCodeFragment
        final PsiFile psiFile = element.getContainingFile();
        if (!(psiFile instanceof ObjJFile))
            return null;
        final ASTNode rootNode = element instanceof ObjJFile ? psiFile.getNode() : element.getNode();
        final ObjJBlockContext context = new ObjJBlockContext(settings, mode);
        final ObjJFormattedBlock rootBlock = new ObjJFormattedBlock(rootNode, null, null, settings, context);
        return new DocumentBasedFormattingModel(rootBlock, element.getProject(), settings, psiFile.getFileType(), psiFile);
    }

    @Nullable
    @Override
    public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
        return null;
    }
}