package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJAccessorPropertyImpl;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJAccessorPropertyStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ObjJAccessorPropertyStubImpl extends ObjJStubBaseImpl<ObjJAccessorPropertyImpl> implements ObjJAccessorPropertyStub {

    private final String containingClass;
    private final String variableName;
    private final String getter;
    private final String setter;
    private String varType;
    private final boolean shouldResolve;

    public ObjJAccessorPropertyStubImpl(final StubElement parent, final String containingClass,
                                        @Nullable String varType, @Nullable String variableName,
                                        @Nullable String getter, @Nullable String setter,
                                        final boolean shouldResolve
    ) {
        super(parent, ObjJStubTypes.ACCESSOR_PROPERTY);
        this.containingClass = containingClass;
        this.varType = varType != null && !varType.isEmpty() ? varType : null;
        this.variableName = variableName != null && !variableName.isEmpty() ? variableName : null;
        this.getter = getter != null && !getter.isEmpty() ? getter : null;
        this.setter = setter != null && !setter.isEmpty() ? setter : null;
        this.shouldResolve = shouldResolve;
    }

    @NotNull
    @Override
    public String getContainingClass() {
        return containingClass;
    }

    @Nullable
    @Override
    public String getVariableName() {
        return variableName;
    }

    @Nullable
    @Override
    public String getGetter() {
        return getter;
    }

    @javax.annotation.Nullable
    @Override
    public String getSetter() {
        return setter;
    }

    @Nullable
    @Override
    public String getVarType() {
        return varType;
    }

    @NotNull
    @Override
    public List<String> getParamTypes() {
        return getter != null ? Collections.emptyList() : Collections.singletonList(getVarType());
    }

    @NotNull
    @Override
    public List<String> getSelectorStrings() {
        return Arrays.asList(getSelectorString().split(ObjJMethodPsiUtils.SELECTOR_SYMBOL));
    }

    @NotNull
    @Override
    public String getSelectorString() {
        return getter != null ? getter : setter != null ? setter : ObjJMethodPsiUtils.EMPTY_SELECTOR;
    }

    @NotNull
    @Override
    public String getContainingClassName() {
        return containingClass;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @NotNull
    @Override
    public ObjJClassType getReturnType() {
        return getter != null ? ObjJClassType.getClassType(varType) : ObjJClassType.VOID;
    }

    @NotNull
    @Override
    public String getReturnTypeAsString() {
        return getter != null ? varType : ObjJClassType.VOID_CLASS_NAME;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean shouldResolve() {
        return shouldResolve;
    }
}
