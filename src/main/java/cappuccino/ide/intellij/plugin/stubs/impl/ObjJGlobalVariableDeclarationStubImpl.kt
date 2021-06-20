package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJGlobalVariableDeclarationImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJGlobalVariableDeclarationStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement

class ObjJGlobalVariableDeclarationStubImpl(parent: StubElement<*>, override val fileName: String?, override val variableName: String, override val variableType: String?) : ObjJStubBaseImpl<ObjJGlobalVariableDeclarationImpl>(parent, ObjJStubTypes.GLOBAL_VARIABLE), ObjJGlobalVariableDeclarationStub
