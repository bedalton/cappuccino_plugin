package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJStubBasedElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.util.ReflectionUtil
import org.jetbrains.annotations.NonNls
import java.lang.reflect.Constructor


abstract class ObjJStubElementType<StubT : StubElement<*>, PsiT : ObjJStubBasedElement<*>>(@NonNls debugName: String, psiClass: Class<PsiT>) : IStubElementType<StubT, PsiT>(debugName, ObjJLanguage.instance) {

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
        return "objj." + toString()
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return true
    }

    override fun indexStub(stub: StubT, sink: IndexSink) {
        // do not force inheritors to implement this method
    }

    protected val indexService by lazy {
        ServiceManager.getService(StubIndexService::class.java)
    }

    protected fun shouldResolve(node: ASTNode): Boolean {
        val psiElement = node.psi
        val classDeclarationElement = ObjJPsiImplUtil.getContainingClass(psiElement) ?: return false
        return ObjJPsiImplUtil.shouldResolve(classDeclarationElement)
    }
}