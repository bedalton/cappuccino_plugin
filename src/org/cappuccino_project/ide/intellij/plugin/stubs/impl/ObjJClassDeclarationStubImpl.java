package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ObjJClassDeclarationStubImpl<PsiT extends ObjJClassDeclarationElement<? extends ObjJClassDeclarationStub>> extends ObjJStubBaseImpl<PsiT> implements ObjJClassDeclarationStub<PsiT> {
    private final String className;
    private final List<String> inheritedProtocols;

    ObjJClassDeclarationStubImpl(
            @NotNull
                    StubElement parent,
            @NotNull
                    IStubElementType elementType,
            @NotNull
            final String className,
            @NotNull
            final List<String> inheritedProtocols) {
        super(parent, elementType);
        this.className = className;
        this.inheritedProtocols = inheritedProtocols;
    }

    @NotNull
    @Override
    public String getClassName() {
        return this.className;
    }

    @NotNull
    @Override
    public List<String> getInheritedProtocols() {
        return this.inheritedProtocols;
    }
}
