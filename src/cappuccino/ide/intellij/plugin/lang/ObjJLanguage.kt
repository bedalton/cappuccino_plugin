package cappuccino.ide.intellij.plugin.lang

import com.intellij.lang.Language

class ObjJLanguage private  constructor() : Language("ObjectiveJ") {
    companion object {
        @JvmStatic
        val instance = ObjJLanguage()
    }
}
