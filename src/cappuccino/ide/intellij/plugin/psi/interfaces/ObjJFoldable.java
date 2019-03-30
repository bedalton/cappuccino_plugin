package cappuccino.ide.intellij.plugin.psi.interfaces;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.FoldingGroup;


public interface ObjJFoldable extends ObjJCompositeElement {
    FoldingDescriptor createFoldingDescriptor(FoldingGroup group);
}
