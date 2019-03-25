package cappuccino.ide.intellij.plugin.settings

import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class ObjJPluginSettingsConfigurable : Configurable {

    private val pluginSettingsPanel:ObjJPluginSettingsPanel by lazy {
        ObjJPluginSettingsPanel()
    }

    override fun isModified(): Boolean {
        return  ObjJPluginSettings.resolveCallTargetFromAssignments != pluginSettingsPanel.resolveVariableTypeFromAssignments.isSelected ||
                ObjJPluginSettings.filterMethodCallsStrictIfTypeKnown != pluginSettingsPanel.filterMethodCallStrictIfTypeKnown.isSelected ||
                ObjJPluginSettings.ignoreUnderscoredClasses != pluginSettingsPanel.underscore_ignoreClassesCheckbox.isSelected ||
                ObjJPluginSettings.unqualifiedIgnore_ignoreMethodDeclaration != pluginSettingsPanel.unqualifiedIgnore_ignoreMethodDec.isSelected ||
                ObjJPluginSettings.unqualifiedIgnore_ignoreUndeclaredVariables != pluginSettingsPanel.unqualifiedIgnore_ignoreUndecVars.isSelected ||
                ObjJPluginSettings.unqualifiedIgnore_ignoreConflictingMethodDeclaration != pluginSettingsPanel.unqualifiedIgnore_ignoreConflictingMethodDecs.isSelected ||
                ObjJPluginSettings.unqualifiedIgnore_ignoreMethodReturnErrors != pluginSettingsPanel.unqualifiedIgnore_ignoreMethodReturnErrors.isSelected ||
                ObjJPluginSettings.unqualifiedIgnore_ignoreInvalidSelectorErrors != pluginSettingsPanel.unqualifiedIgnore_ignoreInvalidSelectors.isSelected ||
                ObjJPluginSettings.ignoredSelectorsAsString != pluginSettingsPanel.globallyIgnoredSelectors.text ||
                ObjJPluginSettings.ignoredVariableNamesAsString != pluginSettingsPanel.globallyIgnoredSelectors.text ||
                ObjJPluginSettings.ignoredFunctionNamesAsString != pluginSettingsPanel.globallyIgnoredFunctionNames.text
    }

    override fun getDisplayName(): String {
        return ObjJBundle.message("objective-j.language.name")
    }

    override fun apply() {
        ObjJPluginSettings.resolveCallTargetFromAssignments = pluginSettingsPanel.resolveVariableTypeFromAssignments.isSelected
        ObjJPluginSettings.filterMethodCallsStrictIfTypeKnown = pluginSettingsPanel.filterMethodCallStrictIfTypeKnown.isSelected
        ObjJPluginSettings.ignoreUnderscoredClasses = pluginSettingsPanel.underscore_ignoreClassesCheckbox.isSelected
        ObjJPluginSettings.unqualifiedIgnore_ignoreMethodDeclaration = pluginSettingsPanel.unqualifiedIgnore_ignoreMethodDec.isSelected
        ObjJPluginSettings.unqualifiedIgnore_ignoreUndeclaredVariables = pluginSettingsPanel.unqualifiedIgnore_ignoreUndecVars.isSelected
        ObjJPluginSettings.unqualifiedIgnore_ignoreConflictingMethodDeclaration = pluginSettingsPanel.unqualifiedIgnore_ignoreConflictingMethodDecs.isSelected
        ObjJPluginSettings.unqualifiedIgnore_ignoreMethodReturnErrors = pluginSettingsPanel.unqualifiedIgnore_ignoreMethodReturnErrors.isSelected
        ObjJPluginSettings.unqualifiedIgnore_ignoreInvalidSelectorErrors = pluginSettingsPanel.unqualifiedIgnore_ignoreInvalidSelectors.isSelected
        ObjJPluginSettings.ignoredSelectorsAsString = pluginSettingsPanel.globallyIgnoredSelectors.text
        ObjJPluginSettings.ignoredVariableNamesAsString = pluginSettingsPanel.globallyIgnoredSelectors.text
        ObjJPluginSettings.ignoredFunctionNamesAsString = pluginSettingsPanel.globallyIgnoredFunctionNames.text
    }

    override fun createComponent(): JComponent? {
        val component = pluginSettingsPanel.`$$$getRootComponent$$$`()
        pluginSettingsPanel.resolveVariableTypeFromAssignments.isSelected = ObjJPluginSettings.resolveCallTargetFromAssignments
        pluginSettingsPanel.filterMethodCallStrictIfTypeKnown.isSelected = ObjJPluginSettings.filterMethodCallsStrictIfTypeKnown
        pluginSettingsPanel.underscore_ignoreClassesCheckbox.isSelected = ObjJPluginSettings.ignoreUnderscoredClasses
        pluginSettingsPanel.unqualifiedIgnore_ignoreMethodDec.isSelected = ObjJPluginSettings.unqualifiedIgnore_ignoreMethodDeclaration
        pluginSettingsPanel.unqualifiedIgnore_ignoreUndecVars.isSelected = ObjJPluginSettings.unqualifiedIgnore_ignoreUndeclaredVariables
        pluginSettingsPanel.unqualifiedIgnore_ignoreConflictingMethodDecs.isSelected = ObjJPluginSettings.unqualifiedIgnore_ignoreConflictingMethodDeclaration
        pluginSettingsPanel.unqualifiedIgnore_ignoreMethodReturnErrors.isSelected = ObjJPluginSettings.unqualifiedIgnore_ignoreMethodReturnErrors
        pluginSettingsPanel.unqualifiedIgnore_ignoreInvalidSelectors.isSelected = ObjJPluginSettings.unqualifiedIgnore_ignoreInvalidSelectorErrors
        pluginSettingsPanel.globallyIgnoredSelectors.text = ObjJPluginSettings.ignoredSelectorsAsString
        pluginSettingsPanel.globallyIgnoredSelectors.text = ObjJPluginSettings.ignoredVariableNamesAsString
        pluginSettingsPanel.globallyIgnoredFunctionNames.text = ObjJPluginSettings.ignoredFunctionNamesAsString
        return component
    }

}