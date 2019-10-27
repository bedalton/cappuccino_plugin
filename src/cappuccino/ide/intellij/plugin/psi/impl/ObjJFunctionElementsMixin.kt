package cappuccino.ide.intellij.plugin.psi.impl

import cappuccino.ide.intellij.plugin.caches.ObjJFunctionDeclarationCache
import cappuccino.ide.intellij.plugin.caches.ObjJFunctionNameCache
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.inferFunctionDeclarationReturnType
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionNameElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasTreeStructureElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFunctionDeclarationPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJTreeStructureUtil
import cappuccino.ide.intellij.plugin.structure.ObjJStructureViewElement
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubElementType
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.lang.ASTNode

abstract class ObjJFunctionDeclarationElementMixin<StubT : ObjJFunctionDeclarationElementStub<*>> : ObjJStubBasedElementImpl<StubT>, ObjJFunctionDeclarationElement<StubT>, ObjJHasTreeStructureElement {

    protected abstract val cache:ObjJFunctionDeclarationCache

    constructor(
            stub: StubT, type:ObjJStubElementType<*, *>) : super(stub, type)

    constructor(node: ASTNode) : super(node)

    override fun getCachedReturnType(tag:Long):InferenceResult? {
        return inferFunctionDeclarationReturnType(this, tag)
    }

    override fun createTreeStructureElement(): ObjJStructureViewElement {
        return ObjJTreeStructureUtil.createTreeStructureElement(this)
    }
}


/**
 * A mixin class for function name elements to utilize caching
 */
abstract class ObjJFunctionNameMixin(node: ASTNode):ObjJCompositeElementImpl(node), ObjJFunctionNameElement {

    /**
     * Cache for this function name
     */
    private val cache:ObjJFunctionNameCache by lazy {
        ObjJFunctionNameCache(this)
    }

    /**
     * Attempts to get parent function declaration from cache
     */
    override val cachedParentFunctionDeclaration:ObjJFunctionDeclarationElement<*>?
        get() = cache.parentFunctionDeclarationElement ?: ObjJFunctionDeclarationPsiUtil.getParentFunctionDeclaration(this)

    /**
     * Gets cached value for return type
     */
    override fun getCachedReturnType(tag:Long):InferenceResult? {
        val returnType = cache.getReturnType(tag)
        if (returnType != null)
            return returnType
        val cachedParent = cachedParentFunctionDeclaration ?: return null
        return inferFunctionDeclarationReturnType(cachedParent, tag)
    }
}