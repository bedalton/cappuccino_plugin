package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportElement
import com.intellij.psi.stubs.StubIndexKey

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
