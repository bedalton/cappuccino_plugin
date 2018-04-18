package org.cappuccino_project.ide.intellij.plugin.contributor;

import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.references.ObjJClassNameReference;
import org.cappuccino_project.ide.intellij.plugin.references.ObjJFunctionNameReference;
import org.cappuccino_project.ide.intellij.plugin.references.ObjJSelectorReference;
import org.cappuccino_project.ide.intellij.plugin.references.ObjJVariableReference;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class ObjJReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(
            @NotNull
                    PsiReferenceRegistrar psiReferenceRegistrar) {
        //Selector
        PsiElementPattern.Capture<ObjJSelector> selector = psiElement(ObjJSelector.class);
        psiReferenceRegistrar.registerReferenceProvider(selector, new SelectorReferenceProvider());

        //ClassName
        PsiElementPattern.Capture<ObjJClassName> classNameCapture = psiElement(ObjJClassName.class);
        psiReferenceRegistrar.registerReferenceProvider(classNameCapture, new ClassNameReferenceProvider());

        //VariableName
        PsiElementPattern.Capture<ObjJVariableName> variableName = psiElement(ObjJVariableName.class);
        psiReferenceRegistrar.registerReferenceProvider(variableName, new VariableNameReferenceProvider());

        PsiElementPattern.Capture<ObjJFunctionName> functionName = psiElement(ObjJFunctionName.class);
        psiReferenceRegistrar.registerReferenceProvider(functionName, new FunctionNameReferenceProvider());

        //After
     //   LOGGER.log(Level.INFO, "Registered Providers");
    }


    private static class SelectorReferenceProvider extends PsiReferenceProvider {

        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(
                @NotNull
                        PsiElement psiElement,
                @NotNull
                        ProcessingContext processingContext) {
            return psiElement instanceof ObjJSelector ? new PsiReference[]{createReference((ObjJSelector)psiElement)} : PsiReference.EMPTY_ARRAY;
        }
        @NotNull
        private PsiReference createReference(ObjJSelector selector) {
            return new ObjJSelectorReference(selector);
        }

        @Override
        public boolean acceptsTarget(@NotNull PsiElement target) {
            return target instanceof ObjJSelector;
        }
    }


    private static class FunctionNameReferenceProvider extends PsiReferenceProvider {

        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(
                @NotNull
                        PsiElement psiElement,
                @NotNull
                        ProcessingContext processingContext) {
            return psiElement instanceof ObjJFunctionName ? new PsiReference[] {createReference((ObjJFunctionName) psiElement)} : PsiReference.EMPTY_ARRAY;
        }
        @NotNull
        private PsiReference createReference(ObjJFunctionName functionName) {
            return new ObjJFunctionNameReference(functionName);
        }

        @Override
        public boolean acceptsTarget(@NotNull PsiElement target) {
            return target instanceof ObjJFunctionName;
        }
    }


    private static class ClassNameReferenceProvider extends PsiReferenceProvider {

        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(
                @NotNull
                        PsiElement psiElement,
                @NotNull
                        ProcessingContext processingContext) {
            return psiElement instanceof ObjJClassName ? new PsiReference[]{createReference((ObjJClassName)psiElement)} : PsiReference.EMPTY_ARRAY;
        }
        @NotNull
        private PsiReference createReference(ObjJClassName className) {
            return new ObjJClassNameReference(className);
        }

        @Override
        public boolean acceptsTarget(@NotNull PsiElement target) {
            return target instanceof ObjJClassName;
        }
    }

    private static class VariableNameReferenceProvider extends PsiReferenceProvider {

        @NotNull
        @Override
        public PsiReference[] getReferencesByElement(
                @NotNull
                        PsiElement psiElement,
                @NotNull
                        ProcessingContext processingContext) {
            return psiElement instanceof ObjJClassName ? new PsiReference[]{createReference((ObjJVariableName)psiElement)} : PsiReference.EMPTY_ARRAY;
        }
        @NotNull
        private PsiReference createReference(ObjJVariableName var) {
            return new ObjJVariableReference(var);
        }

        @Override
        public boolean acceptsTarget(@NotNull PsiElement target) {
            return target instanceof ObjJVariableName && ObjJTreeUtil.getParentOfType(target, ObjJFunctionCall.class) == null;
        }
    }

}
