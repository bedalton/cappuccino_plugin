package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces;

import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJAccessorPropertyImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface ObjJAccessorPropertyStub extends ObjJMethodHeaderDeclarationStub<ObjJAccessorPropertyImpl> {
    @NotNull
    String getContainingClass();
    @Nullable
    String getVariableName();
    @Nullable
    String getGetter();
    @Nullable
    String getSetter();
    @Nullable
    String getVarType();
}
