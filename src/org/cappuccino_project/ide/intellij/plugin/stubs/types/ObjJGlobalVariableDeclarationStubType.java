package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJGlobalVariableDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJGlobalVariableDeclarationImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJGlobalVariableDeclarationStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJGlobalVariableDeclarationStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJInstanceVariableDeclarationStub;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil;
import org.cappuccino_project.ide.intellij.plugin.utils.Strings;
import org.codehaus.groovy.ast.ASTNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ObjJGlobalVariableDeclarationStubType extends ObjJStubElementType<ObjJGlobalVariableDeclarationStub,ObjJGlobalVariableDeclarationImpl> {

    public static final int VERSION = 3;

    ObjJGlobalVariableDeclarationStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJGlobalVariableDeclarationImpl.class, ObjJGlobalVariableDeclarationStub.class);
    }

    @Override
    public ObjJGlobalVariableDeclarationImpl createPsi(
            @NotNull
                    ObjJGlobalVariableDeclarationStub objJGlobalVariableDeclarationStub) {
        return new ObjJGlobalVariableDeclarationImpl(objJGlobalVariableDeclarationStub, this);
    }

    @NotNull
    @Override
    public ObjJGlobalVariableDeclarationStub createStub(
            @NotNull
                    ObjJGlobalVariableDeclarationImpl variableDeclaration, StubElement stubParent) {
        return new ObjJGlobalVariableDeclarationStubImpl(stubParent, ObjJFileUtil.getContainingFileName(variableDeclaration), variableDeclaration.getVariableNameString(), variableDeclaration.getVariableType());
    }

    @Override
    public void serialize(
            @NotNull
                    ObjJGlobalVariableDeclarationStub stub,
            @NotNull
                    StubOutputStream stream) throws IOException {
        stream.writeName(Strings.notNull(stub.getFileName()));
        stream.writeName(stub.getVariableName());
        stream.writeName(Strings.notNull(stub.getVariableType()));
    }

    @NotNull
    @Override
    public ObjJGlobalVariableDeclarationStub deserialize(
            @NotNull
                    StubInputStream stream, StubElement stubParent) throws IOException {
        String fileName = StringRef.toString(stream.readName());
        if (fileName.isEmpty()) {
            fileName = null;
        }
        final String variableName = StringRef.toString(stream.readName());
        String variableType = StringRef.toString(stream.readName());
        if (variableType.isEmpty()) {
            variableType = null;
        }
        return new ObjJGlobalVariableDeclarationStubImpl(stubParent, fileName, variableName, variableType);
    }

    @Override
    public boolean shouldCreateStub(com.intellij.lang.ASTNode node) {
        PsiElement element = node.getPsi();
        return element instanceof ObjJGlobalVariableDeclaration;
    }

    @Override
    public void indexStub(@NotNull
                                  ObjJGlobalVariableDeclarationStub stub, @NotNull
                                  IndexSink indexSink) {
        ServiceManager.getService(StubIndexService.class).indexGlobalVariableDeclaration(stub, indexSink);
    }

}
