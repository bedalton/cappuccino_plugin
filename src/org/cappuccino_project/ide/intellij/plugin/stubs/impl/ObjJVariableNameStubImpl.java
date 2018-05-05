package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.openapi.util.Pair;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJVariableNameImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJVariableNameStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ObjJVariableNameStubImpl extends NamedStubBase<ObjJVariableNameImpl> implements ObjJVariableNameStub {

    private final String variableName;
    private final List<Pair<Integer,Integer>> blockRanges;
    private final Pair<Integer,Integer> greatestBlockRange;
    private final boolean shouldResolve;

    public ObjJVariableNameStubImpl(StubElement parent, final @NotNull
            String variableName, @NotNull List<Pair<Integer,Integer>> blockRanges, @Nullable Pair<Integer,Integer> greatestBlockRange, final boolean shouldResolve) {
        super(parent, ObjJStubTypes.VARIABLE_NAME, variableName);
        this.variableName = variableName;
        this.blockRanges = blockRanges;
        this.greatestBlockRange = greatestBlockRange;
        this.shouldResolve = shouldResolve;
    }

    @NotNull
    public String getVariableName() {
        return variableName;
    }

    @NotNull
    @Override
    public List<Pair<Integer, Integer>> getContainingBlockRanges() {
        return blockRanges;
    }

    @Nullable
    @Override
    public Pair<Integer, Integer> getGreatestContainingBlockRange() {
        return greatestBlockRange;
    }

    @Override
    public boolean shouldResolve() {
        return shouldResolve;
    }
}
