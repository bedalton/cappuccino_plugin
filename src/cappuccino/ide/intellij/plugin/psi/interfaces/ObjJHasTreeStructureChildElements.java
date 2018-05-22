package cappuccino.ide.intellij.plugin.psi.interfaces;

import com.intellij.ide.util.treeView.smartTree.TreeElement;

import java.util.ArrayList;
import java.util.List;

public interface ObjJHasTreeStructureChildElements extends ObjJCompositeElement {

    default List<ObjJHasTreeStructureElement> getTreeChildren() {
        return this.getChildrenOfType(ObjJHasTreeStructureElement.class);
    }

}
