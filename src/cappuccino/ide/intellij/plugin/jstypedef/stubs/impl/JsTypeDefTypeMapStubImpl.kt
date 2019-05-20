package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefTypeMapImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypeMapEntry
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypeMapStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefTypesList
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.psi.stubs.StubElement

class JsTypeDefTypeMapStubImpl(
        parent:StubElement<*>,
        override val fileName:String,
        override val mapName: String,
        override val values:List<JsTypeDefTypeMapEntry>
) : JsTypeDefStubBaseImpl<JsTypeDefTypeMapImpl>(parent, JsTypeDefStubTypes.JS_TYPE_MAP), JsTypeDefTypeMapStub {

    /**
     * Get All types for a given key
     */
    override fun getTypesForKey(key: String): JsTypeDefTypesList {
        val types = values.filter { it.key == key }.map { it.types }
        val nullable = types.any { it.nullable }
        return JsTypeDefTypesList(
                types = types.flatMap { it.types }.toSet(),
                nullable = nullable
        )
    }
}