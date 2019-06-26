package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefFunctionImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefFunctionStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypesList
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.psi.stubs.StubElement

class JsTypeDefFunctionStubImpl(
        parent:StubElement<*>,
        override val fileName:String,
        override val enclosingNamespace:String,
        override val functionName: String,
        override val parameters: List<JsTypeDefNamedProperty>,
        override val returnType: JsTypeDefTypesList,
        override val global:Boolean,
        override val static: Boolean

) : JsTypeDefStubBaseImpl<JsTypeDefFunctionImpl>(parent, JsTypeDefStubTypes.JS_FUNCTION), JsTypeDefFunctionStub {


}