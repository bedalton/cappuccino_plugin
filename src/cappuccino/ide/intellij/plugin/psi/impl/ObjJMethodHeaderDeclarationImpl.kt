package cappuccino.ide.intellij.plugin.psi.impl

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.Tag
import cappuccino.ide.intellij.plugin.inference.toClassList
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderDeclarationStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubElementType
import com.intellij.lang.ASTNode

abstract class ObjJMethodHeaderDeclarationImpl<StubT : ObjJMethodHeaderDeclarationStub<*>> : ObjJStubBasedElementImpl<StubT>, ObjJCompositeElement, ObjJHasMethodSelector, ObjJHasContainingClass, ObjJMethodHeaderDeclaration<StubT> {

    constructor(stub:StubT, type:ObjJStubElementType<*, *>) : super(stub, type)
    constructor(node:ASTNode) : super(node)

    //val returnType:Set<String>

    override fun getCachedReturnType(tag: Tag): InferenceResult? = methodHeaderCache.getCachedReturnType(tag)

    override fun getReturnTypes(tag: Tag): Set<String> = getCachedReturnType(tag)?.toClassList("?").orEmpty()

    abstract override val explicitReturnType:String

    abstract override val methodScope: MethodScope

    override val isStatic: Boolean
        get() = methodScope == MethodScope.STATIC
}
