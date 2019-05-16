package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefFunctionImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefFunctionStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefFunctionStubParameter
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefFunctionStubReturnType
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.psi.stubs.StubElement

class JsTypeDefFunctionStubImpl(
        parent:StubElement<*>,
        override val fileName:String,
        override val moduleName:String,
        override val functionName: String,
        override val parameters: List<JsTypeDefFunctionStubParameter>,
        override val returnType: JsTypeDefFunctionStubReturnType,
        override val global:Boolean

) : JsTypeDefStubBaseImpl<JsTypeDefFunctionImpl>(parent, JsTypeDefStubTypes.JS_FUNCTION), JsTypeDefFunctionStub {


}