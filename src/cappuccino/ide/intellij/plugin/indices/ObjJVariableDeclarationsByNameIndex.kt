package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.ObjJVariableDeclaration

class ObjJVariableDeclarationsByNameIndex private constructor() : ObjJStringStubIndexBase<ObjJVariableDeclaration>() {

    override val indexedElementClass: Class<ObjJVariableDeclaration>
        get() = ObjJVariableDeclaration::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJVariableDeclaration> {
        return KEY
    }

    companion object {

        val instance = ObjJVariableDeclarationsByNameIndex()

        val KEY = IndexKeyUtil.createIndexKey(ObjJVariableDeclarationsByNameIndex::class.java)

        private const val VERSION = 6
    }


}
