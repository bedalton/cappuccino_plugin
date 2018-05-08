package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJImportStatement

class ObjJImportsIndex : ObjJStringStubIndexBase<ObjJImportStatement<*>>() {

    protected override val indexedElementClass: Class<ObjJImportStatement<*>>
        get() = ObjJImportStatement<*>::class.java

    override fun getKey(): StubIndexKey<String, ObjJImportStatement<*>> {
        return KEY
    }

    companion object {

        val instance = ObjJImportsIndex()
        private val KEY = IndexKeyUtil.createIndexKey<String, ObjJImportStatement>(ObjJImportsIndex::class.java)
    }
}
