package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportStub

interface ObjJImportStatement<StubT : ObjJImportStub<out ObjJImportStatement<*>>> : ObjJStubBasedElement<StubT>, ObjJCompositeElement {

    val frameworkName: String?
    val fileName: String

    val importAsUnifiedString: String
        get() = (if (frameworkName != null) frameworkName else "") + DELIMITER + fileName

    companion object {

        val DELIMITER = "::"
    }

}
