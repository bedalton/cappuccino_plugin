package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJMethodHeaderImpl;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJMethodHeaderStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils;
import org.cappuccino_project.ide.intellij.plugin.utils.Strings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ObjJMethodHeaderStubType extends ObjJStubElementType<ObjJMethodHeaderStub, ObjJMethodHeaderImpl> {
    ObjJMethodHeaderStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJMethodHeaderImpl.class, ObjJMethodHeaderStub.class);
    }

    @Override
    public ObjJMethodHeaderImpl createPsi(
            @NotNull
                    ObjJMethodHeaderStub objJMethodHeaderStub) {
        return new ObjJMethodHeaderImpl(objJMethodHeaderStub, this);
    }

    @NotNull
    @Override
    public ObjJMethodHeaderStub createStub(
            @NotNull
                    ObjJMethodHeaderImpl objJMethodHeader, StubElement parentStub) {
        final String containingClassName = objJMethodHeader.getContainingClassName();
        final List<String> selectors = objJMethodHeader.getSelectorStrings();
        final List<String> params = objJMethodHeader.getParamTypesAsStrings();
        final String returnType = null;//objJMethodHeader.getReturnType();
        final boolean required = ObjJMethodPsiUtils.methodRequired(objJMethodHeader);
        final boolean shouldResolve = ObjJPsiImplUtil.shouldResolve(objJMethodHeader);
        return new ObjJMethodHeaderStubImpl(parentStub, containingClassName, objJMethodHeader.isStatic(), selectors, params, returnType, required, shouldResolve);
    }

    @Override
    public void serialize(
            @NotNull
                    ObjJMethodHeaderStub stub,
            @NotNull
                    StubOutputStream stubOutputStream) throws IOException {
        final String containingClassName = stub.getContainingClassName();
        stubOutputStream.writeName(containingClassName);
        stubOutputStream.writeBoolean(stub.isStatic());
        final int numSelectors = stub.getSelectorStrings().size();
        stubOutputStream.writeInt(numSelectors);
        for (String selector : stub.getSelectorStrings()) {
            stubOutputStream.writeName(Strings.notNull(selector));
        }
        final int numParams = stub.getParamTypes().size();
        stubOutputStream.writeInt(numParams);
        for (String param : stub.getParamTypes()) {
            stubOutputStream.writeName(Strings.notNull(param));
        }
        stubOutputStream.writeName(stub.getReturnType().getClassName());
        stubOutputStream.writeBoolean(stub.isRequired());
        stubOutputStream.writeBoolean(stub.shouldResolve());

    }

    @NotNull
    @Override
    public ObjJMethodHeaderStubImpl deserialize(
            @NotNull
                    StubInputStream stream, StubElement parentStub) throws IOException {
        final String containingClassName = StringRef.toString(stream.readName());
        final boolean isStatic = stream.readBoolean();
        final int numSelectors = stream.readInt();
        final List<String> selectors = new ArrayList<>();
        for (int i=0;i<numSelectors;i++) {
            selectors.add(StringRef.toString(stream.readName()));
        }
        final int numParams = stream.readInt();
        final List<String> params = new ArrayList<>();
        for (int i=0;i<numParams;i++) {
            params.add(StringRef.toString(stream.readName()));
        }
        final String returnType = StringRef.toString(stream.readName());
        final boolean required = stream.readBoolean();
        final boolean shouldResolve = stream.readBoolean();
        return new ObjJMethodHeaderStubImpl(parentStub, containingClassName, isStatic, selectors, params, returnType, required, shouldResolve);
    }


    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return true;
    }


    @Override
    public void indexStub(@NotNull ObjJMethodHeaderStub stub, @NotNull
            IndexSink sink) {
        ServiceManager.getService(StubIndexService.class).indexMethod(stub, sink);
    }
}
