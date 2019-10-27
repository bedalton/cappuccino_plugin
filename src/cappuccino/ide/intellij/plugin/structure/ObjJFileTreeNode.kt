package cappuccino.ide.intellij.plugin.structure

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

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

    override fun getChildrenImpl(): Collection<AbstractTreeNode<*>> {
        val file = objJFile
        return getChildren(file)
    }
}