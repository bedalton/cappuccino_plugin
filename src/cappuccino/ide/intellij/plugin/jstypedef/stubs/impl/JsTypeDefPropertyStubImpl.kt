package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefPropertyImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefPropertyStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypesList
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.psi.stubs.StubElement

class JsTypeDefPropertyStubImpl (
        parent:StubElement<*>,
        override val fileName: String,
        override val namespace: String,
        override val namespaceComponents: List<String>,
        override val propertyName: String,
        override val types: JsTypeDefTypesList
) : JsTypeDefStubBaseImpl<JsTypeDefPropertyImpl>(parent, JsTypeDefStubTypes.JS_PROPERTY), JsTypeDefPropertyStub {
    override val nullable: Boolean get() = types.nullable
}