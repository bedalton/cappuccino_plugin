package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJFunctionLiteralImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ObjJFunctionLiteralStubImpl extends ObjJFunctionDeclarationElementStubImpl<ObjJFunctionLiteralImpl> implements ObjJFunctionDeclarationElementStub<ObjJFunctionLiteralImpl> {
    public ObjJFunctionLiteralStubImpl(StubElement parent,
                                       @NotNull
                                                   String fileName,
                                       @NotNull
                                                   String fqName,
                                       @NotNull
                                                   List<String> paramNames,
                                       @Nullable
                                                   String returnType) {
        super(parent, ObjJStubTypes.FUNCTION_LITERAL,  fileName, fqName, paramNames, returnType);
    }
}
