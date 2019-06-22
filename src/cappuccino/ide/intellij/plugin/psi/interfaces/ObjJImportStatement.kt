package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImportStub

interface ObjJImportStatement<StubT : ObjJImportStub<*>> : ObjJStubBasedElement<StubT>, ObjJCompositeElement {

    val frameworkNameString: String
    val fileNameString: String

    val importAsUnifiedString: String
        get() = (if (frameworkNameString != null) frameworkNameString else "") + DELIMITER + fileNameString

    companion object {
        const val ROOT_FRAMEWORK = "ROOT";
        const val DELIMITER = "::"
    }

}
