package cappuccino.ide.intellij.plugin.psi.interfaces;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.List;

public interface ObjJFoldable extends ObjJCompositeElement {
    FoldingDescriptor createFoldingDescriptor(FoldingGroup group);

    default List<ObjJFoldable> getFoldableChildren() {
        List<ObjJFoldable> out = new ArrayList<>();
        for (ObjJFoldable foldable : this.getChildrenOfType(ObjJFoldable.class)) {
            out.add(foldable);
            out.addAll(foldable.getFoldableChildren());
        }
        return out;
    }
}
