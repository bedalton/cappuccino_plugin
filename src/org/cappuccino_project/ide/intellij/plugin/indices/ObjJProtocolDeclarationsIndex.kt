package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJProtocolDeclaration

class ObjJProtocolDeclarationsIndex private constructor() : ObjJStringStubIndexBase<ObjJProtocolDeclaration>() {

    protected override val indexedElementClass: Class<ObjJProtocolDeclaration>
        get() = ObjJProtocolDeclaration::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJProtocolDeclaration> {
        return KEY
    }

    companion object {

        val instance = ObjJProtocolDeclarationsIndex()
        private val KEY = IndexKeyUtil.createIndexKey(ObjJProtocolDeclarationsIndex::class.java)
        private val VERSION = 1
    }

}
