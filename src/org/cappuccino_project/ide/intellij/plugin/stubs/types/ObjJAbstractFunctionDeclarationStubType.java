package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class ObjJAbstractFunctionDeclarationStubType<PsiT extends ObjJFunctionDeclarationElement<? extends ObjJFunctionDeclarationElement>, Stub extends ObjJFunctionDeclarationElementStub<PsiT>> extends ObjJStubElementType<ObjJFunctionDeclarationElementStub<PsiT>, PsiT> {
    ObjJAbstractFunctionDeclarationStubType(
            @NotNull
                    String debugName, Class<PsiT> functionDecClass, Class<Stub> stubClass) {
        super(debugName, functionDecClass, stubClass);
    }

    @NotNull
    @Override
    public ObjJFunctionDeclarationElementStub<PsiT> createStub(
            @NotNull
                    PsiT element, StubElement stubParent) {
        final String fileName = element.getContainingFile() != null && element.getContainingFile().getVirtualFile() != null ? element.getContainingFile().getVirtualFile().getName() : "undefined";
        final String functionNameString = element.getFunctionNameAsString();
        final List<String> paramNames = element.getParamNames();
        final String returnType = element.getReturnType();
        return createStub(stubParent, fileName, functionNameString, paramNames, returnType);
    }

    @NotNull
    abstract ObjJFunctionDeclarationElementStub<PsiT> createStub(StubElement parent,
                                                                 @NotNull
                                                                         String fileName,
                                                                 @NotNull
                                                                         String fqName,
                                                                 @NotNull
                                                                         List<String> paramNames,
                                                                 @Nullable
                                                                         String returnType);

    @Override
    public void serialize(
            @NotNull
                    ObjJFunctionDeclarationElementStub stub,
            @NotNull
                    StubOutputStream stream) throws IOException {

        stream.writeName(stub.getFileName());
        stream.writeName(stub.getFqName());
        stream.writeInt(stub.getNumParams());
        for (Object param : stub.getParamNames()) {
            stream.writeName((String)param);
        }
        stream.writeName(stub.getReturnType());
    }

    @NotNull
    @Override
    public ObjJFunctionDeclarationElementStub<PsiT> deserialize(
            @NotNull
                    StubInputStream stream, StubElement stubParent) throws IOException {
        final String fileName = StringRef.toString(stream.readName());
        final String fqName = StringRef.toString(stream.readName());
        final int numParams = stream.readInt();
        final List<String> paramNames = new ArrayList<>();
        for (int i=0;i<numParams;i++) {
            paramNames.add(StringRef.toString(stream.readName()));
        }
        final String returnType = StringRef.toString(stream.readName());
        return createStub(stubParent, fileName, fqName, paramNames, returnType);
    }

    @Override
    public void indexStub(@NotNull ObjJFunctionDeclarationElementStub stub, @NotNull IndexSink indexSink) {
        ServiceManager.getService(StubIndexService.class).indexFunctionDeclaration(stub, indexSink);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return node.getPsi() instanceof ObjJFunctionDeclarationElement;
    }
}
