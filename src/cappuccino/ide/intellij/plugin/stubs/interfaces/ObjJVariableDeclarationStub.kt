package cappuccino.ide.intellij.plugin.stubs.interfaces

import cappuccino.ide.intellij.plugin.psi.impl.ObjJVariableDeclarationImpl
import com.intellij.psi.stubs.StubElement

interface ObjJVariableDeclarationStub : StubElement<ObjJVariableDeclarationImpl> {
    val qualifiedNamesList:List<QualifiedReferenceStubComponents>
    val hasVarKeyword:Boolean
}