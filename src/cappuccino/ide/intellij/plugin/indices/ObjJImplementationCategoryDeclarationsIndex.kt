package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration

class ObjJImplementationCategoryDeclarationsIndex private constructor() : ObjJStringStubIndexBase<ObjJImplementationDeclaration>() {

    override val indexedElementClass: Class<ObjJImplementationDeclaration>
        get() = ObjJImplementationDeclaration::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJImplementationDeclaration> {
        return KEY
    }

    companion object {

        val instance = ObjJImplementationCategoryDeclarationsIndex()
        private val KEY = IndexKeyUtil.createIndexKey(ObjJImplementationCategoryDeclarationsIndex::class.java)
        private const val VERSION = 1
    }

}
