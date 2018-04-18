package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.util.io.StringRef;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage;
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService;
import org.cappuccino_project.ide.intellij.plugin.stubs.ObjJStubVersions;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJFileStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFileStub;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjJFileStubType extends IStubFileElementType<ObjJFileStub> {

    private static final String NAME = "objj.FILE";
    public ObjJFileStubType() {
        super(NAME, ObjJLanguage.INSTANCE);
    }

    public ObjJFileStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJLanguage.INSTANCE);
    }

    @Override
    public StubBuilder getBuilder() {
        return new ObjJFileStubBuilder();
    }
    @Override
    public int getStubVersion() {
        return ObjJStubVersions.SOURCE_STUB_VERSION;
    }

    @NotNull
    @Override
    public String getExternalId() {
        return NAME;
    }

    @Override
    public void serialize(@NotNull ObjJFileStub stub, @NotNull StubOutputStream stream)
            throws IOException {
        stream.writeName(stub.getFileName());
        stream.writeInt(stub.getImports().size());
        for (String importStatement : stub.getImports()) {
            stream.writeName(importStatement);
        }
    }

    @NotNull
    @Override
    public ObjJFileStub deserialize(@NotNull StubInputStream stream, StubElement parentStub) throws IOException {
        String fileName = StringRef.toString(stream.readName());
        int numImports = stream.readInt();
        List<String> imports = new ArrayList<>();
        for (int i=0;i<numImports;i++) {
            imports.add(StringRef.toString(stream.readName()));
        }
        return new ObjJFileStubImpl(null, fileName, imports);
    }

    @Override
    public void indexStub(@NotNull ObjJFileStub stub, @NotNull IndexSink sink) {
        StubIndexService.getInstance().indexFile(stub, sink);
    }
}
