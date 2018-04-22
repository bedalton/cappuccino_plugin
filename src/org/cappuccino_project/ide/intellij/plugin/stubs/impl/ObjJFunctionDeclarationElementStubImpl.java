package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ObjJFunctionDeclarationElementStubImpl<PsiT extends ObjJFunctionDeclarationElement<? extends ObjJFunctionDeclarationElementStub>> extends ObjJStubBaseImpl<PsiT> implements ObjJFunctionDeclarationElementStub<PsiT> {

    private final String fileName;
    private final String fqName;
    private final String functionName;
    private final List<String> paramNames;
    private final String returnType;
    private final boolean shouldResolve;

    public ObjJFunctionDeclarationElementStubImpl(StubElement parent, @NotNull IStubElementType stubElementType, @NotNull String fileName, @NotNull String fqName, @NotNull List<String> paramNames, @Nullable String returnType, final boolean shouldResolve) {
        super(parent, stubElementType);
        this.fileName = fileName;
        this.fqName = fqName;
        final int lastDotIndex = fqName.lastIndexOf(".");
        this.functionName = fqName.substring(lastDotIndex >=0 ? lastDotIndex : 0);
        this.paramNames = paramNames;
        this.returnType = returnType;
        this.shouldResolve = shouldResolve;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getFqName() {
        return fqName;
    }

    @Override
    public String getFunctionName() {
        return functionName;
    }

    @Override
    public int getNumParams() {
        return paramNames.size();
    }

    @NotNull
    @Override
    public List<String> getParamNames() {
        return paramNames;
    }

    @Override
    public String getReturnType() {
        return returnType;
    }

    @Override
    public boolean shouldResolve() {
        return shouldResolve;
    }
}
