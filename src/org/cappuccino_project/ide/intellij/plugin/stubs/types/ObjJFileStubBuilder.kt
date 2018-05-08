package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService

class ObjJFileStubBuilder : DefaultStubBuilder() {
    override fun createStubForFile(file: PsiFile): StubElement<*> {
        return if (file !is ObjJFile) {
            super.createStubForFile(file)
        } else StubIndexService.instance.createFileStub(file)
    }
}
