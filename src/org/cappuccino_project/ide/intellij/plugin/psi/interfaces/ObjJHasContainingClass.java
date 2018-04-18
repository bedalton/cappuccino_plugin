package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import com.intellij.psi.util.PsiTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJProtocolDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ObjJHasContainingClass extends ObjJCompositeElement {

    @NotNull
    default String getContainingClassName() {
        ObjJClassDeclarationElement containingClass = getContainingClass();
        return containingClass != null ? containingClass.getClassType().getClassName() : ObjJClassType.UNDEF.getClassName();
    }

    @Nullable
    default ObjJClassDeclarationElement getContainingClass() {
        ObjJImplementationDeclaration implementationDeclaration =  PsiTreeUtil.getParentOfType(this, ObjJImplementationDeclaration.class);
        if (implementationDeclaration != null) {
            return implementationDeclaration;
        }
        ObjJProtocolDeclaration protocolDeclaration = PsiTreeUtil.getParentOfType(this, ObjJProtocolDeclaration.class);
        if (protocolDeclaration != null) {
            return protocolDeclaration;
        }
        return null;
    }

}
