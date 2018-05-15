package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJVarTypeIdImpl

interface ObjJVarTypeIdStub : StubElement<ObjJVarTypeIdImpl>, ObjJResolveableStub<ObjJVarTypeIdImpl> {
    val idType: String
}
