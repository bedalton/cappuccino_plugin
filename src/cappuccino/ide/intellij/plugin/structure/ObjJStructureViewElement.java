package cappuccino.ide.intellij.plugin.structure;

import cappuccino.ide.intellij.plugin.psi.ObjJGlobalVariableDeclaration;
import cappuccino.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration;
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration;
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader;
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasTreeStructureElement;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJStructureViewElement  implements StructureViewTreeElement, SortableTreeElement {
    private ObjJHasTreeStructureElement element;

    @NotNull private final ItemPresentation itemPresentation;
    @NotNull private final String alphaSortKey;
    private final Boolean alwaysLeaf;

    public ObjJStructureViewElement(ObjJHasTreeStructureElement element, @NotNull ItemPresentation itemPresentation, @Nullable
            String alphaSortKey) {
        this.element = element;
        this.itemPresentation = itemPresentation;
        this.alphaSortKey = alphaSortKey != null ? alphaSortKey : "";
        this.alwaysLeaf = element instanceof ObjJInstanceVariableDeclaration ||
                element instanceof ObjJGlobalVariableDeclaration ||
        element instanceof ObjJMethodDeclaration ||
        element instanceof ObjJMethodHeader;
    }

    public Boolean isAlwaysLeaf() {
        return alwaysLeaf;
    }

    @Override
    public Object getValue() {
        return element;
    }

    @Override
    public void navigate(boolean requestFocus) {
        element.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return element.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return element.canNavigateToSource();
    }

    @NotNull
    @Override
    public String getAlphaSortKey() {
        return alphaSortKey;
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        return itemPresentation;
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        return element.getTreeStructureChildElements();
    }
}