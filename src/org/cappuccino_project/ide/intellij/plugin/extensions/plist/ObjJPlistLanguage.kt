package org.cappuccino_project.ide.intellij.plugin.extensions.plist

import com.intellij.lang.Language

class ObjJPlistLanguage private constructor() : Language("ObjJPlist") {
    companion object {
        val INSTANCE = ObjJPlistLanguage()
    }
}