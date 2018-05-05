package org.cappuccino_project.ide.intellij.plugin.contributor;

import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.idea.ActionsBundle;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJClassName;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodHeader;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ObjJGoToSuper implements LanguageCodeInsightActionHandler {
    @Override
    public boolean isValidFor(Editor editor, PsiFile psiFile) {
        return psiFile instanceof ObjJFile;
    }

    @Override
    public void invoke(
            @NotNull
                    Project project,
            @NotNull
                    Editor editor,
            @NotNull
                    PsiFile psiFile) {
        PsiElement focusedElement = psiFile.findElementAt(editor.getCaretModel().getOffset());
        if (focusedElement == null) {
            return;
        }

        FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.goto.super");
        NavigatablePsiElement navigatablePsiElement = getSuperElement(focusedElement);
        if (navigatablePsiElement != null) {
            navigatablePsiElement.navigate(true);
        }
    }

    private NavigatablePsiElement getSuperElement(@NotNull PsiElement element) {
        if (!(element instanceof ObjJClassName)) {
            element = ObjJTreeUtil.getNonStrictParentOfType(element, ObjJClassName.class, ObjJMethodHeader.class);
        }
        if (element instanceof ObjJClassName) {
            final ObjJClassName className = ((ObjJClassName)element);
            if (className.getParent() instanceof ObjJImplementationDeclaration) {
                final ObjJImplementationDeclaration implementationDeclaration = ((ObjJImplementationDeclaration)className.getParent());
                if (implementationDeclaration.isCategory()) {
                    for (ObjJImplementationDeclaration loopDeclaration : ObjJImplementationDeclarationsIndex.getInstance().get(className.getText(), className.getProject())) {
                        if (loopDeclaration.isCategory() || loopDeclaration.isEquivalentTo(implementationDeclaration)) {
                            continue;
                        }
                        return (NavigatablePsiElement)(loopDeclaration.getClassName() != null ? loopDeclaration.getClassName().getNavigationElement() : loopDeclaration.getNavigationElement());
                    }
                }
            }
        }
        if (element instanceof ObjJMethodHeader) {
            final ObjJMethodHeader methodHeader = ((ObjJMethodHeader)element);
            final ObjJClassDeclarationElement classDeclarationElement = methodHeader.getContainingClass();
            if (classDeclarationElement == null) {
                return null;
            }
            if (!(classDeclarationElement instanceof ObjJImplementationDeclaration)) {
                return null;
            }
            final List<String> protocolNames = classDeclarationElement.getInheritedProtocols();
            for (ObjJMethodHeaderDeclaration currentMethodHeader : ObjJUnifiedMethodIndex.getInstance().get(methodHeader.getSelectorString(), element.getProject())) {
                if (protocolNames.contains(currentMethodHeader.getContainingClassName())) {
                    return ((NavigatablePsiElement)currentMethodHeader);
                }
            }
        }
        return null;
    }

    public void update(@NotNull Editor editor, @NotNull PsiFile file, Presentation presentation) {
        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        PsiElement containingElement = element instanceof ObjJClassName ? element : ObjJTreeUtil.getNonStrictParentOfType(element,  ObjJClassName.class, ObjJMethodDeclaration.class, ObjJClassDeclarationElement.class);
        if (containingElement instanceof  ObjJMethodHeader) {
            presentation.setText(ActionsBundle.actionText("GotoSuperMethod"));
            presentation.setDescription(ActionsBundle.actionDescription("GotoSuperMethod"));
        } else {
            presentation.setText(ActionsBundle.actionText("GotoSuperClass"));
            presentation.setDescription(ActionsBundle.actionDescription("GotoSuperClass"));
        }

    }

    @Override
    public boolean startInWriteAction () { return false; }
}
