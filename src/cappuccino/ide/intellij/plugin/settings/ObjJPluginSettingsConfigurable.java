package cappuccino.ide.intellij.plugin.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsHolder.INSTANCE;

public class ObjJPluginSettingsConfigurable implements Configurable {

    private ObjJSettingsPanel panel;

    @Nls
    @Override
    public String getDisplayName() {
        return "Objective-J";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        panel = new ObjJSettingsPanel();
        panel.refresh();
        return panel.getMain();
    }

    @Override
    public boolean isModified() {
        return
                !INSTANCE.ignoredSelectorString().equals(panel.ignoredSelectors()) ||
                !INSTANCE.ignoredVariableNameString().equals(panel.ignoredVariables()) ||
                INSTANCE.ignoreOvershadowedVariables() != panel.isSuppressOvershadowedVariableWarningsEnabled() ||
                INSTANCE.selectorRenameEnabled() != panel.isSelectorRenameEnabled() ||
                INSTANCE.useIncludedSdkDefinitions() != panel.useIncludedSdkDefinitions();
    }

    @Override
    public void apply() {
        INSTANCE.ignoredSelectorString(panel.ignoredSelectors());
        INSTANCE.ignoredVariableNameString(panel.ignoredVariables());
        INSTANCE.ignoreOvershadowedVariables(panel.isSuppressOvershadowedVariableWarningsEnabled());
        INSTANCE.selectorRenameEnabled(panel.isSelectorRenameEnabled());
        INSTANCE.useIncludedSdkDefinitions(panel.useIncludedSdkDefinitions());
    }

}
