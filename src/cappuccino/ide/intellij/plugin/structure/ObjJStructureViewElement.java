package cappuccino.ide.intellij.plugin.structure;

import cappuccino.ide.intellij.plugin.psi.*;
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasTreeStructureElement;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJStructureViewElement implements StructureViewTreeElement, SortableTreeElement {
    private ObjJHasTreeStructureElement hasTreeStructureElements;
    private final ObjJCompositeElement compositeElement;

    @NotNull
    private final ItemPresentation itemPresentation;
    @NotNull
    private final String alphaSortKey;
    private final Boolean alwaysLeaf;

    private Integer weight;

    public ObjJStructureViewElement(ObjJHasTreeStructureElement element,
                                    @NotNull
                                            ItemPresentation itemPresentation,
                                    @Nullable
                                            String alphaSortKey) {
        this.compositeElement = element;
        this.hasTreeStructureElements = element;
        this.itemPresentation = itemPresentation;
        this.alphaSortKey = alphaSortKey != null ? alphaSortKey : "";
        this.alwaysLeaf = element instanceof ObjJInstanceVariableDeclaration ||
                element instanceof ObjJGlobalVariableDeclaration ||
                element instanceof ObjJMethodDeclaration ||
                element instanceof ObjJMethodHeader;
    }

    public ObjJStructureViewElement withWeight(final int weight) {
        this.weight = weight;
        return this;
    }

    @Nullable
    public Integer getWeight() {
        return weight;
    }

    public ObjJStructureViewElement(ObjJCompositeElement element,
                                    @NotNull
                                            ItemPresentation itemPresentation,
                                    @Nullable
                                            String alphaSortKey) {
        this.compositeElement = element;
        this.itemPresentation = itemPresentation;
        this.alphaSortKey = alphaSortKey != null ? alphaSortKey : "";
        this.alwaysLeaf = element instanceof ObjJInstanceVariableDeclaration ||
                element instanceof ObjJGlobalVariableDeclaration ||
                element instanceof ObjJMethodDeclaration ||
                element instanceof ObjJMethodHeader ||
                element instanceof ObjJClassName
        ;
    }

    public Boolean isAlwaysLeaf() {
        return alwaysLeaf;
    }

    @Override
    public Object getValue() {
        return compositeElement;
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (hasTreeStructureElements != null) {
            hasTreeStructureElements.navigate(requestFocus);
        }
        if (compositeElement instanceof Navigatable) {
            ((Navigatable) compositeElement).navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return compositeElement instanceof Navigatable && ((Navigatable) compositeElement).canNavigateToSource();
    }

    @Override
    public boolean canNavigateToSource() {
        if (hasTreeStructureElements != null) {
            return hasTreeStructureElements.canNavigateToSource();
        }
        return compositeElement instanceof Navigatable && ((Navigatable) compositeElement).canNavigateToSource();
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
        final ObjJHasTreeStructureElement element = hasTreeStructureElements;
        if (element == null) {
            return new TreeElement[0];
        }
        final TreeElement[] elements = element.getTreeStructureChildElements();
        return elements != null ? elements : new TreeElement[0];
    }
}