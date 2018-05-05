package org.cappuccino_project.ide.intellij.plugin.psi.impl;


import com.intellij.lang.ASTNode;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJProtocolDeclaration;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJProtocolDeclarationStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.jetbrains.annotations.NotNull;


public abstract class ObjJProtocolDeclarationMixin extends ObjJStubBasedElementImpl<ObjJProtocolDeclarationStub> implements ObjJProtocolDeclaration {

    public ObjJProtocolDeclarationMixin(
            @NotNull
                    ObjJProtocolDeclarationStub stub) {
        super(stub, ObjJStubTypes.PROTOCOL);
    }

    public ObjJProtocolDeclarationMixin(@NotNull ASTNode node) {
        super(node);
    }
}
