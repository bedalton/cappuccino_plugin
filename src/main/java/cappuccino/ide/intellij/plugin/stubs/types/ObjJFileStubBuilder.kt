package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.indices.StubIndexService
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.StubElement

class ObjJFileStubBuilder : DefaultStubBuilder() {
    override fun createStubForFile(file: PsiFile): StubElement<*> {
        return if (file !is ObjJFile) {
            super.createStubForFile(file)
        } else StubIndexService.instance.createFileStub(file)
    }
}
