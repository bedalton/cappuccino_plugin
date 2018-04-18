package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public interface ObjJHasBlockStatement extends ObjJHasBlockStatements{
    @Nullable
    ObjJBlock getBlock();

    @NotNull
    default List<ObjJBlock> getBlockList() {
        return getBlock() != null ? Collections.singletonList(getBlock()) : Collections.emptyList();
    }
}
