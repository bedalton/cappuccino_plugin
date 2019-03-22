package cappuccino.ide.intellij.plugin.psi.impl


import com.intellij.lang.ASTNode
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJProtocolDeclarationStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes


abstract class ObjJProtocolDeclarationMixin : ObjJStubBasedElementImpl<ObjJProtocolDeclarationStub>, ObjJProtocolDeclaration {

    constructor(
            stub: ObjJProtocolDeclarationStub) : super(stub, ObjJStubTypes.PROTOCOL)

    constructor(node: ASTNode) : super(node)
}
