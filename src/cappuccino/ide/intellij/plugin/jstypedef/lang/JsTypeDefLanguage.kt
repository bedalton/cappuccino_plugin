package cappuccino.ide.intellij.plugin.jstypedef.lang

import com.intellij.lang.Language

class JsTypeDefLanguage private constructor() : Language("ObjectiveJ") {
    companion object {
        @JvmStatic
        val instance = JsTypeDefLanguage()
    }
}
