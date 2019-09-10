package cappuccino.ide.intellij.plugin.psi.impl

import cappuccino.ide.intellij.plugin.caches.ObjJVariableNameCache
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.inferQualifiedReferenceType
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.previousSiblings
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFunctionDeclarationPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.references.ObjJVariableReference
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJVariableNameStub
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType

abstract class ObjJVariableNameMixin: ObjJStubBasedElementImpl<ObjJVariableNameStub>, ObjJVariableName {

    private val cache = ObjJVariableNameCache(this)


    constructor(stub: ObjJVariableNameStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    constructor(node: ASTNode) : super(node)

    override fun getReference(): ObjJVariableReference {
        return ObjJPsiImplUtil.getReference(this)
    }

    abstract override fun getName(): String;

    override val cachedParentFunctionDeclaration: ObjJFunctionDeclarationElement<*>?
        get() = cache.cachedParentFunctionDeclaration ?: ObjJFunctionDeclarationPsiUtil.getParentFunctionDeclaration(this.reference.resolve())

    override fun getCachedMethods(tag:Long)
            = cache.getMethods(tag)

    override fun getClassTypes(tag:Long): InferenceResult?
        = inferQualifiedReferenceType(this.previousSiblings + this, tag)//cache.getClassTypes(tag)

}