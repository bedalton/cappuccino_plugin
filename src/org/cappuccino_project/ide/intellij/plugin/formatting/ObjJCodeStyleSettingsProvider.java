package org.cappuccino_project.ide.intellij.plugin.formatting;

import com.intellij.application.options.CodeStyle;
import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.openapi.options.Configurable;
import com.intellij.psi.codeStyle.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJCodeStyleSettingsProvider extends CodeStyleSettingsProvider {

    @Override
    public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings codeStyleSettings) {
        return new ObjJCodeStyleSettings(codeStyleSettings);
    }


    public static ObjJCodeStyleSettings getSettings() {
        return CodeStyle.getDefaultSettings().getCustomSettings(ObjJCodeStyleSettings.class);
    }

    @Nullable
    @Override
    public String getConfigurableDisplayName() {
        return "Objective-J";
    }

    @NotNull
    @Override
    public Configurable createSettingsPage(CodeStyleSettings settings, CodeStyleSettings originalSettings) {
        return new CodeStyleAbstractConfigurable(settings, originalSettings, "Simple") {
            @Override
            protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
                return new ObjJCodeStyleMainPanel(getCurrentSettings(), settings);
            }

            @Nullable
            @Override
            public String getHelpTopic() {
                return null;
            }
        };
    }
}