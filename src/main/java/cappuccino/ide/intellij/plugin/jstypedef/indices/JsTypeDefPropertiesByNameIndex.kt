package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.indices.IndexKeyUtil
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import com.intellij.psi.stubs.StubIndexKey

class JsTypeDefPropertiesByNameIndex private constructor() : JsTypeDefStringStubIndexBase<JsTypeDefProperty>() {

    override val indexedElementClass: Class<JsTypeDefProperty>
        get() = JsTypeDefProperty::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, JsTypeDefProperty> {
        return KEY
    }

    companion object {

        val instance = JsTypeDefPropertiesByNameIndex()

        private val KEY = IndexKeyUtil.createIndexKey(JsTypeDefPropertiesByNameIndex::class.java)

        private const val VERSION = 1
    }


}
