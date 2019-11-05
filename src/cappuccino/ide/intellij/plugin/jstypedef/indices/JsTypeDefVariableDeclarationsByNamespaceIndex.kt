package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.indices.IndexKeyUtil
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefVariableDeclaration
import com.intellij.psi.stubs.StubIndexKey

class JsTypeDefVariableDeclarationsByNamespaceIndex private constructor() : JsTypeDefStringStubIndexBase<JsTypeDefVariableDeclaration>() {

    override val indexedElementClass: Class<JsTypeDefVariableDeclaration>
        get() = JsTypeDefVariableDeclaration::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, JsTypeDefVariableDeclaration> {
        return KEY
    }

    companion object {

        val instance = JsTypeDefVariableDeclarationsByNamespaceIndex()

        internal val KEY = IndexKeyUtil.createIndexKey(JsTypeDefVariableDeclarationsByNamespaceIndex::class.java)

        private const val VERSION = 1
    }


}
