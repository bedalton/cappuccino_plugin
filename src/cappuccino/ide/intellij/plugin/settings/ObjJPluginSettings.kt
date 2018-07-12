package cappuccino.ide.intellij.plugin.settings

import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.BooleanSetting
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.StringSetting

object ObjJPluginSettings {

    //EOS
    private val INFER_EOS_KEY = "objj.parser.INFER_EOS"
    private val INFER_EOS_DEFAULT = false
    private val inferEOS = BooleanSetting(INFER_EOS_KEY, INFER_EOS_DEFAULT)

    //CallTarget
    private val VALIDATE_CALL_TARGET = "objj.resolve.calltarget.RESOLVE_CALL_TARGET"
    private val VALIDATE_CALL_TARGET_DEFAULT = false
    private val validateCallTarget = BooleanSetting(VALIDATE_CALL_TARGET, VALIDATE_CALL_TARGET_DEFAULT)

    //CallTarget
    private val COLLAPSE_BY_DEFAULT_KEY = "objj.resolve.calltarget.RESOLVE_CALL_TARGET"
    private val COLLAPSE_BY_DEFAULT_DEFAULT = false
    private val collapseByDefault = BooleanSetting(COLLAPSE_BY_DEFAULT_KEY, COLLAPSE_BY_DEFAULT_DEFAULT)

    private val IGNORE_PROPERTIES_KEY = "objj.annotator.ignoreProperties";
    private val IGNORE_PROPERTIES_DEFAULT = "";
    private val ignoredKeywordsSetting = StringSetting(IGNORE_PROPERTIES_KEY, IGNORE_PROPERTIES_DEFAULT)
    private val IGNORE_KEYWORDS_DELIM = ","
    private var ignoredKeywords = ignoredKeywordsSetting.value!!.split(IGNORE_KEYWORDS_DELIM)

    private val IGNORE_OVERSHADOWED_VARIABLES_KEY = "objj.annotator.ignoreOvershadowed"
    private val IGNORE_OVERSHADOWED_VARIABLES_DEFAULT = false
    private val ignoreOvershadowedVariablesSetting = BooleanSetting(IGNORE_OVERSHADOWED_VARIABLES_KEY, IGNORE_OVERSHADOWED_VARIABLES_DEFAULT)


    fun inferEOS(): Boolean {
        return inferEOS.value ?: INFER_EOS_DEFAULT
    }

    fun inferEos(infer: Boolean) {
        inferEOS.value = infer
    }

    fun validateCallTarget(): Boolean {
        return validateCallTarget.value ?: VALIDATE_CALL_TARGET_DEFAULT
    }

    fun validateCallTarget(newValidateCallTargetValue: Boolean) {
        validateCallTarget.value = newValidateCallTargetValue
    }

    fun collapseByDefault() : Boolean = collapseByDefault.value ?: COLLAPSE_BY_DEFAULT_DEFAULT
    fun collapseByDefault(collapse:Boolean) {
        this.collapseByDefault.value = collapse
    }


    fun addIgnoreKeyword(keyword:String) {
        if (ignoredKeywords.contains(keyword)) {
            return;
        }
        ignoredKeywords += keyword;
        ignoreKeywords(ignoredKeywords.joinToString(IGNORE_KEYWORDS_DELIM))
    }

    fun ignoreKeywords(keywords:String) {
        ignoredKeywordsSetting.value = keywords
        ignoredKeywords = loadIgnoredKeywords();
    }

    fun ignoredKeywords() : List<String> {
        return ignoredKeywords
    }

    fun isIgnoredKeyword(keyword:String) : Boolean {
        return ignoredKeywords.contains(keyword);
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

    fun ignoreOvershadowedVariables() : Boolean {
        return ignoreOvershadowedVariablesSetting.value ?: IGNORE_OVERSHADOWED_VARIABLES_DEFAULT
    }

    fun ignoreOvershadowedVariables(shouldIgnore:Boolean) {
        ignoreOvershadowedVariablesSetting.value = shouldIgnore
    }



}
