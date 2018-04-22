package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJFunctionDeclarationImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJFunctionDeclarationStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ObjJFunctionDeclarationStubType extends ObjJAbstractFunctionDeclarationStubType<ObjJFunctionDeclarationImpl, ObjJFunctionDeclarationStubImpl> {

    ObjJFunctionDeclarationStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJFunctionDeclarationImpl.class, ObjJFunctionDeclarationStubImpl.class);
    }

    @Override
    public ObjJFunctionDeclarationImpl createPsi(
            @NotNull
                    ObjJFunctionDeclarationElementStub<ObjJFunctionDeclarationImpl> stub) {
        return new ObjJFunctionDeclarationImpl(stub, this);
    }

    @NotNull
    @Override
    ObjJFunctionDeclarationElementStub<ObjJFunctionDeclarationImpl> createStub(StubElement parent,
                                                                               @NotNull
                                                                                       String fileName,
                                                                               @NotNull
                                                                                       String fqName,
                                                                               @NotNull
                                                                                       List<String> paramNames,
                                                                               @Nullable
                                                                                       String returnType,
                                                                               final boolean shouldResolve) {
        return new ObjJFunctionDeclarationStubImpl(parent, fileName, fqName, paramNames, returnType, shouldResolve);
    }
}
