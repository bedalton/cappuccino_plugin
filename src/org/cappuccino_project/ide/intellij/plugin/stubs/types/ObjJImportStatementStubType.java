package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImportFrameworkImpl;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJImportStatement;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJImportStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportStub;
import org.cappuccino_project.ide.intellij.plugin.utils.Strings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class ObjJImportStatementStubType<PsiT extends ObjJImportStatement<? extends ObjJImportStub<PsiT>>> extends ObjJStubElementType<ObjJImportStub<PsiT>, PsiT>{

    public ObjJImportStatementStubType(
            @NotNull
                    String debugName,
            @NotNull
                    Class<PsiT> psiClass) {
        super(debugName, psiClass, ObjJImportStub.class);
    }
    @NotNull
    @Override
    public ObjJImportStub<PsiT> createStub(
            @NotNull
                    PsiT statement, StubElement stubParent) {
        return new ObjJImportStubImpl<PsiT>(stubParent, this, statement.getFrameworkName(), statement.getFileName());
    }

    @Override
    public void serialize(
            @NotNull
                    ObjJImportStub<PsiT> stub,
            @NotNull
                    StubOutputStream stream) throws IOException {
        stream.writeName(Strings.notNull(stub.getFramework()));
        stream.writeName(stub.getFileName());
    }

    @NotNull
    @Override
    public ObjJImportStub<PsiT> deserialize(
            @NotNull
                    StubInputStream stream, StubElement stubParent) throws IOException {
        String frameworkName = StringRef.toString(stream.readName());
        if (frameworkName.isEmpty()) {
            frameworkName = null;
        }
        String fileName = StringRef.toString(stream.readName());
        return new ObjJImportStubImpl<>(stubParent, this, frameworkName, fileName);
    }
}
