package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import com.intellij.psi.stubs.StubIndexKey

class ObjJImplementationDeclarationsIndex private constructor() : ObjJStringStubIndexBase<ObjJImplementationDeclaration>() {

    override val indexedElementClass: Class<ObjJImplementationDeclaration>
        get() = ObjJImplementationDeclaration::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJImplementationDeclaration> {
        return KEY
    }

    companion object {

        val instance = ObjJImplementationDeclarationsIndex()
        private val KEY = IndexKeyUtil.createIndexKey(ObjJImplementationDeclarationsIndex::class.java)
        private const val VERSION = 3
    }

}
