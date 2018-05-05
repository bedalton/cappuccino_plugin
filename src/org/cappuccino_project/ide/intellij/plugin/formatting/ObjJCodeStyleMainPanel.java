package org.cappuccino_project.ide.intellij.plugin.formatting;

import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage;

class ObjJCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
    ObjJCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
        super(ObjJLanguage.INSTANCE, currentSettings, settings);
    }



    @Override
    public String getPreviewText() {
        return  "@import <Framework/Framework.j>" + "\n" +
                "@import @\"CPButton.j\"" + "\n" +
                "\n" +
                "@implementation ObjectiveJ <Language>" +"\n" +
                "{" + "\n" +
                "\tif" +
                "}" + "\n" +
                "\n" +
                "@end"
                ;
    }


}