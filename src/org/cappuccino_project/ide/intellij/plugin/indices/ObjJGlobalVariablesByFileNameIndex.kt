package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJGlobalVariableDeclaration
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJGlobalVariableDeclarationStubType

/**
 * Index to find global variables by file name
 */
class ObjJGlobalVariablesByFileNameIndex : ObjJStringStubIndexBase<ObjJGlobalVariableDeclaration>() {

    protected override val indexedElementClass: Class<ObjJGlobalVariableDeclaration>
        get() = ObjJGlobalVariableDeclaration::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJGlobalVariableDeclaration> {
        return KEY
    }

    companion object {

        val instance = ObjJGlobalVariablesByFileNameIndex()
        private val KEY = IndexKeyUtil.createIndexKey(ObjJGlobalVariablesByFileNameIndex::class.java)
        private val VERSION = ObjJGlobalVariableDeclarationStubType.VERSION
    }
}