package cappuccino.ide.intellij.plugin.settings

import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.BooleanSetting

object ObjJPluginSettingsHolder {

    //EOS
    private const val INFER_EOS_KEY = "objj.parser.INFER_EOS"
    private const val INFER_EOS_DEFAULT = false
    private val inferEOS = BooleanSetting(INFER_EOS_KEY, INFER_EOS_DEFAULT)

    //CallTarget
    private const val VALIDATE_CALL_TARGET = "objj.resolve.calltarget.validate"
    private const val VALIDATE_CALL_TARGET_DEFAULT = false
    private val validateCallTarget = BooleanSetting(VALIDATE_CALL_TARGET, VALIDATE_CALL_TARGET_DEFAULT)

    //Folding
    private const val COLLAPSE_BY_DEFAULT_KEY = "objj.folding.collapse-by-default"
    private const val COLLAPSE_BY_DEFAULT_DEFAULT = false
    private val COLLAPSE_BY_DEFAULT = BooleanSetting(COLLAPSE_BY_DEFAULT_KEY, COLLAPSE_BY_DEFAULT_DEFAULT)

    // ==== Variables ==== //
    // Variables.unresolved.ignore
    private const val IGNORED_VARIABLE_NAME_KEYS = "objj.annotator.variables.unresolved.ignored"
    private val IGNORED_VARIABLE_NAMES = ObjJIgnoredStringsListSetting(IGNORED_VARIABLE_NAME_KEYS)
    // Variables.overshadowed
    private const val SUPRRESS_OVERSHADOWED_VARIABLES_KEY = "objj.annotator.variables.overshadowed.ignore"
    private const val SUPPRESS_OVERSHADOWED_VARIABLES_DEFAULT = false
    private val SUPPRESS_VARIABLES_OVERSHADOWED_WARNING = BooleanSetting(SUPRRESS_OVERSHADOWED_VARIABLES_KEY, SUPPRESS_OVERSHADOWED_VARIABLES_DEFAULT)

    // ==== SELECTORS === //
    // Selectors.unresolved.ignore
    private const val SELECTOR_IGNORED = "objj.annotator.selectors.unresolved.ignored"
    private val IGNORED_MISSING_SELECTORS = ObjJIgnoredStringsListSetting(SELECTOR_IGNORED, ObjJMethodPsiUtils.SELECTOR_SYMBOL)
    // Selectors.rename
    private const val SELECTOR_RENAME_ENABLED_KEY = "objj.experimental.selectors.rename.enabled"
    private const val SELECTOR_RENAME_ENABLED_DEFAULT = false
    private val SELECTOR_RENAME_ENABLED = BooleanSetting(SELECTOR_RENAME_ENABLED_KEY, SELECTOR_RENAME_ENABLED_DEFAULT)


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

    fun collapseByDefault(): Boolean = COLLAPSE_BY_DEFAULT.value ?: COLLAPSE_BY_DEFAULT_DEFAULT
    fun collapseByDefault(collapse: Boolean) {
        this.COLLAPSE_BY_DEFAULT.value = collapse
    }


    // ================================= //
    // =========== Variables =========== //
    // ================================= //

    fun ignoreOvershadowedVariables(): Boolean {
        return SUPPRESS_VARIABLES_OVERSHADOWED_WARNING.value ?: SUPPRESS_OVERSHADOWED_VARIABLES_DEFAULT
    }

    fun ignoreOvershadowedVariables(shouldIgnore: Boolean) {
        SUPPRESS_VARIABLES_OVERSHADOWED_WARNING.value = shouldIgnore
    }

    fun addIgnoredVariableNameToList(keyword: String) = IGNORED_VARIABLE_NAMES.ignoreKeyword(keyword)

    fun removeIgnoredVariableNameFromList(keyword: String) = IGNORED_VARIABLE_NAMES.removeIgnoredKeyword(keyword)

    fun ignoredVariableNameList(): List<String> = IGNORED_VARIABLE_NAMES.ignoredKeywords()

    fun ignoredVariableNameString(): String = IGNORED_VARIABLE_NAMES.asString()

    fun ignoredVariableNameString(string: String) {
        IGNORED_VARIABLE_NAMES.ignoredKeywords(string)
    }

    fun isVariableNameIgnored(keyword: String): Boolean = IGNORED_VARIABLE_NAMES.isIgnoredKeyword(keyword)

    // ================================= //
    // =========== Selectors =========== //
    // ================================= //

    fun addIgnoredSelectorToList(keyword: String) = IGNORED_MISSING_SELECTORS.ignoreKeyword(keyword)

    fun removeIgnoredSelectorFromList(keyword: String) = IGNORED_MISSING_SELECTORS.removeIgnoredKeyword(keyword)

    fun ignoredSelectorsList(): List<String> = IGNORED_MISSING_SELECTORS.ignoredKeywords()
    fun ignoredSelectorString(): String = IGNORED_MISSING_SELECTORS.asString()
    fun ignoredSelectorString(string: String) = {
        IGNORED_MISSING_SELECTORS.ignoredKeywords(string)
    }

    fun isSelectorIgnored(keyword: String): Boolean = IGNORED_MISSING_SELECTORS.isIgnoredKeyword(keyword)


    fun selectorRenameEnabled(): Boolean = SELECTOR_RENAME_ENABLED.value ?: SELECTOR_RENAME_ENABLED_DEFAULT

    fun selectorRenameEnabled(enabled: Boolean) {
        SELECTOR_RENAME_ENABLED.value = enabled
    }
}
