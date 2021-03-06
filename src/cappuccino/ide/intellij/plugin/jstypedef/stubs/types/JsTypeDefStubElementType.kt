package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefLanguage
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefStubBasedElement
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.util.ReflectionUtil
import org.jetbrains.annotations.NonNls

import java.lang.reflect.Constructor


abstract class JsTypeDefStubElementType<StubT : StubElement<*>, PsiT : JsTypeDefStubBasedElement<*>>(@NonNls debugName: String, psiClass: Class<PsiT>) : IStubElementType<StubT, PsiT>(debugName, JsTypeDefLanguage.instance) {

    private val byNodeConstructor: Constructor<PsiT>

    init {
        try {
            byNodeConstructor = psiClass.getConstructor(ASTNode::class.java)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException("Stub element type declaration for " + psiClass.simpleName + " is missing required constructors", e)
        }
    }

    fun createPsiFromAst(node: ASTNode): PsiT {
        return ReflectionUtil.createInstance(byNodeConstructor, node)
    }

    override fun getExternalId(): String {
        return "JsTypeDef." + toString()
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return true
    }
}