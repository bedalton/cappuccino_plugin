package cappuccino.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJInstanceVariableDeclarationImpl
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJInstanceVariableDeclarationStub
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJMethodStruct
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes

class ObjJInstanceVariableDeclarationStubImpl(parent: StubElement<*>, containingClass: String?, override val variableType: String, override val variableName: String, getter: String?, setter: String?, override val accessorStructs: List<ObjJMethodStruct>, private val shouldResolve: Boolean) : ObjJStubBaseImpl<ObjJInstanceVariableDeclarationImpl>(parent, ObjJStubTypes.INSTANCE_VAR), ObjJInstanceVariableDeclarationStub {

    override val containingClass: String = containingClass ?: ObjJClassType.UNDEF_CLASS_NAME
    override val getter: String? = if (getter != null && getter.isNotEmpty()) getter else null
    override val setter: String? = if (setter != null && setter.isNotEmpty()) setter else null

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }

}
