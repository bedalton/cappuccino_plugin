package cappuccino.ide.intellij.plugin.settings

import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.StringSetting
import cappuccino.ide.intellij.plugin.utils.orFalse

class ObjJIgnoredStringsListSetting (val key:String, private val ignorePropertiesDefault:String = "") {
    private val ignoredKeywordsSetting = StringSetting(key, ignorePropertiesDefault)
    private var ignoredKeywords = ignoredKeywordsSetting.value!!.replace(" ".toRegex(), "").split(IGNORE_KEYWORDS_DELIM)

    fun ignoreKeyword(keyword:String) {
        if (ignoredKeywords.contains(keyword)) {
            return
        }
        ignoredKeywords = ignoredKeywords + keyword
        ignoreKeywords(ignoredKeywords.joinToString(IGNORE_KEYWORDS_DELIM))
    }

    fun removeIgnoredKeyword(keyword:String) {
        if (!ignoredKeywords.contains(keyword)) {
            return
        }
        ignoredKeywords = ignoredKeywords - keyword
        ignoreKeywords(ignoredKeywords.joinToString(IGNORE_KEYWORDS_DELIM))
    }

    fun ignoredKeywords() : List<String> {
        return ignoredKeywords
    }

    fun isIgnoredKeyword(keyword:String) : Boolean {
        return ignoredKeywords.contains(keyword)
    }

    fun ignoreKeywords(keywords:String) {
        ignoredKeywordsSetting.value = keywords
        ignoredKeywords = loadIgnoredKeywords()
    }

    private fun loadIgnoredKeywords() : MutableList<String> {
        val ignoredKeywords:MutableList<String> = ArrayList()
        val keywordsString:String = if (ignoredKeywordsSetting.value?.isNotEmpty().orFalse())
            ignoredKeywordsSetting.value!!
        else
            ignorePropertiesDefault

        for (keyword in keywordsString.split(IGNORE_KEYWORDS_DELIM)) {
            val trimmedKeyword = keyword.trim()
            if (trimmedKeyword.isEmpty()) {
                continue
            }
            ignoredKeywords.add(trimmedKeyword)
        }
        return ignoredKeywords
    }
    companion object {
        const val IGNORE_KEYWORDS_DELIM = ","
    }
}
