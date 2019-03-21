package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall

class ObjJMethodCallIndex private constructor() : ObjJStringStubIndexBase<ObjJMethodCall>() {

    override val indexedElementClass: Class<ObjJMethodCall>
        get() = ObjJMethodCall::class.java

    override fun getKey(): StubIndexKey<String, ObjJMethodCall> {
        return KEY
    }

    override fun getVersion(): Int {
        return ObjJIndexService.INDEX_VERSION + VERSION
    }

    companion object {
        private val KEY = IndexKeyUtil.createIndexKey(ObjJMethodCallIndex::class.java)
        val instance = ObjJMethodCallIndex()
        private const val VERSION = 1
    }
}
