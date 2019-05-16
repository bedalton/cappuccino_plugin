package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.indices.IndexKeyUtil
import cappuccino.ide.intellij.plugin.indices.ObjJStringStubIndexBase
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.lang.ObjJFile

class JsTypeDefFilesByNameIndex private constructor() : ObjJStringStubIndexBase<JsTypeDefFile>() {

    override val indexedElementClass: Class<JsTypeDefFile>
        get() = JsTypeDefFile::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, JsTypeDefFile> {
        return KEY
    }

    companion object {

        val instance = JsTypeDefFilesByNameIndex()

        private val KEY = IndexKeyUtil.createIndexKey(JsTypeDefFilesByNameIndex::class.java)

        private const val VERSION = 3
    }


}
