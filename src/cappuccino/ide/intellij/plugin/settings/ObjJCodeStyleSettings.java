package cappuccino.ide.intellij.plugin.settings;

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

public class ObjJCodeStyleSettings extends CustomCodeStyleSettings {

    public boolean SPACE_BETWEEN_METHOD_TYPE_AND_RETURN_TYPE = true;
    public boolean SPACE_BETWEEN_VARIABLE_TYPE_AND_NAME = false;
    public boolean SPACE_BETWEEN_SELECTOR_AND_VARIABLE_TYPE = false;
    public boolean SPACE_BETWEEN_RETURN_TYPE_AND_FIRST_SELECTOR = false;
    public boolean SPACE_BETWEEN_SELECTOR_AND_VALUE_IN_METHOD_CALL = false;
    public boolean ALIGN_FIRST_SELECTOR_IN_METHOD_CALL = true;
    public boolean NEW_LINE_AFTER_BLOCKS = false;
    public boolean BRACE_ON_NEW_LINE = true;
    public boolean SPACE_BEFORE_PAREN_STATEMENT = true;
    public boolean SPACE_BEFORE_LBRACE = true;
    public boolean GROUP_STATEMENTS = false;
    public boolean DECLARATIONS_ON_NEW_LINE = true;
    public boolean ALIGN_SELECTORS_IN_METHOD_CALL = true;
    public boolean ALIGN_SELECTORS_IN_METHOD_DECLARATION = false;
    public boolean ALIGN_PROPERTIES = false;


    ObjJCodeStyleSettings(CodeStyleSettings container) {
        super(ObjJLanguage.getInstance().getID(), container);
    }
}
