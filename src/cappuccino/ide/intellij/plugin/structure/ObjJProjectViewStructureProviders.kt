package cappuccino.ide.intellij.plugin.structure

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.DumbAware
import java.util.*

class ObjJExpandNodeProjectViewProvider : TreeStructureProvider, DumbAware {

    // should be called after ClassesTreeStructureProvider
    override fun modify(
            parent: AbstractTreeNode<Any>,
            children: Collection<AbstractTreeNode<Any>>,
            settings: ViewSettings
    ): Collection<AbstractTreeNode<out Any>> {
        val result = ArrayList<AbstractTreeNode<out Any>>()

        for (child in children) {
            val childValue = child.value as? ObjJFile

            if (childValue != null) {
                result.add(ObjJFileTreeNode(childValue.project, childValue, settings))
            }
            else {
                result.add(child)
            }

        }

        return result
    }

    override fun getData(selected: Collection<AbstractTreeNode<Any>>, dataName: String): Any? = null
}

/*
class KotlinSelectInProjectViewProvider(private val project: Project) : SelectableTreeStructureProvider, DumbAware {
    override fun getData(selected: Collection<AbstractTreeNode<Any>>, dataName: String): Any? = null

    override fun modify(
            parent: AbstractTreeNode<Any>,
            children: Collection<AbstractTreeNode<Any>>,
            settings: ViewSettings
    ): Collection<AbstractTreeNode<out Any>> {
        return ArrayList(children)
    }


    // should be called before ClassesTreeStructureProvider
    override fun getTopLevelElement(element: PsiElement): PsiElement? {
        if (!element.isValid) return null
        val file = element.containingFile as? ObjJFile ?: return null

        val virtualFile = file.virtualFile
        if (!fileInRoots(virtualFile)) return file

        var current = element.parentsWithSelf.firstOrNull() { it.isSelectable() }

        if (current is KtFile) {
            val declaration = current.declarations.singleOrNull()
            val nameWithoutExtension = virtualFile?.nameWithoutExtension ?: file.name
            if (declaration is KtClassOrObject && nameWithoutExtension == declaration.name) {
                current = declaration
            }
        }

        return current ?: file
    }

    private fun PsiElement.isSelectable(): Boolean = when (this) {
        is KtFile -> true
        is KtDeclaration ->
            parent is KtFile ||
                    ((parent as? KtClassBody)?.parent as? KtClassOrObject)?.isSelectable() ?: false
        else -> false
    }

    private fun fileInRoots(file: VirtualFile?): Boolean {
        val index = ProjectRootManager.getInstance(project).fileIndex
        return file != null && (index.isInSourceContent(file) || index.isInLibraryClasses(file) || index.isInLibrarySource(file))
    }
}
*/