package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.indices.IndexKeyUtil
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefModule
import com.intellij.psi.stubs.StubIndexKey

class JsTypeDefModulesByNameIndex private constructor() : JsTypeDefStringStubIndexBase<JsTypeDefModule>() {

    override val indexedElementClass: Class<JsTypeDefModule>
        get() = JsTypeDefModule::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, JsTypeDefModule> {
        return KEY
    }

    companion object {

        val instance = JsTypeDefModulesByNameIndex()

        private val KEY = IndexKeyUtil.createIndexKey(JsTypeDefModulesByNameIndex::class.java)

        private const val VERSION = 1
    }


}
