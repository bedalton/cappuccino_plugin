package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import com.intellij.psi.stubs.StubIndexKey

class ObjJMethodCallIndex private constructor() : ObjJStringStubIndexBase<ObjJMethodCall>() {

    override val indexedElementClass: Class<ObjJMethodCall>
        get() = ObjJMethodCall::class.java

    override fun getKey(): StubIndexKey<String, ObjJMethodCall> {
        return KEY
    }

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    companion object {
        private val KEY = IndexKeyUtil.createIndexKey(ObjJMethodCallIndex::class.java)
        val instance = ObjJMethodCallIndex()
        private const val VERSION = 1
    }
}
