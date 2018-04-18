package org.cappuccino_project.ide.intellij.plugin.references;

import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionUtil;
import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageViewLongNameLocation;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelector;
import org.jetbrains.annotations.NotNull;

public class ObjJElementDescriptionProvider {

    public static final ObjJElementDescriptionProvider INSTANCE = new ObjJElementDescriptionProvider();

    private ObjJElementDescriptionProvider() {}

    @NotNull
    public static String getElementDescription(
            @NotNull
                    PsiElement psiElement,
            @NotNull
                    ElementDescriptionLocation elementDescriptionLocation) {
        if (psiElement instanceof ObjJSelector) {
            return getElementDescription((ObjJSelector) psiElement, elementDescriptionLocation);
        }
        return ElementDescriptionUtil.getElementDescription(psiElement, elementDescriptionLocation);
    }

    @NotNull
    private static String getElementDescription(@NotNull ObjJSelector selector,
                                         @NotNull
                                                 ElementDescriptionLocation elementDescriptionLocation ) {
        if (elementDescriptionLocation instanceof UsageViewLongNameLocation) {
            return selector.getSelectorString(true);
        }
        return ElementDescriptionUtil.getElementDescription(selector, elementDescriptionLocation);
    }
}
