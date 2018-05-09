package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJImportStatement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJStubBasedElement

interface ObjJImportStub<PsiT : ObjJImportStatement<ObjJImportStub<PsiT>>> : StubElement<PsiT> {
    val fileName: String
    val framework: String?
}
