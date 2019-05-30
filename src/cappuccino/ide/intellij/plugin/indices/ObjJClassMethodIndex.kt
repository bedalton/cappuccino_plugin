package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader

class ObjJClassMethodIndex private constructor() : ObjJStringStubIndexBase<ObjJMethodHeader>() {

    override val indexedElementClass: Class<ObjJMethodHeader>
        get() = ObjJMethodHeader::class.java

    override fun getKey(): StubIndexKey<String, ObjJMethodHeader> {
        return KEY
    }

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    companion object {
        private val KEY = IndexKeyUtil.createIndexKey(ObjJClassMethodIndex::class.java)
        val instance = ObjJClassMethodIndex()
        private const val VERSION = 3
    }


}
