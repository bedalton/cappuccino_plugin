package cappuccino.ide.intellij.plugin.stubs.interfaces

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListClass
import cappuccino.ide.intellij.plugin.psi.impl.ObjJObjectLiteralImpl
import com.intellij.psi.stubs.StubElement

interface ObjJObjectLiteralStub : StubElement<ObjJObjectLiteralImpl> {
    val objectWithoutInference: JsTypeListClass?
}