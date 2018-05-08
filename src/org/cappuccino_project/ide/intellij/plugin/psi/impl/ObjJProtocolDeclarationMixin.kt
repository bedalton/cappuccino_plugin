package org.cappuccino_project.ide.intellij.plugin.psi.impl


import com.intellij.lang.ASTNode
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJProtocolDeclarationStub
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes


abstract class ObjJProtocolDeclarationMixin : ObjJStubBasedElementImpl<ObjJProtocolDeclarationStub>, ObjJProtocolDeclaration {

    constructor(
            stub: ObjJProtocolDeclarationStub) : super(stub, ObjJStubTypes.PROTOCOL) {
    }

    constructor(node: ASTNode) : super(node) {}
}
