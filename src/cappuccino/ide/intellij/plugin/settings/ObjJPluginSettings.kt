package cappuccino.ide.intellij.plugin.settings

import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.BooleanSetting

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
    private val ignoredKeywordsSetting = ObjJIgnoredStringsListSetting(IGNORE_PROPERTIES_KEY)

    private val IGNORE_MISSING_SELECTORS_KEY = "objj.annotator.ignoreMissingSelector"
    private val ignoreMissingSelectorsSetting = ObjJIgnoredStringsListSetting(IGNORE_MISSING_SELECTORS_KEY)

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


    fun ignoreVariableName(keyword:String) = ignoredKeywordsSetting.ignoreKeyword(keyword)

    fun removeIgnoredVariableName(keyword:String) = ignoredKeywordsSetting.removeIgnoredKeyword(keyword)

    fun ignoredVariableNames() : List<String> = ignoredKeywordsSetting.ignoredKeywords()

    fun isIgnoredVariableName(keyword:String) : Boolean  = ignoredKeywordsSetting.isIgnoredKeyword(keyword)


    fun ignoreSelector(keyword:String) = ignoreMissingSelectorsSetting.ignoreKeyword(keyword)

    fun doNotIgnoreSelector(keyword:String) = ignoreMissingSelectorsSetting.removeIgnoredKeyword(keyword)

    fun ignoredSelectors() : List<String> = ignoreMissingSelectorsSetting.ignoredKeywords()

    fun isIgnoredSelector(keyword:String) : Boolean  = ignoreMissingSelectorsSetting.isIgnoredKeyword(keyword)


    fun ignoreOvershadowedVariables() : Boolean {
        return ignoreOvershadowedVariablesSetting.value ?: IGNORE_OVERSHADOWED_VARIABLES_DEFAULT
    }

    fun ignoreOvershadowedVariables(shouldIgnore:Boolean) {
        ignoreOvershadowedVariablesSetting.value = shouldIgnore
    }



}
