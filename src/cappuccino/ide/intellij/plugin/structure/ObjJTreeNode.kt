package cappuccino.ide.intellij.plugin.structure

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasTreeStructureElement
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import java.util.ArrayList

class ObjJTreeNode(private val element: ObjJHasTreeStructureElement, project: Project) : AbstractTreeNode<PsiElement>(project, element) {
    private val viewElement: ObjJStructureViewElement = element.createTreeStructureElement()
    private val alphaSortKey: String
    private val alwaysLeaf: Boolean

    init {
        this.alphaSortKey = viewElement.alphaSortKey
        alwaysLeaf = viewElement.isAlwaysLeaf
    }

    override fun isAlwaysLeaf(): Boolean {
        return alwaysLeaf
    }

    override fun navigate(requestFocus: Boolean) {
        element.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean {
        return element.canNavigate()
    }

    override fun canNavigateToSource(): Boolean {
        return element.canNavigateToSource()
    }

    override fun update(
            presentationData: PresentationData) {
        val elementPresentationData = viewElement.presentation
        presentationData.setIcon(elementPresentationData.getIcon(true))
        presentationData.presentableText = elementPresentationData.presentableText
        presentationData.locationString = elementPresentationData.locationString
    }
    override fun getChildren(): List<AbstractTreeNode<PsiElement>> {
        return getChildren(element)
    }
}


internal fun getChildren(element: ObjJCompositeElement) : List<AbstractTreeNode<PsiElement>> {
    val project = element.project
    val treeElements:MutableList<AbstractTreeNode<PsiElement>> = mutableListOf()
    for (child in element.getChildrenOfType(ObjJHasTreeStructureElement::class.java)) {
        treeElements.add(ObjJTreeNode(child, project))
    }

    when (element) {
        is ObjJImplementationDeclaration -> {
            val instanceVars = element.instanceVariableList?.instanceVariableDeclarationList?.map {
                ObjJTreeNode(it, project)
            }.orEmpty()
            treeElements.addAll(instanceVars)
        }
        is ObjJProtocolDeclaration -> {
            val instanceVars = element.instanceVariableDeclarationList.map {
                ObjJTreeNode(it, project)
            }
            treeElements.addAll(instanceVars)
        }
        is ObjJFile -> treeElements.addAll(getVariableNamesInFile(element, project))
    }
    return treeElements
}

private fun getVariableNamesInFile(file: ObjJFile, project:Project) : List<AbstractTreeNode<PsiElement>> {
    val bodyVariableAssignments = file.getChildrenOfType(ObjJBodyVariableAssignment::class.java)
    val fileScopeVariables = ArrayList<ObjJVariableName>()
    for (bodyVariableAssignment in bodyVariableAssignments) {
        if (bodyVariableAssignment.getVariableModifier() == null)
            continue
        val declarationList = bodyVariableAssignment.getVariableDeclarationList() ?: continue
        fileScopeVariables.addAll(declarationList.getVariableNameList())
        val variablesInDecList = declarationList.getVariableDeclarationList()
                .flatMap{ dec ->
                    dec.qualifiedReferenceList.mapNotNull { it.primaryVar }
                }
        fileScopeVariables.addAll(variablesInDecList)
    }
    return fileScopeVariables.map{ ObjJTreeNode(it, project) }
}