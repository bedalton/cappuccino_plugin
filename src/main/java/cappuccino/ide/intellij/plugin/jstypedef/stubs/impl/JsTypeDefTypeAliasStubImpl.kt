package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefTypeAliasImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypeAliasStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.psi.stubs.StubElement

class JsTypeDefTypeAliasStubImpl (
        parent: StubElement<*>,
        override val fileName: String,
        override val typeName:String,
        override val types: InferenceResult,
        override val comment: String? = null
) : JsTypeDefStubBaseImpl<JsTypeDefTypeAliasImpl>(parent, JsTypeDefStubTypes.JS_TYPE_ALIAS), JsTypeDefTypeAliasStub