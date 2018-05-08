package org.cappuccino_project.ide.intellij.plugin.settings

import com.intellij.ide.util.PropertiesComponent

import org.cappuccino_project.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.*

object ObjJPluginSettings {

    //EOS
    private val INFER_EOS_KEY = "parser.INFER_EOS"
    private val INFER_EOS_DEFAULT = false
    private val inferEOS = BooleanSetting(INFER_EOS_KEY, INFER_EOS_DEFAULT)

    //CallTarget
    private val VALIDATE_CALL_TARGET = "resolve.calltarget.RESOLVE_CALL_TARGET"
    private val VALIDATE_CALL_TARGET_DEFAULT = false
    private val validateCallTarget = BooleanSetting(VALIDATE_CALL_TARGET, VALIDATE_CALL_TARGET_DEFAULT)

    fun inferEOS(): Boolean {
        return inferEOS.value
    }

    fun inferEos(infer: Boolean) {
        inferEOS.value = infer
    }

    fun validateCallTarget(): Boolean {
        return validateCallTarget.value
    }

    fun validateCallTarget(newValidateCallTargetValue: Boolean) {
        validateCallTarget.value = newValidateCallTargetValue
    }


}
