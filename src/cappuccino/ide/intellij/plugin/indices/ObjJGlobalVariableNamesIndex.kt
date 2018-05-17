package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.ObjJGlobalVariableDeclaration
import cappuccino.ide.intellij.plugin.stubs.types.ObjJGlobalVariableDeclarationStubType

/**
 * Index to find global variables by their name.
 */
class ObjJGlobalVariableNamesIndex : ObjJStringStubIndexBase<ObjJGlobalVariableDeclaration>() {

    protected override val indexedElementClass: Class<ObjJGlobalVariableDeclaration>
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
        private val VERSION = ObjJGlobalVariableDeclarationStubType.VERSION
    }
}
