package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionLiteral
import cappuccino.ide.intellij.plugin.psi.impl.ObjJFunctionDeclarationImpl
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJResolveableElement

interface ObjJFunctionDeclarationElementStub<PsiT : StubBasedPsiElement<*>> : StubElement<PsiT>, ObjJResolveableStub<PsiT> {
    val fileName: String
    val fqName: String
    val functionName: String
    val numParams: Int
    val paramNames: List<String>
    val returnType: String?
}