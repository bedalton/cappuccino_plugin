package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.ObjJTypeDef
import com.intellij.psi.stubs.StubIndexKey

class ObjJTypeDefIndex : ObjJStringStubIndexBase<ObjJTypeDef>() {

    override val indexedElementClass: Class<ObjJTypeDef>
        get() = ObjJTypeDef::class.java

    override fun getKey(): StubIndexKey<String, ObjJTypeDef> {
        return KEY
    }

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    companion object {

        private const val VERSION = 3
        val instance = ObjJTypeDefIndex()
        val KEY = IndexKeyUtil.createIndexKey<String, ObjJTypeDef>(ObjJTypeDefIndex::class.java)
    }
}
