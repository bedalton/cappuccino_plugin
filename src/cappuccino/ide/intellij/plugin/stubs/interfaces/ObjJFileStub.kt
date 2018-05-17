package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.stubs.PsiFileStub
import cappuccino.ide.intellij.plugin.lang.ObjJFile

interface ObjJFileStub : PsiFileStub<ObjJFile> {
    //val imports: List<String>
    val fileName: String
    //fun getImportsForFramework(framework: String): List<String>
}