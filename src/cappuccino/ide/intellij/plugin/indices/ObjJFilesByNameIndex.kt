package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.lang.ObjJFile

class ObjJFilesByNameIndex private constructor() : ObjJStringStubIndexBase<ObjJFile>() {

    override val indexedElementClass: Class<ObjJFile>
        get() = ObjJFile::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJFile> {
        return KEY
    }

    companion object {

        val instance = ObjJFilesByNameIndex()

        private val KEY = IndexKeyUtil.createIndexKey(ObjJFilesByNameIndex::class.java)

        private const val VERSION = 3
    }


}
