package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJImplementationDeclarationImpl
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement

interface ObjJClassDeclarationStub<PsiT : StubBasedPsiElement<*>> : StubElement<PsiT>, ObjJResolveableStub<PsiT> {

    val className: String
    val inheritedProtocols: List<String>

}