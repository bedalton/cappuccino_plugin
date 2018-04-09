package org.cappuccino_project.ide.intellij.plugin.references.presentation;

import com.intellij.navigation.ItemPresentation;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelector;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ObjJSelectorItemPresentation implements ItemPresentation {

    private final ObjJSelector selector;

    public ObjJSelectorItemPresentation(@NotNull ObjJSelector selector) {
        this.selector = selector;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return ObjJPsiImplUtil.getDescriptiveText(selector);
    }

    @Nullable
    @Override
    public String getLocationString() {
        String className;
        ObjJClassDeclarationElement classDeclarationElement = selector.getContainingClass();
        if (classDeclarationElement instanceof ObjJImplementationDeclaration) {
            ObjJImplementationDeclaration implementationDeclaration = ((ObjJImplementationDeclaration) classDeclarationElement);
            className = classDeclarationElement.getClassNameString() + (implementationDeclaration.getCategoryName() != null ? " (" + implementationDeclaration.getCategoryName().getClassName() + ")" : "");
        } else {
            className = classDeclarationElement != null ? classDeclarationElement.getClassNameString() : null;
        }
        String fileName = ObjJPsiImplUtil.getFileName(selector);
        return (className != null ? className : "") + (className != null && fileName != null ? " " : "" ) + (fileName != null ? "in " + fileName : "");
    }

    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return null;
    }
}
