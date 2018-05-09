package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration

class ObjJMethodFragmentIndex private constructor() : ObjJStringStubIndexBase<ObjJMethodHeaderDeclaration<*>>() {

    protected override val indexedElementClass: Class<ObjJMethodHeaderDeclaration<*>>
        get() = ObjJMethodHeaderDeclaration::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJMethodHeaderDeclaration<*>> {
        return KEY
    }

    companion object {
        val instance = ObjJMethodFragmentIndex()
        val KEY: StubIndexKey<String, ObjJMethodHeaderDeclaration<*>> = IndexKeyUtil.createIndexKey<String, ObjJMethodHeaderDeclaration<*>>(ObjJMethodFragmentIndex::class.java)
        private val VERSION = 0
    }
}
