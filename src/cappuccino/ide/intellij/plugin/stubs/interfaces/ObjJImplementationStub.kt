package cappuccino.ide.intellij.plugin.stubs.interfaces

import cappuccino.ide.intellij.plugin.psi.impl.ObjJImplementationDeclarationImpl

interface ObjJImplementationStub : ObjJClassDeclarationStub<ObjJImplementationDeclarationImpl> {

    val superClassName: String?
    val categoryName: String?
    val isCategory: Boolean

}
