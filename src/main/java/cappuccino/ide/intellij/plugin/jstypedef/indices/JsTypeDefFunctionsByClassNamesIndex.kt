package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.indices.IndexKeyUtil
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunction
import com.intellij.psi.stubs.StubIndexKey

class JsTypeDefFunctionsByClassNamesIndex private constructor() : JsTypeDefStringStubIndexBase<JsTypeDefFunction>() {

    override val indexedElementClass: Class<JsTypeDefFunction>
        get() = JsTypeDefFunction::class.java

    override fun getVersion(): Int {
        return super.getVersion() + VERSION
    }

    override fun getKey(): StubIndexKey<String, JsTypeDefFunction> {
        return KEY
    }

    companion object {

        val instance = JsTypeDefFunctionsByClassNamesIndex()

        private val KEY = IndexKeyUtil.createIndexKey(JsTypeDefFunctionsByClassNamesIndex::class.java)

        private const val VERSION = 1
    }


}
