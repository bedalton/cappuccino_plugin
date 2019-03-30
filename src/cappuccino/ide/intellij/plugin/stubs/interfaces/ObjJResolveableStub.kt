package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.stubs.StubElement
import com.intellij.psi.StubBasedPsiElement

interface ObjJResolveableStub<PsiT : StubBasedPsiElement<*>> : StubElement<PsiT> {
    fun shouldResolve(): Boolean
}
