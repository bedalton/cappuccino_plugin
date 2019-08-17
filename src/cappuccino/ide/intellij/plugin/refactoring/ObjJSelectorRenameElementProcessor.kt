package cappuccino.ide.intellij.plugin.refactoring

import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.EMPTY_FRAMEWORK_NAME
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
import com.intellij.util.containers.MultiMap
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel

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


    override fun findExistingNameConflicts(element: PsiElement,
                                  newName: String,
                                  conflicts: MultiMap<PsiElement, String>) {
        val framework = element.enclosingFrameworkName
        //val displayedFrameworkName = if (framework != EMPTY_FRAMEWORK_NAME) framework else ""
        val selectorIndex = (element as? ObjJSelector)?.selectorIndex ?: return
        val selector = element.getParentOfType(ObjJHasMethodSelector::class.java)?.selectorString ?: return
        ObjJUnifiedMethodIndex.instance[selector, element.project]
                .mapNotNull {
                    it.selectorList.getOrNull(selectorIndex)
                }
                .filterNot {
                    it.enclosingFrameworkName == framework
                }.forEach {
                    /*val scope = when(val containingClass = it.containingClass) {
                        is ObjJProtocolDeclaration -> "protocol"
                        is ObjJImplementationDeclaration -> if (containingClass.isCategory) "category for" else "implementation"
                        else -> ""
                    }*/
                    conflicts.putValue(it, ObjJBundle.message("objective-j.settings.experimental.selector-outside-framework-conflict.message"))//,
                            //it.getParentOfType(ObjJHasMethodSelector::class.java)?.text ?: selector, scope, containingClass?.classNameString ?: it.containingFileName, displayedFrameworkName))
                }
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
            ObjJPluginSettings.experimental_didAskAboutAllowSelectorRename = true
            ObjJPluginSettings.experimental_allowSelectorRename = true;
            dialog.dialogWrapper.close(DialogWrapper.OK_EXIT_CODE)
            super.show()
            dialog.dispose();
        }
        dialog.setNorthPanel(JLabel("<html><body style='width:500px'>${ObjJBundle.message("objective-j.settings.experimental.selector-rename.warning")}"))
        dialog.show()
    }


}