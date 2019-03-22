package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.utils.ArrayUtils

object ObjJKeywordsList {

    val keywords = listOf("break", "do", "case", "else", "new", "var", "catch", "finally", "return", "void", "for", "continue", "switch", "while", "debugger", "function", "if", "throw", "delete", "var", "objj_msgSend", "YES", "yes", "NO", "no")

    fun search(queryString: String): List<String> {
        return ArrayUtils.search(keywords, queryString)
    }
}
