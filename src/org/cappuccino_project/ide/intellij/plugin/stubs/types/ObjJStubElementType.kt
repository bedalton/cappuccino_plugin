package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.util.ArrayFactory
import com.intellij.util.ReflectionUtil
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJStubBasedElement
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil
import org.jetbrains.annotations.NonNls

import java.lang.reflect.Array
import java.lang.reflect.Constructor
import java.util.logging.Level
import java.util.logging.Logger


abstract class ObjJStubElementType<StubT : StubElement<*>, PsiT : ObjJStubBasedElement<*>>(@NonNls debugName: String, psiClass: Class<PsiT>, stubClass: Class<*>) : IStubElementType<StubT, PsiT>(debugName, ObjJLanguage.INSTANCE) {

    private val byNodeConstructor: Constructor<PsiT>
    private val emptyArray: Array<PsiT>
    val arrayFactory: ArrayFactory<PsiT>

    init {
        try {
            byNodeConstructor = psiClass.getConstructor(ASTNode::class.java)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException("Stub element type declaration for " + psiClass.simpleName + " is missing required constructors", e)
        }


        emptyArray = Array.newInstance(psiClass, 0) as Array<PsiT>
        arrayFactory = { count ->
            if (count == 0) {
                return emptyArray
            }

            Array.newInstance(psiClass, count) as Array<PsiT>
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

    protected fun shouldResolve(node: ASTNode): Boolean {
        val psiElement = node.psi
        val classDeclarationElement = ObjJPsiImplUtil.getContainingClass(psiElement) ?: return false
        return ObjJPsiImplUtil.shouldResolve(classDeclarationElement)
    }
}