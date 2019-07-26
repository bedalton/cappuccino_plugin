package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.indices.IndexKeyUtil
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefTypeAlias
import com.intellij.psi.stubs.StubIndexKey

class JsTypeDefTypeAliasIndex private constructor() : JsTypeDefStringStubIndexBase<JsTypeDefTypeAlias>() {

    override val indexedElementClass: Class<JsTypeDefTypeAlias>
        get() = JsTypeDefTypeAlias::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, JsTypeDefTypeAlias> {
        return KEY
    }

    companion object {

        val instance = JsTypeDefTypeAliasIndex()

        val KEY = IndexKeyUtil.createIndexKey(JsTypeDefTypeAliasIndex::class.java)

        private const val VERSION = 1
    }


}
