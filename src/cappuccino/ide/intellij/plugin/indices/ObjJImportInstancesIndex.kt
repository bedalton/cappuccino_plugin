package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportElement

/**
 * Indexes by format {Framework}/{Filename}
 * Used to find which files import any given file
 */
class ObjJImportInstancesIndex : ObjJStringStubIndexBase<ObjJImportElement<*>>() {

    override val indexedElementClass: Class<ObjJImportElement<*>>
        get() = ObjJImportElement::class.java

    override fun getKey(): StubIndexKey<String, ObjJImportElement<*>> {
        return KEY
    }

    companion object {
        val instance = ObjJImportInstancesIndex()
        val KEY = IndexKeyUtil.createIndexKey<String, ObjJImportElement<*>>(ObjJImportInstancesIndex::class.java)
    }
}
