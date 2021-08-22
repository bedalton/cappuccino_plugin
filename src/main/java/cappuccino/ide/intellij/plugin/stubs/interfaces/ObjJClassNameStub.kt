package cappuccino.ide.intellij.plugin.stubs.interfaces

import cappuccino.ide.intellij.plugin.psi.impl.ObjJClassNameImpl
import com.intellij.psi.stubs.StubElement

interface ObjJClassNameStub : StubElement<ObjJClassNameImpl> {
    val classNameString : String
}
