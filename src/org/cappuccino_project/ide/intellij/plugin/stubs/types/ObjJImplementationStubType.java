package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.stubs.*;
import com.intellij.util.io.StringRef;
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImplementationDeclarationImpl;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJImplementationStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImplementationStub;
import org.cappuccino_project.ide.intellij.plugin.utils.Strings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjJImplementationStubType extends ObjJClassDeclarationStubType<ObjJImplementationStub, ObjJImplementationDeclarationImpl> {
    ObjJImplementationStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJImplementationDeclarationImpl.class, ObjJImplementationStub.class);
    }

    @Override
    public ObjJImplementationDeclarationImpl createPsi(
            @NotNull
                    ObjJImplementationStub objJImplementationStub) {
        return new ObjJImplementationDeclarationImpl(objJImplementationStub, this);
    }

    @NotNull
    @Override
    public ObjJImplementationStub createStub(
            @NotNull
                    ObjJImplementationDeclarationImpl element, StubElement parentStub) {
        final String className = element.getClassName() != null ? element.getClassName().getText() : "";
        final String superClassName = element.getSuperClass() != null ? element.getSuperClass().getText() : null;
        final List<String> protocols = element.getInheritedProtocols();
        final String categoryName = element.getCategoryName() != null ? element.getCategoryName().getText() : null;
        return new ObjJImplementationStubImpl(parentStub, className, superClassName, categoryName, protocols, shouldResolve(element.getNode()));
    }


    @NotNull
    @Override
    public String getExternalId() {
        return "objj." + toString();
    }

    @Override
    public void serialize(
            @NotNull
                    ObjJImplementationStub stub,
            @NotNull
                    StubOutputStream stream) throws IOException {
        stream.writeName(stub.getClassName());
        stream.writeName(Strings.notNull(stub.getSuperClassName(), ObjJClassType.UNDEF_CLASS_NAME));
        stream.writeBoolean(stub.isCategory());
        if (stub.isCategory()) {
            stream.writeName(stub.getCategoryName());
        }
        //protocols
        final List<String> protocols = stub.getInheritedProtocols();
        final int numProtocols = protocols.size();
        stream.writeInt(numProtocols);
        // Write protocol names
        for (String protocol : protocols) {
            stream.writeName(protocol);
        }
        stream.writeBoolean(stub.shouldResolve());
    }

    @NotNull
    @Override
    public ObjJImplementationStub deserialize(
            @NotNull
                    StubInputStream stream, StubElement parentStub) throws IOException {
        final String className = StringRef.toString(stream.readName());
        final String superClassName = StringRef.toString(stream.readName());
        final String categoryName = stream.readBoolean() ? StringRef.toString(stream.readName()) : null;
        final int numProtocols = stream.readInt();
        final List<String> inheritedProtocols = new ArrayList<>();
        for (int i=0;i<numProtocols;i++) {
            inheritedProtocols.add(StringRef.toString(stream.readName()));
        }
        final boolean shouldResolve = stream.readBoolean();
        return new ObjJImplementationStubImpl(parentStub, className, !superClassName.equals(ObjJClassType.UNDEF_CLASS_NAME) ? superClassName : null, categoryName, inheritedProtocols, shouldResolve);
    }

    @Override
    public void indexStub(
            @NotNull
                    ObjJImplementationStub objJImplementationStub,
            @NotNull
                    IndexSink indexSink) {
        ServiceManager.getService(StubIndexService.class).indexClassDeclaration(objJImplementationStub, indexSink);
    }
}
