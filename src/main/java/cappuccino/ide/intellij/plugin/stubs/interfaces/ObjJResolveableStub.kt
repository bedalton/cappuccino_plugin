package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement

interface ObjJResolveableStub<PsiT : StubBasedPsiElement<*>> : StubElement<PsiT> {
    fun shouldResolve(): Boolean
}
