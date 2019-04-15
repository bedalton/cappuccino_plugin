package cappuccino.ide.intellij.plugin.settings

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

class ObjJCodeStyleSettings(settings:CodeStyleSettings) : CustomCodeStyleSettings("ObjJCodeStyleSettings", settings) {
    var SPACE_BETWEEN_VARIABLE_TYPE_AND_NAME = true
    var SPACE_BETWEEN_SELECTOR_AND_VALUE_IN_METHOD_CALL = true
    var SPACE_BETWEEN_SELECTOR_AND_VARIABLE_TYPE = false
    var SPACE_BETWEEN_TYPE_AND_PARENS = false
    var NEW_LINE_AFTER_BLOCKS = true
    var BRACE_ON_NEW_LINE = true
    var SPACE_BEFORE_PAREN_STATEMENT = true
    var GROUP_STATMENTS = true
    var DECLARATIONS_ON_NEW_LINE = true
}