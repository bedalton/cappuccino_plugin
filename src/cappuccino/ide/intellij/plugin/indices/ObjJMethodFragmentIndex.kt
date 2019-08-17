package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import com.intellij.psi.stubs.StubIndexKey

class ObjJMethodFragmentIndex private constructor() : ObjJStringStubIndexBase<ObjJMethodHeaderDeclaration<*>>() {

    override val indexedElementClass: Class<ObjJMethodHeaderDeclaration<*>>
        get() = ObjJMethodHeaderDeclaration::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJMethodHeaderDeclaration<*>> {
        return KEY
    }

    companion object {
        val instance = ObjJMethodFragmentIndex()
        val KEY: StubIndexKey<String, ObjJMethodHeaderDeclaration<*>> = IndexKeyUtil.createIndexKey<String, ObjJMethodHeaderDeclaration<*>>(ObjJMethodFragmentIndex::class.java)
        private const val VERSION = 1
    }
}
