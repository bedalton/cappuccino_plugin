package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJClassName;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodHeader;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodCallPsiUtil;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ObjJClassDeclarationElement<StubT extends ObjJClassDeclarationStub<? extends ObjJClassDeclarationElement>> extends ObjJStubBasedElement<StubT>, ObjJIsOfClassType, ObjJHasProtocolList, ObjJCompositeElement, ObjJResolveableElement<StubT> {

    @NotNull
    @Override
    default ObjJClassType getClassType() {
        final String classNameString = getClassNameString();
        return !ObjJMethodCallPsiUtil.isUniversalMethodCaller(classNameString) ? ObjJClassType.getClassType(classNameString) : ObjJClassType.UNDEF;
    }

    @NotNull
    default String getClassNameString() {
        ObjJClassDeclarationStub stub = getStub();
        if (stub != null) {
            return stub.getClassName();
        }
        return getClassName() != null ? getClassName().getText() : ObjJClassType.UNDEF.getClassName();
    }

    @NotNull
    List<ObjJMethodHeader> getMethodHeaders();

    boolean hasMethod(@NotNull String selector);

    @Nullable
    StubT getStub();

    @Nullable
    ObjJClassName getClassName();
}
