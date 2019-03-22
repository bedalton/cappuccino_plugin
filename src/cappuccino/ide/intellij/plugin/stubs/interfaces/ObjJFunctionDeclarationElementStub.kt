package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement

interface ObjJFunctionDeclarationElementStub<PsiT : StubBasedPsiElement<*>> : StubElement<PsiT>, ObjJResolveableStub<PsiT> {
    val fileName: String
    val fqName: String
    val functionName: String
    val numParams: Int
    val paramNames: List<String>
    val returnType: String?
}