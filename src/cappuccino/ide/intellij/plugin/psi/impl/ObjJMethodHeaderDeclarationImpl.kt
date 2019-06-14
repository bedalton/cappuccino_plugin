package cappuccino.ide.intellij.plugin.psi.impl

import cappuccino.ide.intellij.plugin.caches.getMethodHeaderCache
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderDeclarationStub
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubElementType
import com.intellij.lang.ASTNode

abstract class ObjJMethodHeaderDeclarationImpl<StubT : ObjJMethodHeaderDeclarationStub<*>> : ObjJStubBasedElementImpl<StubT>, ObjJCompositeElement, ObjJHasMethodSelector, ObjJHasContainingClass, ObjJMethodHeaderDeclaration<StubT> {

    constructor(stub:StubT, type:ObjJStubElementType<*, *>) : super(stub, type)
    constructor(node:ASTNode) : super(node)


    private val cache = getMethodHeaderCache(this)
    //val returnType:Set<String>

    override val cachedTypes get() = cache.returnTypes

    abstract override fun getReturnTypes(tag:Long): Set<String>

    abstract override val explicitReturnType:String

    abstract override val methodScope: MethodScope

    override val isStatic: Boolean
        get() = methodScope == MethodScope.STATIC
}
