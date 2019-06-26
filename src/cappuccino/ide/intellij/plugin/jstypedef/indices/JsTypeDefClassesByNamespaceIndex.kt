package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.indices.IndexKeyUtil
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefInterfaceElement
import com.intellij.psi.stubs.StubIndexKey

class JsTypeDefClassesByNamespaceIndex private constructor() : JsTypeDefStringStubIndexBase<JsTypeDefInterfaceElement>() {

    override val indexedElementClass: Class<JsTypeDefInterfaceElement>
        get() = JsTypeDefInterfaceElement::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, JsTypeDefInterfaceElement> {
        return KEY
    }

    companion object {

        val instance = JsTypeDefClassesByNamespaceIndex()

        val KEY = IndexKeyUtil.createIndexKey(JsTypeDefClassesByNamespaceIndex::class.java)

        private const val VERSION = 1
    }


}
