package cappuccino.ide.intellij.plugin.refactoring

import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.enclosingFrameworkName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiElement
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenameDialog
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.usageView.UsageInfo
import com.intellij.util.IncorrectOperationException
import java.awt.GridLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class ObjJSelectorRenameElementProcessor : RenamePsiElementProcessor() {

    var framework: String? = null

    private var allowSelectorRename: Boolean = ObjJPluginSettings.experimental_allowSelectorRename
    private var allowFrameworkRename: Boolean = ObjJPluginSettings.experimental_allowFrameworkSelectorRename
    override fun createRenameDialog(project: Project,
                                    element: PsiElement,
                                    nameSuggestionContext: PsiElement?,
                                    editor: Editor?): RenameDialog {
        framework = element.enclosingFrameworkName
        return ObjJSelectorRenameDialog(project, element, nameSuggestionContext, editor) {
            allowFrameworkRename = it
        }
    }

    override fun forcesShowPreview(): Boolean {
        return true;
    }

    override fun canProcessElement(element: PsiElement): Boolean {
        return element is ObjJSelector
    }

    @Throws(IncorrectOperationException::class)
    override fun renameElement(element: PsiElement,
                               newName: String,
                               usages: Array<UsageInfo>,
                               listener: RefactoringElementListener?) {
        super.renameElement(element, newName, usages, listener)
    }

}

internal class ObjJSelectorRenameDialog(
        project: Project,
        element: PsiElement,
        nameSuggestionContext: PsiElement?,
        editor: Editor?,
        val allowFrameworkRename: (Boolean) -> Unit)
    : RenameDialog(project, element, nameSuggestionContext, editor) {

    init {
        super.init()
        okAction.isEnabled = false
        cancelAction.isEnabled = true
    }


    override fun createCenterPanel(): JComponent? {
        if (ObjJPluginSettings.experimental_allowSelectorRename)
            return super.createCenterPanel()
        val checkbox = JCheckBox(ObjJBundle.message("objective-j.settings.experimental.selector-rename-outside-framework.checkbox"), ObjJPluginSettings.experimental_allowFrameworkSelectorRename)
        checkbox.addChangeListener {
            allowFrameworkRename(checkbox.isSelected)
        }
        return checkbox
    }


    override fun show() {
        if (ObjJPluginSettings.experimental_allowSelectorRename) {
            super.show()
            return;
        }
        val dialog = DialogBuilder()
        dialog.setOkOperation {
            dialog.dialogWrapper.close(DialogWrapper.OK_EXIT_CODE)
            super.show()
            dialog.dispose();
        }
        dialog.setNorthPanel(JLabel("<html><body style='font-weight:bold; font-size:18px;width:90%;max-width:400px'>${ObjJBundle.message("objective-j.settings.experimental.selector-rename.warning")}"))
        val gridLayout = GridLayout(2, 1, 0, 10)
        val centerPanel = JPanel(gridLayout)
        val allowFrameworkRename = JCheckBox(ObjJBundle.message("objective-j.settings.experimental.selector-rename-outside-framework.checkbox"), ObjJPluginSettings.experimental_allowFrameworkSelectorRename)
        allowFrameworkRename.addActionListener {
            ObjJPluginSettings.experimental_allowFrameworkSelectorRename
        }
        val alwaysAllow = JCheckBox("Always allow experimental rename", false)
        alwaysAllow.addChangeListener {
            ObjJPluginSettings.experimental_didAskAboutAllowSelectorRename = true
            ObjJPluginSettings.experimental_allowSelectorRename = true;
        }
        centerPanel.add(alwaysAllow)
        centerPanel.add(allowFrameworkRename)
        dialog.setCenterPanel(centerPanel)
        dialog.show()
    }


}