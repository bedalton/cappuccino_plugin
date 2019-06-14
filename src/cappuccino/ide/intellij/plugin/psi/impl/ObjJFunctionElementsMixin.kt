package cappuccino.ide.intellij.plugin.psi.impl

import cappuccino.ide.intellij.plugin.caches.ObjJFunctionDeclarationCache
import cappuccino.ide.intellij.plugin.caches.ObjJFunctionNameCache
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionNameElement
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubElementType
import com.intellij.lang.ASTNode

abstract class ObjJFunctionDeclarationElementMixin<StubT : ObjJFunctionDeclarationElementStub<*>> : ObjJStubBasedElementImpl<StubT>, ObjJFunctionDeclarationElement<StubT> {

    protected abstract val cache:ObjJFunctionDeclarationCache

    constructor(
            stub: StubT, type:ObjJStubElementType<*, *>) : super(stub, type)

    constructor(node: ASTNode) : super(node)

    override val cachedReturnType:InferenceResult? get() = cache.returnTypes
}

abstract class ObjJFunctionNameMixin(node: ASTNode):ObjJCompositeElementImpl(node), ObjJFunctionNameElement {


    private val cache:ObjJFunctionNameCache by lazy {
        ObjJFunctionNameCache(this)
    }

    override val cachedParentFunctionDeclaration:ObjJFunctionDeclarationElement<*>?
        get() = cache.parentFunctionDeclarationElement

    override val returnTypes
        get() = cache.returnTypes
}