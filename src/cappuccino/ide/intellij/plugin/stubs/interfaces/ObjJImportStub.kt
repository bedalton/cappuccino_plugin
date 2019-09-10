package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.StubElement

interface ObjJImportStub<PsiT : StubBasedPsiElement<*>> : StubElement<PsiT> {
    val fileName: String
    val framework: String?
}
