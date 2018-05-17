package cappuccino.ide.intellij.plugin.decompiler.lang

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.lang.Language
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiManager
import com.intellij.psi.compiled.ClassFileDecompilers
import com.intellij.psi.compiled.ClsStubBuilder

class ObjJSjClassFileDecompiler : ClassFileDecompilers.Full() {
    override fun getStubBuilder(): ClsStubBuilder {
        return ObjJSjClassStubBuilder()
    }

    override fun createFileViewProvider(
            virtualFile: VirtualFile,
            psiManager: PsiManager, b: Boolean): FileViewProvider {
        return ObjJSjFileViewProvider(psiManager, virtualFile, b)
    }

    override fun accepts(
            virtualFile: VirtualFile): Boolean {
        return virtualFile.extension == ObjJSjFileType.FILE_EXTENSION
    }
}
