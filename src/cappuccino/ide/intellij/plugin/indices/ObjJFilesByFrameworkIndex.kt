package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import com.intellij.psi.stubs.StubIndexKey

class ObjJFilesByFrameworkIndex : ObjJStringStubIndexBase<ObjJFile>() {

    override val indexedElementClass: Class<ObjJFile>
        get() = ObjJFile::class.java

    override fun getKey(): StubIndexKey<String, ObjJFile> {
        return KEY
    }

    companion object {

        val instance = ObjJFilesByFrameworkIndex()
        val KEY = IndexKeyUtil.createIndexKey<String, ObjJFile>(ObjJFilesByFrameworkIndex::class.java)
    }
}
