package cappuccino.ide.intellij.plugin.stubs.interfaces

import cappuccino.ide.intellij.plugin.psi.impl.ObjJInstanceVariableDeclarationImpl
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJMethodStruct
import com.intellij.psi.stubs.StubElement


interface ObjJInstanceVariableDeclarationStub : StubElement<ObjJInstanceVariableDeclarationImpl>, ObjJResolveableStub<ObjJInstanceVariableDeclarationImpl> {
    val containingClass: String
    val variableType: String
    val variableName: String
    val getter: String?
    val setter: String?
    val accessorStructs:List<ObjJMethodStruct>
}
