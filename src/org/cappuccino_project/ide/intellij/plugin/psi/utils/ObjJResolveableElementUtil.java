package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJBlockComment;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJComment;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJResolveableElement;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJResolveableStub;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjJResolveableElementUtil {
    private static final Logger LOGGER = Logger.getLogger(ObjJResolveableElementUtil.class.getCanonicalName());

    public static <PsiT extends PsiElement> List<PsiT> onlyResolveableElements(List<PsiT> elements, PsiFile file) {
        List<PsiT> out = new ArrayList<>();
        if (elements == null) {
            return out;
        }
        for (PsiT element : elements) {
            if (shouldResolve(element) || file.isEquivalentTo(element.getContainingFile())) {
                out.add(element);
            }
        }
        return out;
    }


    public static <PsiT extends ObjJResolveableElement> List<PsiT> onlyResolveables(List<PsiT> elements, @NotNull PsiFile file) {
        List<PsiT> out = new ArrayList<>();
        if (elements == null) {
            return out;
        }
        for (PsiT element : elements) {
            ObjJResolveableStub stub = (ObjJResolveableStub)element.getStub();
            if (((stub != null && stub.shouldResolve()) || element.shouldResolve()) || file.isEquivalentTo(element.getContainingFile())) {
                out.add(element);
            }
        }
        return out;
    }

    public static boolean shouldResolve(@Nullable
                                                PsiElement psiElement) {
        if (psiElement == null) {
            return false;
        }
        return shouldResolve(psiElement, "Ignoring "+psiElement.getNode().getElementType().toString()+ " in file: " + ObjJFileUtil.getContainingFileName(psiElement));
    }

    public static boolean shouldResolve(@Nullable ObjJClassDeclarationElement psiElement) {
        if (psiElement == null) {
            return false;
        }
        return shouldResolve(psiElement, "Ignoring " +(psiElement instanceof ObjJImplementationDeclaration ? "class" : "protocol") + " " + psiElement.getClassNameString());
    }

    public static boolean shouldResolve(@Nullable
                                                PsiElement psiElement, @Nullable String shouldNotResolveLoggingStatement) {
        if (psiElement == null) {
            return false;
        }
        StubElement stubElement = psiElement instanceof StubBasedPsiElement ? ((StubBasedPsiElement)psiElement).getStub() : null;
        if (stubElement instanceof ObjJResolveableStub) {
            return ((ObjJResolveableStub)stubElement).shouldResolve();
        }
        PsiElement previousSibling = ObjJTreeUtil.getPreviousNonEmptySibling(psiElement, true);
        final boolean previousSiblingIsComment = previousSibling instanceof ObjJBlockComment || previousSibling instanceof ObjJComment;
        if (!previousSiblingIsComment) {
            return true;
        }
        boolean shouldResolveThisElement = !previousSibling.getText().contains("@ignore");
        if (!shouldResolveThisElement) {
            if (shouldNotResolveLoggingStatement != null) {
                LOGGER.log(Level.INFO, shouldNotResolveLoggingStatement + "; Comment <"+previousSibling.getText()+">");
            } else {
                LOGGER.log(Level.INFO, "Ignoring element of type <"+psiElement.getNode().getElementType().toString()+"> in file: <"+ObjJFileUtil.getContainingFileName(psiElement)+">");
            }
            return false;
        }
        ObjJResolveableElement parentResolveableElement = ObjJTreeUtil.getParentOfType(psiElement, ObjJResolveableElement.class);
        return parentResolveableElement == null || parentResolveableElement.shouldResolve();
    }

    public static boolean shouldResolve(@Nullable ObjJHasContainingClass hasContainingClass) {
        return shouldResolve((PsiElement)hasContainingClass) && (hasContainingClass.getContainingClass() == null || shouldResolve(hasContainingClass.getContainingClass()));
    }
}
