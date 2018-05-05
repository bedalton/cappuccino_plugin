package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJMethodCallImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodCallStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ObjJMethodCallStubImpl extends ObjJStubBaseImpl<ObjJMethodCallImpl> implements ObjJMethodCallStub {

    private final String className;
    private final String callTarget;
    private final List<String> possibleCallTargetTypes;
    private final List<String> selectors;
    private String selectorString;
    private final boolean shouldResolve;

    public ObjJMethodCallStubImpl(@NotNull final StubElement parent, @Nullable String className, @NotNull final String callTarget, @NotNull List<String> possibleCallTargetTypes, @NotNull final List<String> selectors, final boolean shouldResolve) {
        super(parent, ObjJStubTypes.METHOD_CALL);
        this.className = className;
        this.callTarget = callTarget;
        this.possibleCallTargetTypes = possibleCallTargetTypes;
        this.selectors = selectors;
        this.shouldResolve = shouldResolve;
    }

    @NotNull
    @Override
    public String getCallTarget() {
        return callTarget;
    }

    @NotNull
    @Override
    public List<String> getPossibleCallTargetTypes() {
        return possibleCallTargetTypes;
    }

    @NotNull
    @Override
    public List<String> getSelectorStrings() {
        return selectors;
    }

    @NotNull
    @Override
    public String getSelectorString() {
        if (selectorString == null) {
            selectorString = ObjJMethodPsiUtils.getSelectorStringFromSelectorStrings(this.selectors);
        }
        return selectorString;
    }

    @Nullable
    @Override
    public String getContainingClassName() {
        return className;
    }

    @Override
    public boolean shouldResolve() {
        return shouldResolve;
    }

}
