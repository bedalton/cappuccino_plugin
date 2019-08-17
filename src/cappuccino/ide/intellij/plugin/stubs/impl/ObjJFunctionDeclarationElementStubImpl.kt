package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement

open class ObjJFunctionDeclarationElementStubImpl<PsiT : StubBasedPsiElement<*>> (parent: StubElement<*>, stubElementType: IStubElementType<*, *>, override val fileName: String, final override val fqName: String, override val paramNames: List<String>, override val returnType: String?, private val shouldResolve: Boolean, override val scope:ObjJFunctionScope) : ObjJStubBaseImpl<PsiT>(parent, stubElementType), ObjJFunctionDeclarationElementStub<PsiT> {
    final override val functionName: String

    override val numParams: Int
        get() = paramNames.size

    init {
        val lastDotIndex = fqName.lastIndexOf(".")
        this.functionName = fqName.substring(if (lastDotIndex >= 0) lastDotIndex else 0)
    }

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}
