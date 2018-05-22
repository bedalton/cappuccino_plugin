package cappuccino.ide.intellij.plugin.psi.interfaces;

import cappuccino.ide.intellij.plugin.structure.ObjJStructureViewElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.psi.NavigatablePsiElement;

import java.util.ArrayList;
import java.util.List;

public interface ObjJHasTreeStructureElement extends ObjJCompositeElement, NavigatablePsiElement {

    ObjJStructureViewElement createTreeStructureElement();

    default TreeElement[] getTreeStructureChildElements() {
        List<TreeElement> treeElements = new ArrayList<>();
        for (ObjJHasTreeStructureElement child : this.getChildrenOfType(ObjJHasTreeStructureElement.class)) {
            treeElements.add(child.createTreeStructureElement());
        }
        return treeElements.toArray(new TreeElement[0]);
    }

}
