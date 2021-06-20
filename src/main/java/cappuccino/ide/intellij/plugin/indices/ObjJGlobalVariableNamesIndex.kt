package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.ObjJGlobalVariableDeclaration
import cappuccino.ide.intellij.plugin.stubs.types.ObjJGlobalVariableDeclarationStubType
import com.intellij.psi.stubs.StubIndexKey

/**
 * Index to find global variables by their name.
 */
class ObjJGlobalVariableNamesIndex : ObjJStringStubIndexBase<ObjJGlobalVariableDeclaration>() {

    override val indexedElementClass: Class<ObjJGlobalVariableDeclaration>
        get() = ObjJGlobalVariableDeclaration::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJGlobalVariableDeclaration> {
        return KEY
    }

    companion object {

        val instance = ObjJGlobalVariableNamesIndex()
        private val KEY = IndexKeyUtil.createIndexKey(ObjJGlobalVariableNamesIndex::class.java)
        private const val VERSION = ObjJGlobalVariableDeclarationStubType.VERSION
    }
}
