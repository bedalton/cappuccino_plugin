package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJGlobalVariableDeclarationImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJGlobalVariableDeclarationStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.codehaus.groovy.ast.ASTNode;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ObjJGlobalVariableDeclarationStubImpl extends ObjJStubBaseImpl<ObjJGlobalVariableDeclarationImpl> implements ObjJGlobalVariableDeclarationStub {

    private final String fileName;
    private final String variableName;
    private final String variableType;

    public ObjJGlobalVariableDeclarationStubImpl(StubElement parent, @Nullable String fileName, @NotNull String variableName, @Nullable String variableType) {
        super(parent, ObjJStubTypes.GLOBAL_VARIABLE);
        this.variableName = variableName;
        this.variableType = variableType;
        this.fileName = fileName;
    }

    @NotNull
    @Override
    public String getVariableName() {
        return variableName;
    }

    @Nullable
    @Override
    public String getVariableType() {
        return variableType;
    }

    @Nullable
    @Override
    public String getFileName() {
        return fileName;
    }
}
