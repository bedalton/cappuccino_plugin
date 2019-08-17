package cappuccino.ide.intellij.plugin.stubs.interfaces

import cappuccino.ide.intellij.plugin.psi.impl.ObjJVarTypeIdImpl
import com.intellij.psi.stubs.StubElement

interface ObjJVarTypeIdStub : StubElement<ObjJVarTypeIdImpl>, ObjJResolveableStub<ObjJVarTypeIdImpl> {
    val idType: String
}
