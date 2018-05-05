package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJInstanceVariableDeclarationImpl;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJInstanceVariableDeclarationStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJInstanceVariableDeclarationStubImpl extends ObjJStubBaseImpl<ObjJInstanceVariableDeclarationImpl> implements ObjJInstanceVariableDeclarationStub {

    private final String containingClass;
    private final String varType;
    private final String variableName;
    private final String getter;
    private final String setter;
    private final boolean shouldResolve;

    public ObjJInstanceVariableDeclarationStubImpl(StubElement parent, String containingClass, @NotNull String varType, @NotNull String variableName, @Nullable String getter, @Nullable String setter,final boolean shouldResolve) {
        super(parent, ObjJStubTypes.INSTANCE_VAR);
        this.containingClass = containingClass != null ? containingClass : ObjJClassType.UNDEF_CLASS_NAME;
        this.varType = varType;
        this.variableName = variableName;
        this.getter = getter != null && getter.length() > 0 ? getter : null;
        this.setter = setter != null && setter.length() > 0 ? setter : null;
        this.shouldResolve = shouldResolve;
    }

    @NotNull
    @Override
    public String getContainingClass() {
        return containingClass;
    }

    @NotNull
    @Override
    public String getVariableName() {
        return variableName;
    }

    @NotNull
    @Override
    public String getVarType() {
        return varType;
    }

    @Nullable
    @Override
    public String getGetter() {
        return getter;
    }

    @Nullable
    @Override
    public String getSetter() {
        return setter;
    }

    @Override
    public boolean shouldResolve() {
        return shouldResolve;
    }

}
