package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall

class ObjJMethodCallIndex private constructor() : ObjJStringStubIndexBase<ObjJMethodCall>() {

    protected override val indexedElementClass: Class<ObjJMethodCall>
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
        private val VERSION = 1
    }
}
