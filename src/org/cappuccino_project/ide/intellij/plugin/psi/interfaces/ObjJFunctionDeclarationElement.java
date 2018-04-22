package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJBlock;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJFormalParameterArg;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJLastFormalParameterArg;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ObjJFunctionDeclarationElement<StubT extends ObjJFunctionDeclarationElementStub<? extends ObjJFunctionDeclarationElement>> extends ObjJHasFunctionName, ObjJStubBasedElement<StubT>, ObjJCompositeElement, ObjJResolveableElement<StubT> {
    @NotNull
    List<String> getParamNames();
    @Nullable
    String getReturnType();

    @NotNull
    List<ObjJFormalParameterArg> getFormalParameterArgList();

    @Nullable
    ObjJLastFormalParameterArg getLastFormalParameterArg();

    @Nullable
    ObjJBlock getBlock();
}
