package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration
import com.intellij.psi.stubs.StubIndexKey

class ObjJClassInstanceVariableAccessorMethodIndex : ObjJStringStubIndexBase<ObjJInstanceVariableDeclaration>() {

    override val indexedElementClass: Class<ObjJInstanceVariableDeclaration>
        get() = ObjJInstanceVariableDeclaration::class.java

    override fun getKey(): StubIndexKey<String, ObjJInstanceVariableDeclaration> {
        return KEY
    }

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    companion object {

        val instance = ObjJClassInstanceVariableAccessorMethodIndex()
        private val KEY = IndexKeyUtil.createIndexKey(ObjJClassInstanceVariableAccessorMethodIndex::class.java)
        private const val VERSION = 1
    }
}
