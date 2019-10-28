package cappuccino.ide.intellij.plugin.psi.impl

import cappuccino.ide.intellij.plugin.caches.ObjJFunctionDeclarationCache
import cappuccino.ide.intellij.plugin.caches.ObjJFunctionNameCache
import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.inference.INFERRED_ANY_TYPE
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionNameElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasTreeStructureElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJUniversalFunctionElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFunctionDeclarationPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJTreeStructureUtil
import cappuccino.ide.intellij.plugin.psi.utils.docComment
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

    override val parameterNames : List<String> by lazy {
        formalParameterArgList.map {
            it.variableName?.text ?: "_"
        }
    }

    override fun toJsFunctionType(tag: Long): JsTypeListType.JsTypeListFunctionType {
        val returnTypes = inferFunctionDeclarationReturnType(this, tag) ?: INFERRED_ANY_TYPE
        return JsTypeListType.JsTypeListFunctionType(
                name = this.functionNameString,
                parameters = this.parameterTypes(),
                returnType = returnTypes,
                comment = docComment?.commentText,
                static = true
        )
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
    override val cachedParentFunctionDeclaration:ObjJUniversalFunctionElement?
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