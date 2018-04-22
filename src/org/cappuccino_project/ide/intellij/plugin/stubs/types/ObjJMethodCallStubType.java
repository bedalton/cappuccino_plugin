package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodCall;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJMethodCallImpl;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJMethodCallStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodCallStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjJMethodCallStubType extends ObjJStubElementType<ObjJMethodCallStub, ObjJMethodCallImpl> {
    ObjJMethodCallStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJMethodCallImpl.class, ObjJMethodCallStub.class);
    }

    @Override
    public ObjJMethodCallImpl createPsi(
            @NotNull
                    ObjJMethodCallStub objJMethodCallStub) {
        return new ObjJMethodCallImpl(objJMethodCallStub, this);
    }

    @NotNull
    @Override
    public ObjJMethodCallStub createStub(
            @NotNull
                    ObjJMethodCallImpl methodCall, StubElement stubParent) {
        final String className = methodCall.getContainingClassName();
        final String callTarget = methodCall.getCallTargetText();
        final List<String> callTargetTypes = Collections.singletonList(ObjJClassType.UNDETERMINED);//methodCall.getPossibleCallTargetTypes();
        final List<String> selectorStrings = methodCall.getSelectorStrings();
        final boolean shouldResolve = methodCall.shouldResolve();
        return new ObjJMethodCallStubImpl(stubParent, className, callTarget, callTargetTypes, selectorStrings, shouldResolve);
    }

    @Override
    public void serialize(
            @NotNull
                    ObjJMethodCallStub stub,
            @NotNull
                    StubOutputStream stream) throws IOException {
        stream.writeName(stub.getContainingClassName());
        stream.writeName(stub.getCallTarget());
        stream.writeInt(stub.getPossibleCallTargetTypes().size());
        for (String possibleCallTargetType : stub.getPossibleCallTargetTypes()) {
            stream.writeName(possibleCallTargetType);
        }
        stream.writeInt(stub.getSelectorStrings().size());
        for (String selector : stub.getSelectorStrings()) {
            stream.writeName(selector);
        }
        stream.writeBoolean(stub.shouldResolve());
    }

    @NotNull
    @Override
    public ObjJMethodCallStub deserialize(
            @NotNull
                    StubInputStream stream, StubElement stubParent) throws IOException {
        final String containingClassName = StringRef.toString(stream.readName());
        final String callTarget = StringRef.toString(stream.readName());
        final int numCallTargetTypes = stream.readInt();
        List<String> callTargetTypes = new ArrayList<>();
        for (int i=0;i<numCallTargetTypes; i++) {
            callTargetTypes.add(StringRef.toString(stream.readName()));
        }
        final int numSelectorStrings = stream.readInt();
        final List<String> selectors = new ArrayList<>();
        for (int i=0;i<numSelectorStrings;i++) {
            selectors.add(StringRef.toString(stream.readName()));
        }
        final boolean shouldResolve = stream.readBoolean();
        return new ObjJMethodCallStubImpl(stubParent, containingClassName, callTarget, callTargetTypes, selectors, shouldResolve);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return false;
    }

    @Override
    public void indexStub(
            @NotNull
                    ObjJMethodCallStub stub, @NotNull
                    IndexSink indexSink) {
        ServiceManager.getService(StubIndexService.class).indexMethodCall(stub, indexSink);
    }
}
