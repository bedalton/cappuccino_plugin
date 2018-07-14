package cappuccino.ide.intellij.plugin.settings

import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.StringSetting

class ObjJIgnoredStringsListSetting (val key:String, ignorePropertiesDefault:String = "") {
    private val ignoredKeywordsSetting = StringSetting(key, ignorePropertiesDefault)
    private var ignoredKeywords = ignoredKeywordsSetting.value!!.split(IGNORE_KEYWORDS_DELIM)

    fun ignoreKeyword(keyword:String) {
        if (ignoredKeywords.contains(keyword)) {
            return;
        }
        ignoredKeywords += keyword;
        ignoredKeywords(ignoredKeywords.joinToString(IGNORE_KEYWORDS_DELIM))
    }

    fun removeIgnoredKeyword(keyword:String) {
        if (!ignoredKeywords.contains(keyword)) {
            return;
        }
        ignoredKeywords -= keyword;
        ignoredKeywords(ignoredKeywords.joinToString(IGNORE_KEYWORDS_DELIM))
    }

    fun ignoredKeywords() : List<String> {
        return ignoredKeywords
    }

    fun isIgnoredKeyword(keyword:String) : Boolean {
        return ignoredKeywords.contains(keyword);
    }

    fun ignoredKeywords(keywords:String) {
        ignoredKeywordsSetting.value = keywords
        ignoredKeywords = loadIgnoredKeywords();
    }

    private fun loadIgnoredKeywords() : MutableList<String> {
        val ignoredKeywords:MutableList<String> = ArrayList()
        val keywordsString:String = ignoredKeywordsSetting.value ?: "";
        for (keyword in keywordsString.split(IGNORE_KEYWORDS_DELIM)) {
            val trimmedKeyword = keyword.trim();
            if (trimmedKeyword.isEmpty()) {
                continue;
            }
            ignoredKeywords.add(trimmedKeyword)
        }
        return ignoredKeywords;
    }

    fun asString() : String {
        return ignoredKeywordsSetting.value!!
    }

    companion object {
        private const val IGNORE_KEYWORDS_DELIM = ","
    }
}
