package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import com.intellij.psi.stubs.StubIndexKey

class ObjJClassDeclarationsIndex private constructor() : ObjJStringStubIndexBase<ObjJClassDeclarationElement<*>>() {

    override val indexedElementClass: Class<ObjJClassDeclarationElement<*>>
        get() = ObjJClassDeclarationElement::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJClassDeclarationElement<*>> {
        return KEY
    }

    companion object {

        val instance = ObjJClassDeclarationsIndex()
        private val KEY = IndexKeyUtil.createIndexKey<String, ObjJClassDeclarationElement<*>>(ObjJClassDeclarationsIndex::class.java)
        private const val VERSION = 1
    }

}
