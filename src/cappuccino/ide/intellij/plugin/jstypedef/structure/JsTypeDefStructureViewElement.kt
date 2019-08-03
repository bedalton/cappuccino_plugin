package cappuccino.ide.intellij.plugin.jstypedef.structure

import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefHasTreeStructureElement
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation

class JsTypeDefStructureViewElement(private val element: JsTypeDefHasTreeStructureElement, private val itemPresentation: ItemPresentation, private val alphaSortKey: String) : StructureViewTreeElement {
    override fun getValue(): Any {
        return element
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

    override fun getPresentation(): ItemPresentation {
        return itemPresentation
    }

    override fun getChildren(): Array<TreeElement> {
        return element.treeStructureChildElements
    }

}