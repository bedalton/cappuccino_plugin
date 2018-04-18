package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJInstanceVariableDeclarationImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface ObjJInstanceVariableDeclarationStub extends StubElement<ObjJInstanceVariableDeclarationImpl> {
    @NotNull
    String getContainingClass();
    @NotNull
    String getVarType();
    @NotNull
    String getVariableName();
    @Nullable
    String getGetter();
    @Nullable
    String getSetter();
}
