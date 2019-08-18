package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefPropertyImpl
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.CompletionModifier
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefPropertyStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.psi.stubs.StubElement

class JsTypeDefPropertyStubImpl (
        parent:StubElement<*>,
        override val fileName: String,
        override val enclosingNamespace: String,
        override val enclosingClass: String?,
        override val namespaceComponents: List<String>,
        override val propertyName: String,
        override val types: InferenceResult,
        override val static:Boolean = false,
        override val completionModifier: CompletionModifier
) : JsTypeDefStubBaseImpl<JsTypeDefPropertyImpl>(parent, JsTypeDefStubTypes.JS_PROPERTY), JsTypeDefPropertyStub {
    override val nullable: Boolean get() = types.nullable
}