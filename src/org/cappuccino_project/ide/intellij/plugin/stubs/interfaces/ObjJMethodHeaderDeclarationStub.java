package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces;


import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ObjJMethodHeaderDeclarationStub<PsiT extends ObjJMethodHeaderDeclaration<? extends ObjJMethodHeaderDeclarationStub>> extends StubElement<PsiT> {
    @NotNull
    List<String> getParamTypes();

    @NotNull
    List<String> getSelectorStrings();

    @NotNull
    String getSelectorString();

    @NotNull
    String getContainingClassName();

    boolean isRequired();

    @NotNull
    ObjJClassType getReturnType();

    @NotNull
    String getReturnTypeAsString();

    boolean isStatic();

}
