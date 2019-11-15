package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJVariableTypeIdImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJVariableTypeIdStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement

class ObjJVariableTypeIdStubImpl(parent: StubElement<*>, override val idType: String, internal val shouldResolve: Boolean) : ObjJStubBaseImpl<ObjJVariableTypeIdImpl>(parent, ObjJStubTypes.VAR_TYPE_ID), ObjJVariableTypeIdStub {

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}
