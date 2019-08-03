package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.indices.IndexKeyUtil
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefKeyList
import com.intellij.psi.stubs.StubIndexKey

class JsTypeDefKeyListsByNameIndex private constructor() : JsTypeDefStringStubIndexBase<JsTypeDefKeyList>() {

    override val indexedElementClass: Class<JsTypeDefKeyList>
        get() = JsTypeDefKeyList::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, JsTypeDefKeyList> {
        return KEY
    }

    companion object {

        val instance = JsTypeDefKeyListsByNameIndex()

        private val KEY = IndexKeyUtil.createIndexKey(JsTypeDefKeyListsByNameIndex::class.java)

        private const val VERSION = 1
    }


}
