package org.cappuccino_project.ide.intellij.plugin.psi.types;

import com.intellij.psi.tree.IElementType;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage;

public class ObjJCompositeElementType extends IElementType {
    public ObjJCompositeElementType(String debug) {
        super(debug, ObjJLanguage.INSTANCE);
    }
}