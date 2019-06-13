package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration

class ObjJClassMethodIndex private constructor() : ObjJStringStubIndexBase<ObjJMethodHeaderDeclaration<*>>() {

    override val indexedElementClass: Class<ObjJMethodHeaderDeclaration<*>>
        get() = ObjJMethodHeaderDeclaration::class.java

    override fun getKey(): StubIndexKey<String, ObjJMethodHeaderDeclaration<*>> {
        return KEY
    }

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    companion object {
        val KEY = IndexKeyUtil.createIndexKey(ObjJClassMethodIndex::class.java)
        val instance = ObjJClassMethodIndex()
        private const val VERSION = 3
    }


}
