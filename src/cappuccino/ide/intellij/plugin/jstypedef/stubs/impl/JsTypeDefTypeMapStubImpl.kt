package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefTypeMapEntry
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefTypeMapElementImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypeMapStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.psi.stubs.StubElement

class JsTypeDefTypeMapStubImpl(
        parent:StubElement<*>,
        override val fileName:String,
        override val mapName: String,
        override val values:List<JsTypeDefTypeMapEntry>
) : JsTypeDefStubBaseImpl<JsTypeDefTypeMapElementImpl>(parent, JsTypeDefStubTypes.JS_TYPE_MAP), JsTypeDefTypeMapStub {

    /**
     * Get All types for a given key
     */
    override fun getTypesForKey(key: String): InferenceResult {
        val types = values.filter { it.key == key }.map { it.types }
        val nullable = types.any { it.nullable }
        return InferenceResult(
                types = types.flatMap { it.types }.toSet(),
                nullable = nullable
        )
    }
}