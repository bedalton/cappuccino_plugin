package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.stubs.PsiFileStub
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJImportInfoStub

interface ObjJFileStub : PsiFileStub<ObjJFile> {
    val imports: List<ObjJImportInfoStub>
    val fileName: String
}