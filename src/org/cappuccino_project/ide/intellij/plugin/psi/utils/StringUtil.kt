package org.cappuccino_project.ide.intellij.plugin.psi.utils

object StringUtil {

    fun startsAndEndsWith(string: String?, start: String?, end: String?): Boolean {
        return string != null && (start == null || string.startsWith(start)) && (end == null || string.endsWith(end))
    }
}
