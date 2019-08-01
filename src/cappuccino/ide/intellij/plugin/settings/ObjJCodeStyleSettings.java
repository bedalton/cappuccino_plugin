package cappuccino.ide.intellij.plugin.settings;

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

public class ObjJCodeStyleSettings extends CustomCodeStyleSettings {
    public boolean SPACE_BETWEEN_METHOD_TYPE_AND_RETURN_TYPE = true;
    public boolean SPACE_BETWEEN_VARIABLE_TYPE_AND_NAME = false;
    public boolean SPACE_BETWEEN_SELECTOR_AND_VARIABLE_TYPE = false;
    public boolean SPACE_BETWEEN_RETURN_TYPE_AND_FIRST_SELECTOR = false;
    public boolean SPACE_BETWEEN_SELECTOR_AND_VALUE_IN_METHOD_CALL = false;
    public boolean ALIGN_FIRST_SELECTOR_IN_METHOD_CALL = true;
    public boolean NEW_LINE_AFTER_BLOCKS = false;
    public boolean SPACE_BEFORE_PAREN_STATEMENT = true;
    public boolean SPACE_BEFORE_LBRACE = true;
    public boolean GROUP_STATEMENTS = false;
    public boolean DECLARATIONS_ON_NEW_LINE = true;
    public boolean ALIGN_SELECTORS_IN_METHOD_CALL = true;
    public boolean ALIGN_SELECTORS_IN_METHOD_DECLARATION = false;
    public boolean ALIGN_PROPERTIES = false;
    public int FUNCTION_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
    public int FUNCTION_IN_EXPRESSION_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
    public int INSTANCE_VARIABLE_LIST_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
    public boolean TRY_ON_NEW_LINE = true;
    public int CATCH_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
    public int FINALLY_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
    public int BRACE_ON_NEW_LINE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
    public int SWITCH_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;

    ObjJCodeStyleSettings(CodeStyleSettings container) {
        super(ObjJLanguage.getInstance().getID(), initContainer(container));
    }

    private static CodeStyleSettings initContainer(CodeStyleSettings container) {
        container.FOR_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
        container.DOWHILE_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
        container.WHILE_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
        container.IF_BRACE_FORCE = CommonCodeStyleSettings.FORCE_BRACES_ALWAYS;
        container.ELSE_ON_NEW_LINE = true;
        container.CATCH_ON_NEW_LINE = true;
        container.FINALLY_ON_NEW_LINE = true;
        return container;
    }
}
