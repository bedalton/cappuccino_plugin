package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement

interface ObjJClassDeclarationStub<PsiT : StubBasedPsiElement<*>> : StubElement<PsiT>, ObjJResolveableStub<PsiT> {

    val className: String
    val inheritedProtocols: List<String>

}