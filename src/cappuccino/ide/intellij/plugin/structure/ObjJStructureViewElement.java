package cappuccino.ide.intellij.plugin.structure;

import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasTreeStructureChildElements;
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasTreeStructureElement;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjJStructureViewElement  implements StructureViewTreeElement, SortableTreeElement {
    private ObjJHasTreeStructureElement element;

    @NotNull private final ItemPresentation itemPresentation;
    @NotNull private final String alphaSortKey;

    public ObjJStructureViewElement(ObjJHasTreeStructureElement element, @NotNull ItemPresentation itemPresentation, @Nullable
            String alphaSortKey) {
        this.element = element;
        this.itemPresentation = itemPresentation;
        this.alphaSortKey = alphaSortKey != null ? alphaSortKey : "";
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
        final TreeElement[] out = element.getTreeStructureChildElements();
        //Logger.getLogger("StructureViewElement").log(Level.INFO, "Element <"+itemPresentation.getPresentableText()+"> has "+out.length + " child structure view elements");
        return out;
    }
}