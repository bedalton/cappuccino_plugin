package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.indices.IndexKeyUtil
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefVariableDeclaration
import com.intellij.psi.stubs.StubIndexKey

class JsTypeDefVariableDeclarationsByNameIndex private constructor() : JsTypeDefStringStubIndexBase<JsTypeDefVariableDeclaration>() {

    override val indexedElementClass: Class<JsTypeDefVariableDeclaration>
        get() = JsTypeDefVariableDeclaration::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, JsTypeDefVariableDeclaration> {
        return KEY
    }

    companion object {

        val instance = JsTypeDefVariableDeclarationsByNameIndex()

        val KEY = IndexKeyUtil.createIndexKey(JsTypeDefVariableDeclarationsByNameIndex::class.java)

        private const val VERSION = 1
    }


}
