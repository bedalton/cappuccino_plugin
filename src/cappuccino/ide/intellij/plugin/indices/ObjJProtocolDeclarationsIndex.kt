package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration

class ObjJProtocolDeclarationsIndex private constructor() : ObjJStringStubIndexBase<ObjJProtocolDeclaration>() {

    override val indexedElementClass: Class<ObjJProtocolDeclaration>
        get() = ObjJProtocolDeclaration::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, ObjJProtocolDeclaration> {
        return KEY
    }

    companion object {

        val instance = ObjJProtocolDeclarationsIndex()
        private val KEY = IndexKeyUtil.createIndexKey(ObjJProtocolDeclarationsIndex::class.java)
        private const val VERSION = 1
    }

}
