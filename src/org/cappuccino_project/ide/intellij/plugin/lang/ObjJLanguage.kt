package org.cappuccino_project.ide.intellij.plugin.lang

import com.intellij.lang.Language

class ObjJLanguage private constructor() : Language("ObjectiveJ") {
    companion object {
        val INSTANCE = ObjJLanguage()
    }
}
