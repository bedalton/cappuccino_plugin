package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJFunctionDeclarationImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ObjJFunctionDeclarationStubImpl extends ObjJFunctionDeclarationElementStubImpl<ObjJFunctionDeclarationImpl> implements ObjJFunctionDeclarationElementStub<ObjJFunctionDeclarationImpl> {
    public ObjJFunctionDeclarationStubImpl(StubElement parent,
                                           @NotNull
                                                   String fileName,
                                           @NotNull
                                                   String fqName,
                                           @NotNull
                                                   List<String> paramNames,
                                           @Nullable
                                                   String returnType,
                                           final boolean shouldResolve) {
        super(parent, ObjJStubTypes.FUNCTION_DECLARATION, fileName, fqName, paramNames, returnType, shouldResolve);
    }
}
