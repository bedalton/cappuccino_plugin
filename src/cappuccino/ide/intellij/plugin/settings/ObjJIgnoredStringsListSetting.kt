package cappuccino.ide.intellij.plugin.settings

import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.StringSetting

class ObjJIgnoredStringsListSetting (val key:String, private val requiredEnd:String? = null) {
    private val ignoredKeywordsSetting = StringSetting(key, "")
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
        var fixed:Boolean = false
        for (keyword in keywordsString.split(IGNORE_KEYWORDS_DELIM)) {
            var trimmedKeyword = keyword.trim();
            if (trimmedKeyword.isEmpty()) {
                fixed = true
                continue;
            }
            if (requiredEnd != null && !trimmedKeyword.endsWith(requiredEnd)) {
                fixed = true
                trimmedKeyword += requiredEnd
            }
            ignoredKeywords.add(trimmedKeyword)
        }
        if (fixed) {
            ignoredKeywordsSetting.value = ignoredKeywords.joinToString(IGNORE_KEYWORDS_DELIM)
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
