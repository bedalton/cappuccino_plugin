package cappuccino.ide.intellij.plugin.ui

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.EMPTY_FRAMEWORK_NAME
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.treeStructure.Tree
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.DefaultMutableTreeNode


object ObjJJTreeBuilder {

    internal fun buildWithFiles(files: List<ObjJFile>, onSelect: (event: TreeSelectionEvent) -> Unit): JTree {
        val frameworksMap: Map<String, List<ObjJFile>> = mapFilesByFramework(files)
        val treeRoot = DefaultMutableTreeNode("Root")
        for ((frameworkName, frameworkFiles) in frameworksMap) {
            if (frameworkFiles.isEmpty())
                continue
            val frameworkNode = FrameworkTreeNode(frameworkName)
            for (file in frameworkFiles) {
                frameworkNode.add(FileTreeNode(frameworkName, file.name, file.virtualFile))
            }
            treeRoot.add(frameworkNode)
        }
        val tree = Tree(treeRoot)
        tree.isRootVisible = false
        tree.addTreeSelectionListener(onSelect)
        tree.cellRenderer = CustomTreeCellRenderer
        return tree
    }


    private fun mapFilesByFramework(files: List<ObjJFile>): Map<String, List<ObjJFile>> {
        val frameworkMapTemp = mutableMapOf<String, MutableList<ObjJFile>>()
        for (file in files) {
            val frameworkName = file.frameworkName
            val list = frameworkMapTemp[frameworkName] ?: mutableListOf()
            list.add(file)
            frameworkMapTemp[frameworkName] = list
        }
        return frameworkMapTemp
    }

    private data class FrameworkTreeNode(internal val frameworkName: String) : DefaultMutableTreeNode()

    internal data class FileTreeNode(internal val frameworkName: String, internal val fileName: String, internal val file: VirtualFile) : DefaultMutableTreeNode()

    internal data class FrameworkFile(internal val frameworkName: String, internal val fileName: String, internal val file: VirtualFile) {
        val text: String
            get() {
                val frameworkName = frameworkName
                return if (frameworkName == EMPTY_FRAMEWORK_NAME)
                    "@import \"$fileName\""
                else
                    "@import <$frameworkName/$fileName>"
            }
    }

    internal object CustomTreeCellRenderer : javax.swing.tree.DefaultTreeCellRenderer() {
        override fun getTreeCellRendererComponent(tree: JTree, value: Any, selected: Boolean, expanded: Boolean,
                                                  leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            val label = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus) as JLabel
            if (value is FrameworkTreeNode) {
                label.text = value.frameworkName
            } else if (value is FileTreeNode) {
                val frameworkName = value.frameworkName
                if (frameworkName == EMPTY_FRAMEWORK_NAME)
                    label.text = "@import \"${value.fileName}\""
                else
                    label.text = "@import <$frameworkName/${value.fileName}>"
            } else {
                LOGGER.severe("Unexpected tree node type")
            }

            return label
        }
    }
}