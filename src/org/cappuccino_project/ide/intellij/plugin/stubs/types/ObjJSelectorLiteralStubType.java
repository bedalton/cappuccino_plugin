package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelectorLiteral;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJSelectorLiteralImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJSelectorLiteralStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJSelectorLiteralStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjJSelectorLiteralStubType extends ObjJStubElementType<ObjJSelectorLiteralStub, ObjJSelectorLiteralImpl> {
    ObjJSelectorLiteralStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJSelectorLiteralImpl.class, ObjJSelectorLiteralStub.class);
    }

    @Override
    public ObjJSelectorLiteralImpl createPsi(
            @NotNull
                    ObjJSelectorLiteralStub objJSelectorLiteralStub) {
        return new ObjJSelectorLiteralImpl(objJSelectorLiteralStub, this);
    }

    @NotNull
    @Override
    public ObjJSelectorLiteralStub createStub(
            @NotNull
                    ObjJSelectorLiteralImpl selectorLiteral, StubElement stubParent) {
        return new ObjJSelectorLiteralStubImpl(stubParent, selectorLiteral.getContainingClassName(), selectorLiteral.getSelectorStrings(), selectorLiteral.shouldResolve());
    }

    @Override
    public void serialize(
            @NotNull
                    ObjJSelectorLiteralStub stub,
            @NotNull
                    StubOutputStream stream) throws IOException {
        stream.writeName(stub.getContainingClassName());
        stream.writeInt(stub.getSelectorStrings().size());
        for (String selector : stub.getSelectorStrings()) {
            stream.writeName(selector);
        }
        stream.writeBoolean(stub.shouldResolve());
    }

    @NotNull
    @Override
    public ObjJSelectorLiteralStub deserialize(
            @NotNull
                    StubInputStream stream, StubElement stubParent) throws IOException {
        final String containingClassName = StringRef.toString(stream.readName());
        final int numSelectorStrings = stream.readInt();
        final List<String> selectorStrings = new ArrayList<>();
        for (int i=0;i<numSelectorStrings;i++) {
            selectorStrings.add(StringRef.toString(stream.readName()));
        }
        final boolean shouldResolve = stream.readBoolean();
        return new ObjJSelectorLiteralStubImpl(stubParent, containingClassName, selectorStrings, shouldResolve);
    }

    @Override
    public void indexStub(@NotNull ObjJSelectorLiteralStub stub, @NotNull IndexSink indexSink) {
        ServiceManager.getService(StubIndexService.class).indexSelectorLiteral(stub, indexSink);
    }
}
