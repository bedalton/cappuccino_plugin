package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJBlock;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ObjJHasBlockStatements extends ObjJCompositeElement {
    @NotNull
    List<ObjJBlock> getBlockList();
}
