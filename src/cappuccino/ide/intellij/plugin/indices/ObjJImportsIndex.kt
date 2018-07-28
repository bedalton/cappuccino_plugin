package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportStatement

class ObjJImportsIndex : ObjJStringStubIndexBase<ObjJImportStatement<*>>() {

    protected override val indexedElementClass: Class<ObjJImportStatement<*>>
        get() = ObjJImportStatement::class.java

    override fun getKey(): StubIndexKey<String, ObjJImportStatement<*>> {
        return KEY
    }

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    companion object {

        val instance = ObjJImportsIndex()
        private const val VERSION = 1
        private val KEY = IndexKeyUtil.createIndexKey<String, ObjJImportStatement<*>>(ObjJImportsIndex::class.java)
    }
}
