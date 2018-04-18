package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelector;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ObjJHasMethodSelector extends ObjJCompositeElement, ObjJHasContainingClass {

    @NotNull
    default String getSelectorString() {
        return ObjJMethodPsiUtils.getSelectorStringFromSelectorStrings(getSelectorStrings());
    }

    @NotNull
    List<ObjJSelector> getSelectorList();

    @NotNull
    List<String> getSelectorStrings();
}
