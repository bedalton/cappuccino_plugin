package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJMethodHeaderImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ObjJMethodHeaderStubImpl extends ObjJStubBaseImpl<ObjJMethodHeaderImpl> implements ObjJMethodHeaderStub {

    private final List<String> selectors;
    private final List<String> paramTypes;
    private final String returnType;
    private final String className;
    private final boolean required;
    private final boolean isStatic;
    private final String selectorString;
    private final boolean shouldResolve;

    public ObjJMethodHeaderStubImpl(final StubElement parent, @Nullable final String className, final boolean isStatic, @NotNull  final List<String> selectors, @NotNull final List<String> paramTypes, @Nullable final String returnType, final boolean required, final boolean shouldResolve) {
        super(parent, ObjJStubTypes.METHOD_HEADER);
        this.isStatic = isStatic;
        this.selectors = selectors;
        this.selectorString = ObjJMethodPsiUtils.getSelectorStringFromSelectorStrings(selectors);
        this.paramTypes = paramTypes;
        this.returnType = returnType != null ? returnType : ObjJClassType.UNDETERMINED;
        this.className = className != null ? className : ObjJClassType.UNDEF_CLASS_NAME;
        this.required = required;
        this.shouldResolve = shouldResolve;
    }

    @NotNull
    public String getContainingClassName() {
        return className;
    }

    @NotNull
    @Override
    public List<String> getParamTypes() {
        return paramTypes;
    }

    @NotNull
    @Override
    public List<String> getSelectorStrings() {
        return selectors;
    }

    @NotNull
    @Override
    public String getSelectorString() {
        return selectorString;
    }

    @Override
    @NotNull
    public ObjJClassType getReturnType() {
        return ObjJClassType.getClassType(getReturnTypeAsString());
    }

    @NotNull
    @Override
    public String getReturnTypeAsString() {
        return returnType;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean shouldResolve() {
        return shouldResolve;
    }
}
