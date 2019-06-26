package cappuccino.ide.intellij.plugin.stubs.interfaces

import cappuccino.ide.intellij.plugin.psi.impl.ObjJObjectLiteralImpl
import cappuccino.ide.intellij.plugin.psi.utils.JsObjectType
import com.intellij.psi.stubs.StubElement

interface ObjJObjectLiteralStub : StubElement<ObjJObjectLiteralImpl> {
    val objectWithoutInference:JsObjectType?
}