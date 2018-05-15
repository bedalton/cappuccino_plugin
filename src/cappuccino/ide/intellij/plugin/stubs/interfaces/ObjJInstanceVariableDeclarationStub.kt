package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJInstanceVariableDeclarationImpl


interface ObjJInstanceVariableDeclarationStub : StubElement<ObjJInstanceVariableDeclarationImpl>, ObjJResolveableStub<ObjJInstanceVariableDeclarationImpl> {
    val containingClass: String
    val varType: String
    val variableName: String
    val getter: String?
    val setter: String?
}
