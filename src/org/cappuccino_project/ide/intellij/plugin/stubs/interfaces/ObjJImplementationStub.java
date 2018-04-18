package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces;

import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImplementationDeclarationImpl;

import javax.annotation.Nullable;

public interface ObjJImplementationStub extends ObjJClassDeclarationStub<ObjJImplementationDeclarationImpl> {

    @Nullable
    String getSuperClassName();
    @Nullable
    String getCategoryName();
    boolean isCategory();

}
