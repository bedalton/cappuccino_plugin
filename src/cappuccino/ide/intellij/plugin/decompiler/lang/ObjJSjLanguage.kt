package cappuccino.ide.intellij.plugin.decompiler.lang

import com.intellij.lang.Language

class ObjJSjLanguage private constructor() : Language("ObjJSj") {
    companion object {
        val INSTANCE = ObjJSjLanguage()
    }
}
