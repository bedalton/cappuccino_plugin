package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.indices.IndexKeyUtil
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefTypeMapName
import com.intellij.psi.stubs.StubIndexKey

class JsTypeDefTypeMapByNamespaceIndex private constructor() : JsTypeDefStringStubIndexBase<JsTypeDefTypeMapName>() {

    override val indexedElementClass: Class<JsTypeDefTypeMapName>
        get() = JsTypeDefTypeMapName::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, JsTypeDefTypeMapName> {
        return KEY
    }

    companion object {

        val instance = JsTypeDefTypeMapByNamespaceIndex()

        private val KEY = IndexKeyUtil.createIndexKey(JsTypeDefTypeMapByNamespaceIndex::class.java)

        private const val VERSION = 1
    }


}
