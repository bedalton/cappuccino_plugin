package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.StubIndexKey
import cappuccino.ide.intellij.plugin.psi.ObjJSelectorLiteral

class ObjJSelectorInferredMethodIndex : ObjJMethodHeaderDeclarationsIndexBase<ObjJSelectorLiteral>() {

    override val indexedElementClass: Class<ObjJSelectorLiteral>
        get() = ObjJSelectorLiteral::class.java

    override fun getKey(): StubIndexKey<String, ObjJSelectorLiteral> {
        return KEY
    }

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    companion object {
        val instance = ObjJSelectorInferredMethodIndex()
        private val KEY = IndexKeyUtil.createIndexKey(ObjJSelectorInferredMethodIndex::class.java)
        private const val VERSION = 2
    }
}
