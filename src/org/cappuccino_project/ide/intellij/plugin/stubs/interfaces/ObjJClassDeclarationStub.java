package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ObjJClassDeclarationStub<PsiT extends ObjJClassDeclarationElement<? extends ObjJClassDeclarationStub>> extends StubElement<PsiT>, ObjJResolveableStub<PsiT> {

    @NotNull
    String getClassName();
    @NotNull
    List<String> getInheritedProtocols();

}
