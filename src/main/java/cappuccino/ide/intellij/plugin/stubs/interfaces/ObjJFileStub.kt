package cappuccino.ide.intellij.plugin.stubs.interfaces

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJImportInfoStub
import com.intellij.psi.stubs.PsiFileStub

interface ObjJFileStub : PsiFileStub<ObjJFile> {
    val imports: List<ObjJImportInfoStub>
    val fileName: String
}