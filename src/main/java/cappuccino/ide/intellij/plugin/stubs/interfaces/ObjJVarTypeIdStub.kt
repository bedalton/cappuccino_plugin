package cappuccino.ide.intellij.plugin.stubs.interfaces

import cappuccino.ide.intellij.plugin.psi.impl.ObjJVariableTypeIdImpl
import com.intellij.psi.stubs.StubElement

interface ObjJVariableTypeIdStub : StubElement<ObjJVariableTypeIdImpl>, ObjJResolveableStub<ObjJVariableTypeIdImpl> {
    val idType: String
}
