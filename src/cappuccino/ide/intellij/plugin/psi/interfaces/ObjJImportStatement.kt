package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImportStub

interface ObjJImportStatement<StubT : ObjJImportStub<*>> : ObjJStubBasedElement<StubT>, ObjJCompositeElement {

    val frameworkName: String?
    val fileName: String

    val importAsUnifiedString: String
        get() = (if (frameworkName != null) frameworkName else "") + DELIMITER + fileName

    companion object {

        const val DELIMITER = "::"
    }

}
