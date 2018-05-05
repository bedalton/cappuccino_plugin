package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJVarTypeIdImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJVarTypeIdStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJVarTypeIdStub;
import org.cappuccino_project.ide.intellij.plugin.utils.Strings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ObjJVarTypeIdStubType extends ObjJStubElementType<ObjJVarTypeIdStub, ObjJVarTypeIdImpl> {

    ObjJVarTypeIdStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJVarTypeIdImpl.class, ObjJVarTypeIdStub.class);
    }


    @Override
    public void serialize(
            @NotNull
                    ObjJVarTypeIdStub stub,
            @NotNull
                    StubOutputStream stream) throws IOException {
        stream.writeName(stub.getIdType());
        stream.writeBoolean(stub.shouldResolve());
    }

    @NotNull
    @Override
    public ObjJVarTypeIdStub deserialize(
            @NotNull
                    StubInputStream stream, StubElement stubParent) throws IOException {
        String idType = Strings.notNull(StringRef.toString(stream.readName()), "id");
        final boolean shouldResolve = stream.readBoolean();
        return new ObjJVarTypeIdStubImpl(stubParent, idType, shouldResolve);
    }

    @Override
    public void indexStub(@NotNull ObjJVarTypeIdStub stub, @NotNull
            IndexSink indexSink) {
        //ServiceManager.getService(StubIndexService.class).indexVarTypeId(stub, indexSink);
    }

    @Override
    public ObjJVarTypeIdImpl createPsi(
            @NotNull
                    ObjJVarTypeIdStub stub) {
        return new ObjJVarTypeIdImpl(stub, this);
    }

    @NotNull
    @Override
    public ObjJVarTypeIdStub createStub(
            @NotNull
                    ObjJVarTypeIdImpl varTypeId, StubElement stubParent) {
        return new ObjJVarTypeIdStubImpl(stubParent, varTypeId.getIdType(), shouldResolve(varTypeId.getNode()));
    }

    @NotNull
    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return false;
    }
}
