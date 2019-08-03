package cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces;

import cappuccino.ide.intellij.plugin.jstypedef.structure.JsTypeDefStructureViewElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.psi.NavigatablePsiElement;

import java.util.ArrayList;
import java.util.List;

public interface JsTypeDefHasTreeStructureElement extends JsTypeDefElement, NavigatablePsiElement {

    JsTypeDefStructureViewElement createTreeStructureElement();

    default TreeElement[] getTreeStructureChildElements() {
        List<TreeElement> treeElements = new ArrayList<>();
        for (JsTypeDefHasTreeStructureElement child : this.getChildrenOfType(JsTypeDefHasTreeStructureElement.class)) {
            treeElements.add(child.createTreeStructureElement());
        }
        return treeElements.toArray(new TreeElement[0]);
    }

}
