package cappuccino.ide.intellij.plugin.settings;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class ObjJPluginSettingsPanel {
    private JPanel settingsPanel;
    public JCheckBox underscore_ignoreClassesCheckbox;
    public JCheckBox unqualifiedIgnore_ignoreMethodDec;
    public JCheckBox unqualifiedIgnore_ignoreUndecVars;
    public JCheckBox unqualifiedIgnore_ignoreConflictingMethodDecs;
    public JCheckBox unqualifiedIgnore_ignoreMethodReturnErrors;
    public JCheckBox unqualifiedIgnore_ignoreInvalidSelectors;
    public JTextArea globallyIgnoredVariableNames;
    public JTextArea globallyIgnoredSelectors;

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayoutManager(18, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 16, label1.getFont());
        if (label1Font != null) {
            label1.setFont(label1Font);
        }
        this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("cappuccino/ide/intellij/plugin/lang/objective-j-bundle").getString("objective-j.settings.classes.underscore.title"));
        label1.setVerticalAlignment(1);
        settingsPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        underscore_ignoreClassesCheckbox = new JCheckBox();
        this.$$$loadButtonText$$$(underscore_ignoreClassesCheckbox, ResourceBundle.getBundle("cappuccino/ide/intellij/plugin/lang/objective-j-bundle").getString("objective-j.settings.classes.underscore.checkbox"));
        settingsPanel.add(underscore_ignoreClassesCheckbox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        settingsPanel.add(separator1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(-1, 4), new Dimension(-1, 4), 0, false));
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, -1, 16, label2.getFont());
        if (label2Font != null) {
            label2.setFont(label2Font);
        }
        this.$$$loadLabelText$$$(label2, ResourceBundle.getBundle("cappuccino/ide/intellij/plugin/lang/objective-j-bundle").getString("objective-j.settings.ignore.unqualified.title"));
        settingsPanel.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, ResourceBundle.getBundle("cappuccino/ide/intellij/plugin/lang/objective-j-bundle").getString("objective-j.settings.ignore.unqualified.description"));
        settingsPanel.add(label3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unqualifiedIgnore_ignoreMethodDec = new JCheckBox();
        this.$$$loadButtonText$$$(unqualifiedIgnore_ignoreMethodDec, ResourceBundle.getBundle("cappuccino/ide/intellij/plugin/lang/objective-j-bundle").getString("objective-j.settings.ignore.unqualified.option.removeMethod"));
        settingsPanel.add(unqualifiedIgnore_ignoreMethodDec, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        unqualifiedIgnore_ignoreUndecVars = new JCheckBox();
        this.$$$loadButtonText$$$(unqualifiedIgnore_ignoreUndecVars, ResourceBundle.getBundle("cappuccino/ide/intellij/plugin/lang/objective-j-bundle").getString("objective-j.settings.ignore.unqualified.option.undeclaredVariables"));
        settingsPanel.add(unqualifiedIgnore_ignoreUndecVars, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        unqualifiedIgnore_ignoreConflictingMethodDecs = new JCheckBox();
        this.$$$loadButtonText$$$(unqualifiedIgnore_ignoreConflictingMethodDecs, ResourceBundle.getBundle("cappuccino/ide/intellij/plugin/lang/objective-j-bundle").getString("objective-j.settings.ignore.unqualified.option.conflictingMethodDecs"));
        settingsPanel.add(unqualifiedIgnore_ignoreConflictingMethodDecs, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        unqualifiedIgnore_ignoreMethodReturnErrors = new JCheckBox();
        this.$$$loadButtonText$$$(unqualifiedIgnore_ignoreMethodReturnErrors, ResourceBundle.getBundle("cappuccino/ide/intellij/plugin/lang/objective-j-bundle").getString("objective-j.settings.ignore.unqualified.option.methodReturnValueErrors"));
        settingsPanel.add(unqualifiedIgnore_ignoreMethodReturnErrors, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        unqualifiedIgnore_ignoreInvalidSelectors = new JCheckBox();
        this.$$$loadButtonText$$$(unqualifiedIgnore_ignoreInvalidSelectors, ResourceBundle.getBundle("cappuccino/ide/intellij/plugin/lang/objective-j-bundle").getString("objective-j.settings.ignore.unqualified.option.invalidSelectors"));
        settingsPanel.add(unqualifiedIgnore_ignoreInvalidSelectors, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final JSeparator separator2 = new JSeparator();
        settingsPanel.add(separator2, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, new Dimension(-1, 4), new Dimension(-1, 4), 0, false));
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$(null, -1, 16, label4.getFont());
        if (label4Font != null) {
            label4.setFont(label4Font);
        }
        this.$$$loadLabelText$$$(label4, ResourceBundle.getBundle("cappuccino/ide/intellij/plugin/lang/objective-j-bundle").getString("objective-j.settings.global-ignores.title"));
        settingsPanel.add(label4, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        this.$$$loadLabelText$$$(label5, ResourceBundle.getBundle("cappuccino/ide/intellij/plugin/lang/objective-j-bundle").getString("objective-j.settings.global-ignores.variableNames.title"));
        settingsPanel.add(label5, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        Font label6Font = this.$$$getFont$$$(null, -1, 11, label6.getFont());
        if (label6Font != null) {
            label6.setFont(label6Font);
        }
        this.$$$loadLabelText$$$(label6, ResourceBundle.getBundle("cappuccino/ide/intellij/plugin/lang/objective-j-bundle").getString("objective-j.settings.global-ignores.variableNames.hint"));
        settingsPanel.add(label6, new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        globallyIgnoredVariableNames = new JTextArea();
        settingsPanel.add(globallyIgnoredVariableNames, new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label7 = new JLabel();
        this.$$$loadLabelText$$$(label7, ResourceBundle.getBundle("cappuccino/ide/intellij/plugin/lang/objective-j-bundle").getString("objective-j.settings.global-ignores.methodSelectors.title"));
        settingsPanel.add(label7, new GridConstraints(15, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        Font label8Font = this.$$$getFont$$$(null, -1, 11, label8.getFont());
        if (label8Font != null) {
            label8.setFont(label8Font);
        }
        this.$$$loadLabelText$$$(label8, ResourceBundle.getBundle("cappuccino/ide/intellij/plugin/lang/objective-j-bundle").getString("objective-j.settings.global-ignores.methodSelectors.hint"));
        settingsPanel.add(label8, new GridConstraints(16, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        globallyIgnoredSelectors = new JTextArea();
        settingsPanel.add(globallyIgnoredSelectors, new GridConstraints(17, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 50), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) {
            return null;
        }
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) {
                    break;
                }
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) {
                    break;
                }
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return settingsPanel;
    }

}