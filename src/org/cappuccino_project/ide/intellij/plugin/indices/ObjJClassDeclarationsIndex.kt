package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement

class ObjJClassDeclarationsIndex private constructor() : ObjJStringStubIndexBase<ObjJClassDeclarationElement<*>>() {

    protected override val indexedElementClass: Class<ObjJClassDeclarationElement<*>>
        get() = ObjJClassDeclarationElement<*>::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJClassDeclarationElement<*>> {
        return KEY
    }

    companion object {

        val instance = ObjJClassDeclarationsIndex()
        private val KEY = IndexKeyUtil.createIndexKey<String, ObjJClassDeclarationElement>(ObjJClassDeclarationsIndex::class.java)
        private val VERSION = 1
    }

}
