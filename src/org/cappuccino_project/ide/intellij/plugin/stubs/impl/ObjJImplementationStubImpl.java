package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImplementationDeclarationImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImplementationStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ObjJImplementationStubImpl extends ObjJClassDeclarationStubImpl<ObjJImplementationDeclarationImpl> implements ObjJImplementationStub {
    private final String categoryName;
    private final String superClassName;
    public ObjJImplementationStubImpl(
            @NotNull
                    StubElement parent, @NotNull  String className, @Nullable String superClassName, @Nullable String categoryName, @NotNull List<String> protocols, boolean shouldResolve) {
        super(parent, ObjJStubTypes.IMPLEMENTATION, className, protocols, shouldResolve);
        this.superClassName = superClassName;
        this.categoryName = categoryName;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public boolean isCategory() {
        return categoryName != null;
    }

    @Nullable
    public String getCategoryName() {
        return categoryName;
    }
}
