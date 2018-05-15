package cappuccino.ide.intellij.plugin.stubs.interfaces

import cappuccino.ide.intellij.plugin.psi.impl.ObjJAccessorPropertyImpl


interface ObjJAccessorPropertyStub : ObjJMethodHeaderDeclarationStub<ObjJAccessorPropertyImpl>, ObjJResolveableStub<ObjJAccessorPropertyImpl> {
    val containingClass: String
    val variableName: String?
    val getter: String?
    val setter: String?
    val varType: String?
}
