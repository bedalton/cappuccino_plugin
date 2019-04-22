package cappuccino.ide.intellij.plugin.settings

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

class ObjJCodeStyleSettings(settings:CodeStyleSettings) : CustomCodeStyleSettings("ObjJCodeStyleSettings", settings) {
    var SPACE_BETWEEN_VARIABLE_TYPE_AND_NAME = true
    var SPACE_BETWEEN_SELECTOR_AND_VARIABLE_TYPE = true
    var SPACE_BETWEEN_RETURN_TYPE_AND_FIRST_SELECTOR = false
    var SPACE_BETWEEN_TYPE_AND_PARENS = false
    var SPACE_BETWEEN_SELECTOR_AND_VALUE_IN_METHOD_CALL = false
    var NEW_LINE_AFTER_BLOCKS = false
    var BRACE_ON_NEW_LINE = true
    var SPACE_BEFORE_PAREN_STATEMENT = true
    var SPACE_BEFORE_LBRACE = true
    var GROUP_STATMENTS = false
    var DECLARATIONS_ON_NEW_LINE = true
    var ALIGN_SELECTORS_IN_METHOD_CALL = true
    var ALIGN_SELECTORS_IN_METHOD_DECLARATION = true
    var ALIGN_PROPERTIES = false
}