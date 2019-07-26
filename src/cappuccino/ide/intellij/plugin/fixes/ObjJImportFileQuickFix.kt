package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.indices.ObjJImportInstancesIndex
import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory
import cappuccino.ide.intellij.plugin.psi.ObjJImportBlock
import cappuccino.ide.intellij.plugin.psi.ObjJImportFramework
import cappuccino.ide.intellij.plugin.psi.ObjJImportStatementElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.elementType
import cappuccino.ide.intellij.plugin.psi.utils.getChildOfType
import cappuccino.ide.intellij.plugin.psi.utils.getNextNonEmptySibling
import cappuccino.ide.intellij.plugin.utils.*
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
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.components.JBList
import com.intellij.util.FileContentUtil
import icons.ObjJIcons
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.layout.FlowPane
import java.awt.Component
import javax.swing.*
import kotlin.math.max
import kotlin.math.min


abstract class ObjJImportFileQuickFix(private val thisFramework: String) : BaseIntentionAction(), LocalQuickFix {

    private var selectedForImport: VirtualFile? = null
    private var isFrameworkFileNode: Boolean = false

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
            return if (addImport(project, file, files.first().file, isFrameworkFileNode))
                null
            else
                "Failed to add import"
        }
        val dialog: DialogBuilder = createFileChooserDialog(project, files) { selectedForImport, isFrameworkNode: Boolean, dialogWrapper ->
            if (selectedForImport == null) {
                dialogWrapper.close(0)
                return@createFileChooserDialog
            }
            TransactionGuardImpl.getInstance().submitTransaction(dialogWrapper.disposable, null, Runnable {
                addImport(project, file, selectedForImport, isFrameworkNode)
                dialogWrapper.close(0)
            })
        } ?: return "Failed to create dialog."

        SwingUtilities.invokeLater {
            dialog.showModal(false)
        }
        return null
    }

    private fun createFileChooserDialog(project: Project, fileDescriptorsIn: List<FrameworkFileNode>, callback: (fileToImport: VirtualFile?, isFrameworkNode: Boolean, dialog: DialogWrapper) -> Unit): DialogBuilder? {
        if (fileDescriptorsIn.isEmpty())
            return null
        val fileDescriptors = getFullFrameworkButtons(project, fileDescriptorsIn) + fileDescriptorsIn
        val mainLayout = JBList(fileDescriptors)
        mainLayout.cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
                when (value) {
                    is FrameworkFileNode -> {
                        label.text = value.text
                        if (value.icon != null)
                            label.icon = value.icon
                    }
                    is FrameworkMasterFileNode -> {
                        label.text = value.text
                        if (value.icon != null)
                            label.icon = value.icon
                    }
                    else -> label.text = "{UNDEF}"
                }
                return label
            }
        }
        mainLayout.addListSelectionListener {
            val selected = fileDescriptors.getOrNull(mainLayout.selectedIndex)
            selectedForImport = selected?.file
            isFrameworkFileNode = selected is FrameworkMasterFileNode
        }

        mainLayout.selectedIndex = 0
        val buttons = FlowPane(Orientation.HORIZONTAL)
        buttons.alignment = Pos.CENTER_RIGHT

        val dialog = DialogBuilder()
                .setNorthPanel(JLabel(ObjJBundle.message("objective-j.inspections.not-imported.fix.add-import-title")))
                .centerPanel(mainLayout)
        dialog.setOkOperation {
            invokeAndWaitIfNeeded { callback(selectedForImport, isFrameworkFileNode, dialog.dialogWrapper) }
        }
        dialog.addOkAction()
        dialog.addCancelAction()
        dialog.okAction.setText("Add Import")
        dialog.setCancelOperation {
            callback(null, isFrameworkFileNode, dialog.dialogWrapper)
        }
        dialog.setPreferredFocusComponent(mainLayout)
        return dialog
    }

    protected abstract fun getFileChooserTitle(): String

    protected abstract fun getFileChooserDescription(): String

    private fun addImport(project: Project, fileToAlter: PsiFile, virtualFileToImport: VirtualFile, simplifyImports: Boolean): Boolean {
        return runUndoTransparentWriteAction {
            val fileToImport = PsiManager.getInstance(project).findFile(virtualFileToImport) as? ObjJFile
            if (fileToImport == null) {
                LOGGER.severe("Cannot add import for non-ObjJFile")
                return@runUndoTransparentWriteAction false
            }
            val containingFramework = fileToImport.frameworkName

            // Get import element
            val element = if (containingFramework == thisFramework || containingFramework == EMPTY_FRAMEWORK_NAME)
                ObjJElementFactory.createImportFileElement(project, fileToImport.name)
            else
                ObjJElementFactory.createImportFrameworkFileElement(project, containingFramework, fileToImport.name)

            // Find Sibling element to add import statement after
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

            if (simplifyImports) {
                reduceFrameworksImports(fileToAlter as ObjJFile, containingFramework, fileToImport.containingFile.name)
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

    private fun reduceFrameworksImports(file: ObjJFile, frameworkName: String, skipName: String): List<ObjJImportStatementElement> {
        val imports = file.getChildrenOfType(ObjJImportBlock::class.java)
                .flatMap {
                    it.getChildrenOfType(ObjJImportStatementElement::class.java)
                }
                .filter {
                    it.frameworkNameString == frameworkName && it.fileNameString != skipName
                }

        imports.forEach {
            try {
                it.parent.node.removeChild(it.node)
            } catch (_: Exception) {
            }
        }
        return imports
    }

    private fun addAfter(fileToAlter: PsiFile): PsiElement? {
        var targetElement: PsiElement? = fileToAlter.getChildOfType(ObjJImportBlock::class.java)
        if (targetElement != null)
            return targetElement
        targetElement = fileToAlter.firstChild
        while (targetElement.elementType == TokenType.WHITE_SPACE || targetElement.elementType in ObjJTokenSets.COMMENTS) {
            val temp = targetElement?.getNextNonEmptySibling(true)
            if (temp.elementType in ObjJTokenSets.COMMENTS)
                targetElement = temp
            else {
                break
            }
        }
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

    private fun getFullFrameworkButtons(project: Project, nodes: List<FrameworkFileNode>): List<FrameworkMasterFileNode> {
        // Import Framework imports
        return nodes.map {
            it.frameworkName
        }.distinct().filterNot { it == EMPTY_FRAMEWORK_NAME }.flatMap { frameworkName ->
            FilenameIndex.getFilesByName(project, "$frameworkName.j", GlobalSearchScope.everythingScope(project))
                    .filter {
                        frameworkName == it.enclosingFrameworkName
                    }.distinct()
                    .map { file ->
                        FrameworkMasterFileNode(frameworkName, file.virtualFile, "<$frameworkName/$frameworkName.j>", ObjJIcons.FRAMEWORK_ICON)
                    }
        }
    }

    interface FrameworkFileNode {
        val frameworkName: String
        val file: VirtualFile
        val text: String
        val icon: Icon?
    }

    protected data class FrameworkFileNodeImpl(override val frameworkName: String, override val file: VirtualFile, override val text: String, override val icon: Icon? = null) : FrameworkFileNode

    protected data class FrameworkMasterFileNode(override val frameworkName: String, override val file: VirtualFile, override val text: String, override val icon: Icon? = null) : FrameworkFileNode

}