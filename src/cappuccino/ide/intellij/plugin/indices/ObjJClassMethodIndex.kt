package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration

class ObjJClassMethodIndex private constructor() : ObjJStringStubIndexBase<ObjJMethodHeader>() {

    protected override val indexedElementClass: Class<ObjJMethodHeader>
        get() = ObjJMethodHeader::class.java

    override fun getKey(): StubIndexKey<String, ObjJMethodHeader> {
        return KEY
    }

    override fun getVersion(): Int {
        return ObjJIndexService.INDEX_VERSION + VERSION
    }

    companion object {
        private val KEY = IndexKeyUtil.createIndexKey(ObjJClassMethodIndex::class.java)
        val instance = ObjJClassMethodIndex()
        private val VERSION = 3
    }


}
