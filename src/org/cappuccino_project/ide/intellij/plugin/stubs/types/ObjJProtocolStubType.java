package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.stubs.*;
import com.intellij.util.io.StringRef;
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJProtocolDeclarationImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJProtocolDeclarationStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJProtocolDeclarationStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjJProtocolStubType extends ObjJClassDeclarationStubType<ObjJProtocolDeclarationStub, ObjJProtocolDeclarationImpl> {

    ObjJProtocolStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJProtocolDeclarationImpl.class, ObjJProtocolDeclarationStub.class);
    }

    @Override
    public ObjJProtocolDeclarationImpl createPsi(
            @NotNull
                    ObjJProtocolDeclarationStub objJProtocolDeclarationStub) {
        return new ObjJProtocolDeclarationImpl(objJProtocolDeclarationStub);
    }

    @NotNull
    @Override
    public ObjJProtocolDeclarationStub createStub(
            @NotNull
                    ObjJProtocolDeclarationImpl element, StubElement parentStub) {
        final String className = element.getClassName() != null ? element.getClassName().getText() : "";
        final List<String> protocols = element.getInheritedProtocols();
        return new ObjJProtocolDeclarationStubImpl(parentStub, className, protocols);
    }


    @Override
    public void serialize(
            @NotNull
                    ObjJProtocolDeclarationStub stub,
            @NotNull
                    StubOutputStream stream) throws IOException {
        final String className = stub.getClassName();
        stream.writeName(className);

        //protocols
        final List<String> protocols = stub.getInheritedProtocols();
        final int numProtocols = protocols.size();
        stream.writeInt(numProtocols);
        // Write protocol names
        for (String protocol : protocols) {
            stream.writeName(protocol);
        }
    }

    @NotNull
    @Override
    public ObjJProtocolDeclarationStub deserialize(
            @NotNull
                    StubInputStream stream, StubElement parentStub) throws IOException {
        final String className = StringRef.toString(stream.readName());
        final int numProtocols = stream.readInt();
        final List<String> inheritedProtocols = new ArrayList<>();
        for (int i=0;i<numProtocols;i++) {
            inheritedProtocols.add(StringRef.toString(stream.readName()));
        }
        return new ObjJProtocolDeclarationStubImpl(parentStub, className, inheritedProtocols);
    }

    @Override
    public void indexStub(@NotNull ObjJProtocolDeclarationStub stub, @NotNull
            IndexSink indexSink) {
        ServiceManager.getService(StubIndexService.class).indexClassDeclaration(stub, indexSink);
    }
}
