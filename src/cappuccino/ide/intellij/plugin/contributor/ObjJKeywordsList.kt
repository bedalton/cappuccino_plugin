package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.utils.ArrayUtils

object ObjJKeywordsList {

    val keywords = listOf("break", "do", "case", "else", "new", "var", "try", "catch", "finally", "return", "void", "for", "continue", "switch", "while", "debugger", "function", "if", "throw", "delete", "var", "objj_msgSend", "YES", "NO")

    fun search(queryString: String): List<String> {
        return ArrayUtils.search(keywords, queryString)
    }
}
