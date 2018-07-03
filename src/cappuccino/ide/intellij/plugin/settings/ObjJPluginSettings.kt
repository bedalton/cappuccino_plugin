package cappuccino.ide.intellij.plugin.settings

import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.BooleanSetting

object ObjJPluginSettings {

    //EOS
    private val INFER_EOS_KEY = "parser.INFER_EOS"
    private val INFER_EOS_DEFAULT = false
    private val inferEOS = BooleanSetting(INFER_EOS_KEY, INFER_EOS_DEFAULT)

    //CallTarget
    private val VALIDATE_CALL_TARGET = "resolve.calltarget.RESOLVE_CALL_TARGET"
    private val VALIDATE_CALL_TARGET_DEFAULT = false
    private val validateCallTarget = BooleanSetting(VALIDATE_CALL_TARGET, VALIDATE_CALL_TARGET_DEFAULT)

    //CallTarget
    private val COLLAPSE_BY_DEFAULT_KEY = "resolve.calltarget.RESOLVE_CALL_TARGET"
    private val COLLAPSE_BY_DEFAULT_DEFAULT = false
    private val collapseByDefault = BooleanSetting(COLLAPSE_BY_DEFAULT_KEY, COLLAPSE_BY_DEFAULT_DEFAULT)

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




}
