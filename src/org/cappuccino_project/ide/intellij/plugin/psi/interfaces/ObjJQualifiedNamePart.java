package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import javax.annotation.Nullable;

public interface ObjJQualifiedNamePart extends ObjJCompositeElement {
    @Nullable
    String getQualifiedNameText();
}
