package cappuccino.ide.intellij.plugin.stubs.interfaces

import cappuccino.ide.intellij.plugin.psi.impl.ObjJGlobalVariableDeclarationImpl
import com.intellij.psi.stubs.StubElement

interface ObjJGlobalVariableDeclarationStub : StubElement<ObjJGlobalVariableDeclarationImpl> {

    val fileName: String?
    val variableName: String
    val variableType: String?

}
