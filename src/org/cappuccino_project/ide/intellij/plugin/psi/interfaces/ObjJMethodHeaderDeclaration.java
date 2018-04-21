package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderDeclarationStub;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope;
import org.jetbrains.annotations.NotNull;

public interface ObjJMethodHeaderDeclaration<StubT extends ObjJMethodHeaderDeclarationStub<? extends ObjJMethodHeaderDeclaration>> extends ObjJStubBasedElement<StubT>, ObjJCompositeElement, ObjJHasMethodSelector, ObjJHasContainingClass {

    @NotNull
    String getReturnType();

    @NotNull
    MethodScope getMethodScope();

    default boolean isStatic() {
        return getMethodScope() == MethodScope.STATIC;
    }

    StubT getStub();
}
