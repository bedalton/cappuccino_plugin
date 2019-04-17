package cappuccino.ide.intellij.plugin.psi.interfaces;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface ObjJHasBraces extends PsiElement{

    @Nullable
    PsiElement getOpenBrace();

    @Nullable
    PsiElement getCloseBrace();

}
