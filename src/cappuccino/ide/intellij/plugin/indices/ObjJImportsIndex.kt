package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportElement

class ObjJImportsIndex : ObjJStringStubIndexBase<ObjJImportElement<*>>() {

    override val indexedElementClass: Class<ObjJImportElement<*>>
        get() = ObjJImportElement::class.java

    override fun getKey(): StubIndexKey<String, ObjJImportElement<*>> {
        return KEY
    }

    companion object {

        val instance = ObjJImportsIndex()
        val KEY = IndexKeyUtil.createIndexKey<String, ObjJImportElement<*>>(ObjJImportsIndex::class.java)
    }
}
