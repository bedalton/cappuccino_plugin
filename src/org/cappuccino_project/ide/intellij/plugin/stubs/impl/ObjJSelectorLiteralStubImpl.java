package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJSelectorLiteralImpl;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJSelectorLiteralStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils;
import org.jetbrains.annotations.NotNull;
import java.util.Collections;
import java.util.List;

public class ObjJSelectorLiteralStubImpl extends ObjJStubBaseImpl<ObjJSelectorLiteralImpl> implements ObjJSelectorLiteralStub {

    private final String containingClass;
    private final String selector;
    private final List<String> selectors;
    private final boolean shouldResolve;

    public ObjJSelectorLiteralStubImpl(@NotNull StubElement parent, @NotNull String containingClass, @NotNull List<String> selectorStrings, final boolean shouldResolve) {
        super(parent, ObjJStubTypes.SELECTOR_LITERAL);
        this.containingClass = containingClass;
        this.selectors = selectorStrings;
        this.selector = ArrayUtils.join(selectorStrings, ObjJMethodPsiUtils.SELECTOR_SYMBOL, true);
        this.shouldResolve = shouldResolve;
    }

    @NotNull
    @Override
    public List<String> getParamTypes() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<String> getSelectorStrings() {
        return selectors;
    }

    @NotNull
    @Override
    public String getSelectorString() {
        return selector;
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
        return ObjJClassType.UNDEF;
    }

    @NotNull
    @Override
    public String getReturnTypeAsString() {
        return getReturnType().getClassName();
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
