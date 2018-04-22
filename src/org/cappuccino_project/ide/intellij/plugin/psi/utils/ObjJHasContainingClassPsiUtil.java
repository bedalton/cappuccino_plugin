package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.psi.PsiElement;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodHeader;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelector;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelectorLiteral;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJSelectorLiteralStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodCallPsiUtil.isUniversalMethodCaller;

public class ObjJHasContainingClassPsiUtil {

    public static boolean isSimilarClass(@NotNull final String methodContainingClass, @NotNull final String className) {
        return isUniversalMethodCaller(className) || isUniversalMethodCaller(methodContainingClass) || Objects.equals(className, methodContainingClass);
    }

    @Nullable
    public static ObjJClassDeclarationElement getContainingClass(@Nullable
                                                                         PsiElement element) {
        return ObjJTreeUtil.getParentOfType(element, ObjJClassDeclarationElement.class);
    }

    @NotNull
    public static String getContainingClassName(ObjJMethodHeader methodHeader) {
        if (methodHeader.getStub() != null) {
            return methodHeader.getStub().getContainingClassName();
        }
        ObjJClassDeclarationElement containingClass = methodHeader.getContainingClass();
        return containingClass != null ? containingClass.getClassType().getClassName() : ObjJClassType.UNDEF_CLASS_NAME;
    }

    @NotNull
    public static String getContainingClassName(ObjJCompositeElement compositeElement) {
        final ObjJClassDeclarationElement classDeclarationElement = getContainingClass(compositeElement);
        if (classDeclarationElement == null) {
            return ObjJClassType.UNDEF.getClassName();
        }
        return classDeclarationElement.getClassNameString();
    }

    @NotNull
    public static String getContainingClassName(@Nullable ObjJClassDeclarationElement classDeclarationElement) {
        return classDeclarationElement != null ? classDeclarationElement.getClassNameString() : ObjJClassType.UNDEF_CLASS_NAME;
    }

    @NotNull
    public static String getContainingClassName(@Nullable
                                                        ObjJSelectorLiteral selectorLiteral) {
        if (selectorLiteral == null) {
            return ObjJClassType.UNDEF_CLASS_NAME;
        }
        ObjJSelectorLiteralStub stub = selectorLiteral.getStub();
        if (stub != null) {
            return stub.getContainingClassName();
        }
        return getContainingClassName(selectorLiteral.getContainingClass());
    }
    @NotNull
    public static List<String> getContainingClassNamesFromSelector(@NotNull List<ObjJSelector> elements) {
        List<String> out = new ArrayList<>();
        for (ObjJSelector element : elements) {

        }
        return out;
    }

    @NotNull
    public static List<String> getContainingClassNames(@NotNull List<PsiElement> elements) {
        List<String> out = new ArrayList<>();
        for (PsiElement element : elements) {
            String className = null;
            if (elements instanceof ObjJHasContainingClass) {
                className = ((ObjJHasContainingClass)element).getContainingClassName();
            } else if (element instanceof  ObjJCompositeElement){
                className = getContainingClassName((ObjJCompositeElement)element);
            }
            if (className != null && !out.contains(className)) {
                out.add(className);
            }
        }
        return out;
    }

    public static boolean sharesContainingClass(@NotNull String className, @NotNull PsiElement... elements) {
        for (PsiElement element : elements) {
            if (!ObjJHasContainingClassPsiUtil.hasContainingClass(element, className)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasContainingClass(@Nullable PsiElement element, @NotNull String className) {
        return  className.equals(ObjJClassType.UNDETERMINED) ||
                (element != null && element instanceof ObjJHasContainingClass && ((ObjJHasContainingClass) element).getContainingClassName().equals(className));
    }



    public static String getContainingClassOrFileName(PsiElement psiElement) {
        String containingClassName = null;
        if (psiElement instanceof ObjJHasContainingClass) {
            containingClassName = ((ObjJHasContainingClass)psiElement).getContainingClassName();
        }
        if (containingClassName == null) {
            ObjJClassDeclarationElement classDeclarationElement = ObjJTreeUtil.getParentOfType(psiElement, ObjJClassDeclarationElement.class);
            if (classDeclarationElement != null) {
                containingClassName = classDeclarationElement.getClassNameString();
            }
        }
        if (containingClassName == null) {
            containingClassName = psiElement.getContainingFile().getVirtualFile().getName();
        }
        return containingClassName;
    }

    public static String getContainingSuperClassName(@NotNull ObjJCompositeElement element) {
        ObjJClassDeclarationElement classDeclarationElement = getContainingClass(element);
        if (classDeclarationElement == null || !(classDeclarationElement instanceof ObjJImplementationDeclaration)) {
            return null;
        }
        return ObjJClassDeclarationPsiUtil.getSuperClassName((ObjJImplementationDeclaration)classDeclarationElement);
    }


}
