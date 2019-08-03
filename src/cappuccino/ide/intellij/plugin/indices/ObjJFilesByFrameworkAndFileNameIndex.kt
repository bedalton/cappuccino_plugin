package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJImportInfoStub
import com.intellij.psi.stubs.StubIndexKey

class ObjJFilesByFrameworkAndFileNameIndex : ObjJStringStubIndexBase<ObjJFile>() {

    override val indexedElementClass: Class<ObjJFile>
        get() = ObjJFile::class.java

    override fun getKey(): StubIndexKey<String, ObjJFile> {
        return KEY
    }

    companion object {
        val instance = ObjJFilesByFrameworkAndFileNameIndex()
        val KEY = IndexKeyUtil.createIndexKey<String, ObjJFile>(ObjJFilesByFrameworkAndFileNameIndex::class.java)

        fun getIndexKey(frameworkName:String?, fileName:String) : String {
            return "$frameworkName:::$fileName"
        }

        fun getIndexKey(import:ObjJImportInfoStub) : String {
            return getIndexKey(import.framework, import.fileName!!)
        }
    }
}
