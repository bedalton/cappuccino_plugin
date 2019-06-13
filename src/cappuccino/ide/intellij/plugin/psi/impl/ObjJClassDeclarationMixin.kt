package cappuccino.ide.intellij.plugin.psi.impl


import cappuccino.ide.intellij.plugin.caches.ObjJImplementationDeclarationCache
import cappuccino.ide.intellij.plugin.caches.ObjJProtocolDeclarationCache
import cappuccino.ide.intellij.plugin.psi.ObjJAccessorProperty
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import com.intellij.lang.ASTNode
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImplementationStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJProtocolDeclarationStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes


abstract class ObjJProtocolDeclarationMixin : ObjJStubBasedElementImpl<ObjJProtocolDeclarationStub>, ObjJProtocolDeclaration {

    val cache:ObjJProtocolDeclarationCache = ObjJProtocolDeclarationCache(this)

    constructor(
            stub: ObjJProtocolDeclarationStub) : super(stub, ObjJStubTypes.PROTOCOL)

    constructor(node: ASTNode) : super(node)

    override fun getAccessors(internalOnly: Boolean): List<ObjJAccessorProperty> {
        return cache.getAccessorProperties(internalOnly)
    }

    override fun getMethodsHeader(internalOnly: Boolean): List<ObjJMethodHeader> {
        return cache.getMethods(internalOnly)
    }

    override fun getAllSelectors(internalOnly: Boolean): Set<String> {
        return cache.getAllSelectors(internalOnly)
    }

    override fun getMethodHeaders() : List<ObjJMethodHeader> {
        return cache.getMethods(false)
    }
}

abstract class ObjJImplementationDeclarationMixin : ObjJStubBasedElementImpl<ObjJImplementationStub>, ObjJImplementationDeclaration {

    val cache:ObjJImplementationDeclarationCache = ObjJImplementationDeclarationCache(this)

    constructor(
            stub: ObjJImplementationStub) : super(stub, ObjJStubTypes.PROTOCOL)

    constructor(node: ASTNode) : super(node)

    override fun getAccessors(internalOnly: Boolean): List<ObjJAccessorProperty> {
        return cache.getAccessorProperties(internalOnly)
    }

    override fun getMethodsHeader(internalOnly: Boolean): List<ObjJMethodHeader> {
        return cache.getMethods(internalOnly)
    }

    override fun getAllSelectors(internalOnly: Boolean): Set<String> {
        return cache.getAllSelectors(internalOnly)
    }

    override fun getMethodHeaders() : List<ObjJMethodHeader> {
        return cache.getMethods(false)
    }

    override fun getAllInheritedProtocols() {

    }

}
