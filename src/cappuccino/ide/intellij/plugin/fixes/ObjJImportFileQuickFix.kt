package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.indices.ObjJImportInstancesIndex
import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory
import cappuccino.ide.intellij.plugin.psi.ObjJImportBlock
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.elementType
import cappuccino.ide.intellij.plugin.psi.utils.getChildOfType
import cappuccino.ide.intellij.plugin.psi.utils.getNextNonEmptySibling
import cappuccino.ide.intellij.plugin.utils.EMPTY_FRAMEWORK_NAME
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import cappuccino.ide.intellij.plugin.utils.editor
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.application.TransactionGuardImpl
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.runUndoTransparentWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.TokenType
import com.intellij.ui.components.JBList
import com.intellij.util.FileContentUtil
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.layout.FlowPane
import java.awt.Component
import javax.swing.*
import kotlin.math.max
import kotlin.math.min


abstract class ObjJImportFileQuickFix(private val thisFramework: String) : BaseIntentionAction(), LocalQuickFix {

    private var selectedForImport: VirtualFile? = null

    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return getPossibleFiles(project).isNotEmpty()
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        invoke(project, descriptor.psiElement.editor, descriptor.psiElement.containingFile)
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val addImportError = run(project, file) ?: return
        LOGGER.severe("Failed to add imports with error: $addImportError")
    }


    private fun run(project: Project, file: PsiFile?): String? {
        if (file == null)
            return "File is null"
        val files = getPossibleFiles(project)
        if (files.isEmpty())
            return "No files found to import"
        if (files.size == 1) {
            return if (addImport(project, file, files.first().file))
                null
            else
                "Failed to add import"
        }
        val dialog:DialogBuilder = createFileChooserDialog(files) { selectedForImport, dialogWrapper ->
            if (selectedForImport == null) {
                dialogWrapper.close(0)
                return@createFileChooserDialog
            }
            TransactionGuardImpl.getInstance().submitTransaction(dialogWrapper.disposable, null, Runnable {
                addImport(project, file, selectedForImport)
                dialogWrapper.close(0)
            })
        } ?: return "Failed to create dialog."

        SwingUtilities.invokeLater {
            dialog.showModal(false)
        }
        return null
    }

    private fun createFileChooserDialog(fileDescriptors: List<FrameworkFileNode>, callback: (fileToImport: VirtualFile?, dialog:DialogWrapper) -> Unit): DialogBuilder? {
        if (fileDescriptors.isEmpty())
            return null
        val mainLayout = JBList(fileDescriptors)
        mainLayout.cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(list: JList<*>?, valueIn: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                val label = super.getListCellRendererComponent(list, valueIn, index, isSelected, cellHasFocus) as JLabel
                val value = valueIn as FrameworkFileNode
                label.text = value.text
                if (value.icon != null)
                    label.icon = value.icon
                return label
            }
        }
        mainLayout.selectedIndex = 0
        mainLayout.addListSelectionListener {
            selectedForImport = fileDescriptors.getOrNull(mainLayout.selectedIndex)?.file
        }

        val buttons = FlowPane(Orientation.HORIZONTAL)
        buttons.alignment = Pos.CENTER_RIGHT

        val dialog = DialogBuilder()
                .setNorthPanel(JLabel(ObjJBundle.message("objective-j.inspections.not-imported.fix.add-import-title")))
                .centerPanel(mainLayout)
        dialog.setOkOperation {
            invokeAndWaitIfNeeded { callback(selectedForImport, dialog.dialogWrapper) }
        }
        dialog.addOkAction()
        dialog.addCancelAction()
        dialog.okAction.setText("Add Import")
        dialog.setCancelOperation {
            callback(null, dialog.dialogWrapper)
        }
        dialog.setPreferredFocusComponent(mainLayout)
        return dialog
    }

    protected abstract fun getFileChooserTitle(): String

    protected abstract fun getFileChooserDescription(): String

    private fun addImport(project: Project, fileToAlter: PsiFile, virtualFileToImport: VirtualFile): Boolean {
        return runUndoTransparentWriteAction {
            val fileToImport = PsiManager.getInstance(project).findFile(virtualFileToImport) as? ObjJFile
                    ?: return@runUndoTransparentWriteAction false
            val containingFramework = fileToImport.frameworkName
            LOGGER.severe("ThisFramework: $thisFramework; ImportFileFramework: $containingFramework;")
            val element = if (containingFramework == thisFramework && containingFramework != EMPTY_FRAMEWORK_NAME)
                ObjJElementFactory.createImportFileElement(project, fileToImport.name)
            else
                ObjJElementFactory.createImportFrameworkFileElement(project, containingFramework, fileToImport.name)
            var siblingElement = addAfter(fileToAlter)
            val added = if (siblingElement == null) {
                siblingElement = fileToAlter.firstChild
                if (siblingElement == null)
                    fileToAlter.add(element)
                else
                    fileToAlter.addBefore(element, siblingElement)
            } else if (siblingElement is ObjJImportBlock) {
                siblingElement.add(element)
            } else {
                fileToAlter.addAfter(element, siblingElement)
            }

            if (siblingElement == null) {
                FileContentUtil.reparseFiles(listOf(fileToAlter.virtualFile))
                return@runUndoTransparentWriteAction added != null
            }
            val siblingTextRange = siblingElement.textRange
            val thisElementRange = added.textRange
            val range = TextRange.create(min(siblingTextRange.startOffset, thisElementRange.startOffset), max(siblingTextRange.endOffset, thisElementRange.endOffset))
            EditorUtil.formatRange(fileToAlter, range)
            FileContentUtil.reparseFiles(listOf(fileToAlter.virtualFile))
            return@runUndoTransparentWriteAction added != null
        }

    }

    private fun addAfter(fileToAlter: PsiFile): PsiElement? {
        var targetElement: PsiElement? = fileToAlter.getChildOfType(ObjJImportBlock::class.java)
        if (targetElement != null)
            return targetElement
        targetElement = fileToAlter.firstChild
        if (targetElement.elementType == TokenType.WHITE_SPACE)
            targetElement = targetElement.getNextNonEmptySibling(true)
        return if (targetElement.elementType in ObjJTokenSets.COMMENTS)
            targetElement
        else
            null
    }

    protected abstract fun getPossibleFiles(project: Project): List<FrameworkFileNode>

    protected fun importedInFiles(project: Project, files: List<ObjJFile>): List<ObjJFile> {
        val importNames = files.map {
            val frameworkName = it.frameworkName
            if (frameworkName.isNotNullOrBlank())
                "$frameworkName/${it.name}"
            else
                "$EMPTY_FRAMEWORK_NAME/${it.name}"
        }

        return importNames.flatMap { importName ->
            ObjJImportInstancesIndex.instance[importName, project].mapNotNull {
                it.containingObjJFile
            }
        }
    }

    protected data class FrameworkFileNode(internal val frameworkName: String, internal val file: VirtualFile, internal val text: String, internal val icon: Icon? = null)

}