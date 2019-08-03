package cappuccino.ide.intellij.plugin.jstypedef.lang

import com.intellij.lang.Language

class JsTypeDefLanguage private constructor() : Language("JsTypeDef") {
    companion object {
        @JvmStatic
        val instance = JsTypeDefLanguage()
    }
}
