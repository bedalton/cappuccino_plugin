package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.stubs.StubElement
import com.intellij.psi.StubBasedPsiElement

interface ObjJImportStub<PsiT : StubBasedPsiElement<*>> : StubElement<PsiT> {
    val fileName: String
    val framework: String?
}
