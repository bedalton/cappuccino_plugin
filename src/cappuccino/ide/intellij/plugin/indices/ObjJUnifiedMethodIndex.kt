package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration

class ObjJUnifiedMethodIndex private constructor() : ObjJMethodHeaderDeclarationsIndexBase<ObjJMethodHeaderDeclaration<*>>() {

    override val indexedElementClass: Class<ObjJMethodHeaderDeclaration<*>>
        get() = ObjJMethodHeaderDeclaration::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJMethodHeaderDeclaration<*>> {
        return KEY
    }

    companion object {
        val KEY: StubIndexKey<String, ObjJMethodHeaderDeclaration<*>> = IndexKeyUtil.createIndexKey<String, ObjJMethodHeaderDeclaration<*>>(ObjJUnifiedMethodIndex::class.java)
        val instance = ObjJUnifiedMethodIndex()
        private const val VERSION = 2
    }
}
