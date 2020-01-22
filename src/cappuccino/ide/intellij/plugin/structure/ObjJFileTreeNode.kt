package cappuccino.ide.intellij.plugin.structure

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.fileTypes.StdFileTypes
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.ui.tree.LeafState

class ObjJFileTreeNode(
        myProject: Project,
        val value:ObjJFile,
        viewSettings:ViewSettings
) : PsiFileNode(myProject, value, viewSettings) {

    private val objJFile: ObjJFile
        get() = getValue() as ObjJFile

    override fun contains(otherFile: VirtualFile): Boolean {
        return this.virtualFile == otherFile
    }

    override fun getLeafState(): LeafState {
        return LeafState.DEFAULT
    }

    override fun shouldDrillDownOnEmptyElement(): Boolean {
        val file = this.getValue() as? PsiFile
        return file != null && file.fileType === StdFileTypes.JAVA
    }

    override fun getChildrenImpl(): Collection<AbstractTreeNode<*>> {
        val file = objJFile
        return getChildren(file)
    }
}