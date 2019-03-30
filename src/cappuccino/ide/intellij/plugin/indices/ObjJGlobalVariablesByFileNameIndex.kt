package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.ObjJGlobalVariableDeclaration
import cappuccino.ide.intellij.plugin.stubs.types.ObjJGlobalVariableDeclarationStubType

/**
 * Index to find global variables by file name
 */
class ObjJGlobalVariablesByFileNameIndex : ObjJStringStubIndexBase<ObjJGlobalVariableDeclaration>() {

    override val indexedElementClass: Class<ObjJGlobalVariableDeclaration>
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
        private const val VERSION = ObjJGlobalVariableDeclarationStubType.VERSION
    }
}