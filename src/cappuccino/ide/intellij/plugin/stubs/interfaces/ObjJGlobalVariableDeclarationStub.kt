package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJGlobalVariableDeclarationImpl

interface ObjJGlobalVariableDeclarationStub : StubElement<ObjJGlobalVariableDeclarationImpl> {

    val fileName: String?
    val variableName: String
    val variableType: String?

}
