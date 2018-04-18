package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJInstanceVariableDeclarationImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJInstanceVariableDeclarationStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJInstanceVariableDeclarationStub;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJAccessorPropertyPsiUtil;
import org.cappuccino_project.ide.intellij.plugin.utils.Strings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjJInstanceVariableDeclarationStubType extends ObjJStubElementType<ObjJInstanceVariableDeclarationStub, ObjJInstanceVariableDeclarationImpl> {
    public ObjJInstanceVariableDeclarationStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJInstanceVariableDeclarationImpl.class, ObjJInstanceVariableDeclarationStub.class);
    }

    @Override
    public ObjJInstanceVariableDeclarationImpl createPsi(
            @NotNull
                    ObjJInstanceVariableDeclarationStub objJInstanceVariableDeclarationStub) {
        return new ObjJInstanceVariableDeclarationImpl(objJInstanceVariableDeclarationStub, this);
    }

    @NotNull
    @Override
    public ObjJInstanceVariableDeclarationStub createStub(
            @NotNull
                    ObjJInstanceVariableDeclarationImpl declaration, StubElement stubElement) {
        String getter = null;
        String setter = null;
        String variableName = declaration.getVariableName() != null ? declaration.getVariableName().getText() : "";
        if (declaration.getAtAccessors() != null && declaration.getAccessorPropertyList().isEmpty() && !variableName.isEmpty()) {
            getter = ObjJAccessorPropertyPsiUtil.getGetterSelector(variableName, declaration.getFormalVariableType().getText());
            setter = ObjJAccessorPropertyPsiUtil.getSetterSelector(variableName, declaration.getFormalVariableType().getText());
            Logger.getAnonymousLogger().log(Level.INFO, "Variable: <"+variableName+">; getter: <"+getter+">; setter: <"+setter+">");
        }
        return new ObjJInstanceVariableDeclarationStubImpl(stubElement, declaration.getContainingClassName(), declaration.getFormalVariableType().getText(), variableName, getter, setter);
    }

    @Override
    public void serialize(
            @NotNull
                    ObjJInstanceVariableDeclarationStub stub,
            @NotNull
                    StubOutputStream stream) throws IOException {
        stream.writeName(stub.getContainingClass());
        stream.writeName(stub.getVarType());
        stream.writeName(stub.getVariableName());
        stream.writeName(Strings.notNull(stub.getGetter(), ""));
        stream.writeName(Strings.notNull(stub.getSetter(), ""));
    }
    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return true;
    }

    @Override
    public void indexStub(@NotNull ObjJInstanceVariableDeclarationStub stub, @NotNull IndexSink indexSink) {
        ServiceManager.getService(StubIndexService.class).indexInstanceVariable(stub, indexSink);
    }

    @NotNull
    @Override
    public ObjJInstanceVariableDeclarationStub deserialize(
            @NotNull
                    StubInputStream stream, StubElement parentStub) throws IOException {
        final String containingClass = StringRef.toString(stream.readName());
        final String varType = StringRef.toString(stream.readName());
        final String variableName = StringRef.toString(stream.readName());
        final String getter = StringRef.toString(stream.readName());
        final String setter = StringRef.toString(stream.readName());
        return new ObjJInstanceVariableDeclarationStubImpl(parentStub, containingClass, varType,variableName, getter, setter);
    }
}
