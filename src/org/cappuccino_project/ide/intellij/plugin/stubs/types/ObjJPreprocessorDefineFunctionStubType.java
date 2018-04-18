package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJPreprocessorDefineFunctionImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJPreprocessorFunctionDeclarationStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ObjJPreprocessorDefineFunctionStubType extends ObjJAbstractFunctionDeclarationStubType<ObjJPreprocessorDefineFunctionImpl, ObjJPreprocessorFunctionDeclarationStubImpl> {

    ObjJPreprocessorDefineFunctionStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJPreprocessorDefineFunctionImpl.class, ObjJPreprocessorFunctionDeclarationStubImpl.class);
    }

    @Override
    public ObjJPreprocessorDefineFunctionImpl createPsi(
            @NotNull
                    ObjJFunctionDeclarationElementStub<ObjJPreprocessorDefineFunctionImpl> stub) {
        return new ObjJPreprocessorDefineFunctionImpl(stub, ObjJStubTypes.PREPROCESSOR_FUNCTION);
    }

    @NotNull
    @Override
    ObjJFunctionDeclarationElementStub<ObjJPreprocessorDefineFunctionImpl> createStub(StubElement parent,
                                                                                 @NotNull
                                                                                         String fileName,
                                                                                 @NotNull
                                                                                         String fqName,
                                                                                 @NotNull
                                                                                         List<String> paramNames,
                                                                                 @Nullable
                                                                                         String returnType) {
        return new ObjJPreprocessorFunctionDeclarationStubImpl(parent, fileName, fqName, paramNames, returnType);
    }
}
