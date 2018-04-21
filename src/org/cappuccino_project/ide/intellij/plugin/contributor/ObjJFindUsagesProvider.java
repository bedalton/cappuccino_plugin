package org.cappuccino_project.ide.intellij.plugin.contributor;

import com.intellij.lang.HelpID;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import org.cappuccino_project.ide.intellij.plugin.lexer.ObjJLexer;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJHasContainingClassPsiUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJFindUsagesProvider implements FindUsagesProvider {


    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(
                new ObjJLexer(),
                TokenSet.create(ObjJTypes.ObjJ_SELECTOR, ObjJTypes.ObjJ_VARIABLE_NAME, ObjJTypes.ObjJ_CLASS_NAME, ObjJTypes.ObjJ_FUNCTION_NAME),
                TokenSet.create(ObjJTypes.ObjJ_SINGLE_LINE_COMMENT, ObjJTypes.ObjJ_BLOCK_COMMENT),
                TokenSet.create(ObjJTypes.ObjJ_INTEGER, ObjJTypes.ObjJ_STRING_LITERAL, ObjJTypes.ObjJ_DECIMAL_LITERAL, ObjJTypes.ObjJ_BOOLEAN_LITERAL)
        );
    }

    @Override
    public boolean canFindUsagesFor(
            @NotNull
                    PsiElement psiElement) {
        return  psiElement instanceof ObjJSelector ||
                psiElement instanceof ObjJVariableName ||
                psiElement instanceof ObjJClassName ||
                psiElement instanceof ObjJFunctionName;
    }

    @Nullable
    @Override
    public String getHelpId(
            @NotNull
                    PsiElement psiElement) {
        return HelpID.FIND_OTHER_USAGES;
    }

    @NotNull
    @Override
    public String getType(
            @NotNull
                    PsiElement psiElement) {
        if (psiElement instanceof ObjJSelector) {
            return "method Selector";
        } else if (psiElement instanceof ObjJVariableName) {
            return "variable";
        } else if (psiElement instanceof ObjJClassName) {
            return "class";
        } else if (psiElement instanceof ObjJFunctionName) {
            return "function";
        }
        return "";
    }

    @NotNull
    @Override
    public String getDescriptiveName(
            @NotNull
                    PsiElement psiElement) {
        final String containingClassOrFileName = ObjJHasContainingClassPsiUtil.getContainingClassOrFileName(psiElement);
        return ObjJPsiImplUtil.getDescriptiveText(psiElement) + " in " + containingClassOrFileName;
    }

    @NotNull
    @Override
    public String getNodeText(
            @NotNull
                    PsiElement psiElement, boolean b) {
        return ObjJPsiImplUtil.getDescriptiveText(psiElement);
    }
}
