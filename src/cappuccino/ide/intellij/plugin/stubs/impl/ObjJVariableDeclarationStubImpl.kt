package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJVariableDeclarationImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJVariableDeclarationStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.QualifiedReferenceStubComponents
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement

class ObjJVariableDeclarationStubImpl(parent: StubElement<*>, override val qualifiedNameParts: List<QualifiedReferenceStubComponents>) : ObjJStubBaseImpl<ObjJVariableDeclarationImpl>(parent, ObjJStubTypes.VARIABLE_DECLARATION), ObjJVariableDeclarationStub