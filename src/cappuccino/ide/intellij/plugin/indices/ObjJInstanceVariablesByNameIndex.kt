package cappuccino.ide.intellij.plugin.indices

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration
import cappuccino.ide.intellij.plugin.stubs.ObjJStubVersions

import java.util.ArrayList

class ObjJInstanceVariablesByNameIndex : ObjJStringStubIndexBase<ObjJInstanceVariableDeclaration>() {

    protected override val indexedElementClass: Class<ObjJInstanceVariableDeclaration>
        get() = ObjJInstanceVariableDeclaration::class.java

    override fun getKey(): StubIndexKey<String, ObjJInstanceVariableDeclaration> {
        return KEY
    }

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    companion object {

        val instance = ObjJInstanceVariablesByNameIndex()
        private val KEY = IndexKeyUtil.createIndexKey(ObjJInstanceVariablesByNameIndex::class.java)
        private val VERSION = 1
    }
}
