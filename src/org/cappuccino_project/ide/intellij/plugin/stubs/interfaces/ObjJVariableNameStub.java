package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces;

import com.intellij.psi.stubs.StubElement;
import com.intellij.openapi.util.Pair;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJVariableNameImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ObjJVariableNameStub extends StubElement<ObjJVariableNameImpl>, ObjJResolveableStub<ObjJVariableNameImpl> {
    @NotNull
    String getVariableName();
    @NotNull
    List<Pair<Integer, Integer>> getContainingBlockRanges();
    @Nullable
    Pair<Integer,Integer> getGreatestContainingBlockRange();
}
