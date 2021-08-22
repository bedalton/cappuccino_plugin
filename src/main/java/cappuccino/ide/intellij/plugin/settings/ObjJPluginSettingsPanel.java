package cappuccino.ide.intellij.plugin.settings;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;

public class ObjJPluginSettingsPanel {
    public JPanel settingsPanel;
    public JCheckBox underscore_ignoreClassesCheckbox;
    public JCheckBox unqualifiedIgnore_ignoreMethodDec;
    public JCheckBox unqualifiedIgnore_ignoreUndecVars;
    public JCheckBox unqualifiedIgnore_ignoreConflictingMethodDecs;
    public JCheckBox unqualifiedIgnore_ignoreMethodReturnErrors;
    public JCheckBox unqualifiedIgnore_ignoreInvalidSelectors;
    public JTextArea globallyIgnoredVariableNames;
    public JTextArea globallyIgnoredSelectors;
    public JCheckBox resolveVariableTypeFromAssignments;
    public JCheckBox filterMethodCallStrictIfTypeKnown;
    public JTextArea globallyIgnoredFunctionNames;
    public JTextArea globallyIgnoredClassNames;
    public JCheckBox ignoreMissingClassesWhenSuffixedWithRefOrPointer;
    public JCheckBox inferMethodCallReturnTypes;
    public JCheckBox inferFunctionReturnType;
    public JCheckBox minimizeJumps;

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

}
