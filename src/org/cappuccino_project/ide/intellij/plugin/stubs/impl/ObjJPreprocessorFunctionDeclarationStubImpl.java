package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJFunctionLiteralImpl;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJPreprocessorDefineFunctionImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ObjJPreprocessorFunctionDeclarationStubImpl extends ObjJFunctionDeclarationElementStubImpl<ObjJPreprocessorDefineFunctionImpl> implements ObjJFunctionDeclarationElementStub<ObjJPreprocessorDefineFunctionImpl> {
    public ObjJPreprocessorFunctionDeclarationStubImpl(StubElement parent,
                                                  @NotNull
                                                   String fileName,
                                                  @NotNull
                                                   String fqName,
                                                  @NotNull
                                                   List<String> paramNames,
                                                  @Nullable
                                                   String returnType) {
        super(parent, ObjJStubTypes.PREPROCESSOR_FUNCTION, fileName, fqName, paramNames, returnType);
    }
}
