package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJAccessorProperty;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJAccessorPropertyImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJAccessorPropertyStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJAccessorPropertyStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJInstanceVariableDeclarationStub;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.utils.Strings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ObjJAccessorPropertyStubType extends ObjJStubElementType<ObjJAccessorPropertyStub, ObjJAccessorPropertyImpl> {

    ObjJAccessorPropertyStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJAccessorPropertyImpl.class, ObjJAccessorPropertyStub.class);
    }

    @Override
    public ObjJAccessorPropertyImpl createPsi(
            @NotNull
                    ObjJAccessorPropertyStub objJAccessorPropertyStub) {
        return new ObjJAccessorPropertyImpl(objJAccessorPropertyStub, this);
    }

    @NotNull
    @Override
    public ObjJAccessorPropertyStub createStub(
            @NotNull
                    ObjJAccessorPropertyImpl accessorProperty, StubElement parentStub) {
        final String containingClass = accessorProperty.getContainingClassName();
        final ObjJInstanceVariableDeclaration variableDeclaration = ObjJTreeUtil.getParentOfType(accessorProperty, ObjJInstanceVariableDeclaration.class);
        final String variableName;
        final String variableType;
        if (variableDeclaration != null) {
            final ObjJInstanceVariableDeclarationStub variableDeclarationStub = variableDeclaration.getStub();
            variableType = variableDeclarationStub != null ? variableDeclarationStub.getVarType() : variableDeclaration.getFormalVariableType().getText();
            variableName = variableDeclarationStub != null ? variableDeclarationStub.getVariableName() : variableDeclaration.getVariableName().getText();
        } else {
            variableName = null;
            variableType = null;
        }
        final String getter = variableName != null && variableType != null ? ObjJPsiImplUtil.getGetterSelector(variableName, variableType, accessorProperty) : null;
        final String setter = variableName != null && variableType != null ? ObjJPsiImplUtil.getSetterSelector(variableName, variableType, accessorProperty) : null;
        return new ObjJAccessorPropertyStubImpl(parentStub, containingClass, variableType, variableName, getter,setter);
    }

    @Override
    public void serialize(
            @NotNull
                    ObjJAccessorPropertyStub stub,
            @NotNull
                    StubOutputStream stream) throws IOException {
        stream.writeName(stub.getContainingClass());
        stream.writeName(Strings.notNull(stub.getVarType()));
        stream.writeName(Strings.notNull(stub.getVariableName()));
        stream.writeName(Strings.notNull(stub.getGetter()));
        stream.writeName(Strings.notNull(stub.getSetter()));
    }

    @NotNull
    @Override
    public ObjJAccessorPropertyStub deserialize(
            @NotNull
                    StubInputStream stream, StubElement parentStub) throws IOException {
        final String containingClass = StringRef.toString(stream.readName());
        final String varType = StringRef.toString(stream.readName());
        final String variableName = StringRef.toString(stream.readName());
        final String getter = StringRef.toString(stream.readName());
        final String setter = StringRef.toString(stream.readName());
        return new ObjJAccessorPropertyStubImpl(parentStub, containingClass,varType, variableName, getter, setter);
    }

    @Override
    public void indexStub(@NotNull ObjJAccessorPropertyStub stub, @NotNull IndexSink indexSink) {
        ServiceManager.getService(StubIndexService.class).indexAccessorProperty(stub, indexSink);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return node.getPsi() instanceof ObjJAccessorProperty;
    }
}
