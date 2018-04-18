package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJGlobalVariableDeclarationImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface ObjJGlobalVariableDeclarationStub extends StubElement<ObjJGlobalVariableDeclarationImpl> {

    @Nullable
    String getFileName();
    @NotNull
    String getVariableName();
    @Nullable
    String getVariableType();

}
