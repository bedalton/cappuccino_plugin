package org.cappuccino_project.ide.intellij.plugin.formatting;

import com.intellij.application.options.CodeStyle;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage;

public class ObjJCodeStyleSettings extends CustomCodeStyleSettings {

    private static final String TAG_NAME = "ObjectiveJCodeStyleSettings";
    private Boolean spaceBeforeParenInFunction = null;
    private final CommonCodeStyleSettings myCodeStyleSettings;

    public ObjJCodeStyleSettings(CodeStyleSettings container) {
        super(TAG_NAME, container);
        this.myCodeStyleSettings = container.getCommonSettings(ObjJLanguage.INSTANCE);
    }

}
