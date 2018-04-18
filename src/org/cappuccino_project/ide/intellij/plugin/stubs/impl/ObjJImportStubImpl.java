package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJImportStatement;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJImportStubImpl<PsiT extends ObjJImportStatement<? extends ObjJImportStub<PsiT>>> extends ObjJStubBaseImpl<PsiT> implements ObjJImportStub<PsiT> {

    private final String fileName;
    private final String frameworkName;

    public ObjJImportStubImpl(StubElement parent, IStubElementType elementType, @Nullable String frameworkName, @NotNull String fileName) {
        super(parent, elementType);
        this.frameworkName = frameworkName;
        this.fileName = fileName;
    }

    @NotNull
    @Override
    public String getFileName() {
        return fileName;
    }

    @Nullable
    @Override
    public String getFramework() {
        return frameworkName;
    }
}
