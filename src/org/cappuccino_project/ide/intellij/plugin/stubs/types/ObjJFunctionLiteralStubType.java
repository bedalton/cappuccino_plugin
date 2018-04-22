package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJFunctionLiteral;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJFunctionDeclarationImpl;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJFunctionLiteralImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJFunctionLiteralStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ObjJFunctionLiteralStubType extends ObjJAbstractFunctionDeclarationStubType<ObjJFunctionLiteralImpl, ObjJFunctionLiteralStubImpl> {

    ObjJFunctionLiteralStubType(
            @NotNull
                    String debugName) {
        super(debugName, ObjJFunctionLiteralImpl.class, ObjJFunctionLiteralStubImpl.class);
    }

    @Override
    public ObjJFunctionLiteralImpl createPsi(
            @NotNull
                    ObjJFunctionDeclarationElementStub<ObjJFunctionLiteralImpl> stub) {
        return new ObjJFunctionLiteralImpl(stub, this);
    }

    @NotNull
    @Override
    ObjJFunctionDeclarationElementStub<ObjJFunctionLiteralImpl> createStub(StubElement parent,
                                                                           @NotNull
                                                                                   String fileName,
                                                                           @NotNull
                                                                                   String fqName,
                                                                           @NotNull
                                                                                   List<String> paramNames,
                                                                           @Nullable
                                                                                   String returnType,
                                                                           final boolean shouldResolve
    ) {
        return new ObjJFunctionLiteralStubImpl(parent, fileName, fqName, paramNames, returnType, shouldResolve);
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return node.getPsi() instanceof ObjJFunctionLiteral && ((ObjJFunctionLiteral) node.getPsi()).getFunctionNameNode() != null;
    }
}
