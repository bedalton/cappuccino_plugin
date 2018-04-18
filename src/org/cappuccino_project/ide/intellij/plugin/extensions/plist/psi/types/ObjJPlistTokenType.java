package org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi.types;

import com.intellij.psi.tree.IElementType;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage;

public class ObjJPlistTokenType extends IElementType {
    public ObjJPlistTokenType(String debug) {
        super(debug, ObjJLanguage.INSTANCE);
    }
    @Override
    public String toString() {
        return "PlistTokenType." + super.toString();
    }
}
