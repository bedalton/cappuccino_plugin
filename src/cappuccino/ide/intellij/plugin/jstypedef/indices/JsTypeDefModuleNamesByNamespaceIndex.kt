package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.indices.IndexKeyUtil
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefModuleName
import com.intellij.psi.stubs.StubIndexKey

class JsTypeDefModuleNamesByNamespaceIndex private constructor() : JsTypeDefStringStubIndexBase<JsTypeDefModuleName>() {

    override val indexedElementClass: Class<JsTypeDefModuleName>
        get() = JsTypeDefModuleName::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, JsTypeDefModuleName> {
        return KEY
    }

    companion object {

        val instance = JsTypeDefModuleNamesByNamespaceIndex()

        private val KEY = IndexKeyUtil.createIndexKey(JsTypeDefModuleNamesByNamespaceIndex::class.java)

        private const val VERSION = 1
    }


}
