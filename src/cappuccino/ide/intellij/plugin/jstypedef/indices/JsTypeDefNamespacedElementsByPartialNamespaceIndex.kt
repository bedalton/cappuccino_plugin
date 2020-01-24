package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.indices.IndexKeyUtil
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunction
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefHasNamespace
import com.intellij.psi.stubs.StubIndexKey

class JsTypeDefNamespacedElementsByPartialNamespaceIndex private constructor() : JsTypeDefStringStubIndexBase<JsTypeDefHasNamespace>() {

    override val indexedElementClass: Class<JsTypeDefHasNamespace>
        get() = JsTypeDefHasNamespace::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, JsTypeDefHasNamespace> {
        return KEY
    }

    companion object {

        val instance = JsTypeDefNamespacedElementsByPartialNamespaceIndex()

        private val KEY = IndexKeyUtil.createIndexKey(JsTypeDefNamespacedElementsByPartialNamespaceIndex::class.java)

        private const val VERSION = 1
    }


}
