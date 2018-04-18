package org.cappuccino_project.ide.intellij.plugin.references;

import com.intellij.psi.search.PsiElementProcessor;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.jetbrains.annotations.NotNull;

public class ObjJClassDeclarationProcessor implements PsiElementProcessor<ObjJClassDeclarationElement> {
    @Override
    public boolean execute(
            @NotNull
                    ObjJClassDeclarationElement classDeclarationElement) {
        return false;
    }
}
