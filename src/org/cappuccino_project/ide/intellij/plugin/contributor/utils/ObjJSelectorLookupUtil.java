package org.cappuccino_project.ide.intellij.plugin.contributor.utils;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import org.cappuccino_project.ide.intellij.plugin.contributor.handlers.ObjJSelectorInsertHandler;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJHasContainingClassPsiUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.utils.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJHasContainingClassPsiUtil.getContainingClassOrFileName;

public class ObjJSelectorLookupUtil {

    /**
     * Adds a selector lookup element
     * @param result result set
     * @param targetElement target element
     * @param priority se
     * @param tailText lookup element tail text
     * @param useInsertHandler use insert handler for colon placement
     */
    public static void addSelectorLookupElement(@NotNull
                                                         CompletionResultSet result, PsiElement targetElement, @Nullable String tailText, double priority, boolean useInsertHandler) {
        addSelectorLookupElement(result, targetElement, tailText, priority, useInsertHandler, null);
    }

    public static void addSelectorLookupElement (@NotNull
                                                         CompletionResultSet resultSet, @NotNull ObjJSelector selector, int selectorIndex, double priority) {
        String tailText = getSelectorLookupElementTailText(selector, selectorIndex);
        boolean addColonSuffix = tailText != null || selectorIndex > 0;
        String containingFileOrClassName = getContainingClassOrFileName(selector);
        addSelectorLookupElement(resultSet, selector.getText(), containingFileOrClassName, tailText != null ? tailText : "", priority, addColonSuffix, ObjJPsiImplUtil.getIcon(selector));

    }

    @Nullable
    private static String getSelectorLookupElementTailText(
            @NotNull
                    ObjJSelector selector, int selectorIndex) {
        List<String> trailingSelectors = ObjJMethodPsiUtils.getTrailingSelectorStrings(selector, selectorIndex);
        StringBuilder stringBuilder = new StringBuilder(ObjJMethodPsiUtils.SELECTOR_SYMBOL);
        String paramType = getSelectorVariableType(selector);
        if (paramType != null) {
            stringBuilder.append("(").append(paramType).append(")");
            String variableName = getSelectorVariableName(selector);
            if (variableName != null) {
                stringBuilder.append(variableName);
            }
        }
        if (!trailingSelectors.isEmpty()) {
            stringBuilder.append(" ").append(ArrayUtils.join(trailingSelectors, ObjJMethodPsiUtils.SELECTOR_SYMBOL, true));
        }
        return stringBuilder.length() > 1 ? stringBuilder.toString() : null;
    }

    @Contract("null -> null")
    @Nullable
    private static String getSelectorVariableType(@Nullable ObjJSelector selector) {
        if (selector == null) {
            return null;
        }
        ObjJMethodDeclarationSelector declarationSelector = ObjJTreeUtil.getParentOfType(selector, ObjJMethodDeclarationSelector.class);
        if (declarationSelector != null) {
            return declarationSelector.getFormalVariableType() != null ? declarationSelector.getFormalVariableType().getText() : null;
        }
        ObjJInstanceVariableDeclaration instanceVariableDeclaration = ObjJTreeUtil.getParentOfType(selector, ObjJInstanceVariableDeclaration.class);
        return instanceVariableDeclaration != null ?  instanceVariableDeclaration.getFormalVariableType().getText() : null;
    }

    @Contract("null -> null")
    @Nullable
    private static String getSelectorVariableName(@Nullable ObjJSelector selector) {
        if (selector == null) {
            return null;
        }
        ObjJMethodDeclarationSelector declarationSelector = ObjJTreeUtil.getParentOfType(selector, ObjJMethodDeclarationSelector.class);
        if (declarationSelector != null) {
            return declarationSelector.getVariableName() != null ? declarationSelector.getVariableName().getText() : null;
        }
        ObjJInstanceVariableDeclaration instanceVariableDeclaration = ObjJTreeUtil.getParentOfType(selector, ObjJInstanceVariableDeclaration.class);
        return instanceVariableDeclaration != null && instanceVariableDeclaration.getVariableName() != null ?  instanceVariableDeclaration.getVariableName().getText() : null;
    }

    /**
     * Adds selector lookup element
     * @param result result set
     * @param targetElement target element
     * @param priority se
     * @param tailText lookup element tail text
     * @param useInsertHandler use insert handler for colon placement
     * @param icon icon to use in completion list
     */
    public static void addSelectorLookupElement(@NotNull
                                                         CompletionResultSet result, @NotNull PsiElement targetElement, @Nullable String tailText, double priority, boolean useInsertHandler, @SuppressWarnings("SameParameterValue")
                                                 @Nullable
                                                         Icon icon) {
        if (!(targetElement instanceof ObjJCompositeElement)){
            return;
        }
        final String className = ObjJHasContainingClassPsiUtil.getContainingClassOrFileName(targetElement);
        final LookupElementBuilder elementBuilder = createSelectorLookupElement(targetElement.getText(), className, tailText, useInsertHandler, icon);
        result.addElement(PrioritizedLookupElement.withPriority(elementBuilder, priority));
    }


    public static void addSelectorLookupElement(@NotNull
                                                         CompletionResultSet result,@NotNull String suggestedText, @Nullable String className, @Nullable String tailText, double priority, boolean addSuffix) {
        addSelectorLookupElement(result, suggestedText, className, tailText, priority, addSuffix, null);
    }

    public static void addSelectorLookupElement(@NotNull
                                                         CompletionResultSet result, @NotNull String suggestedText, @Nullable String className, @Nullable String tailText, double priority, boolean addSuffix, @SuppressWarnings("SameParameterValue")
                                                 @Nullable Icon icon) {
        result.addElement(PrioritizedLookupElement.withPriority(createSelectorLookupElement(suggestedText, className, tailText, addSuffix, icon), priority));
    }

    @SuppressWarnings("WeakerAccess")
    public static LookupElementBuilder createSelectorLookupElement(@NotNull String suggestedText, @Nullable String className, @Nullable String tailText, boolean useInsertHandler, @Nullable Icon icon) {
        LookupElementBuilder elementBuilder = LookupElementBuilder
                .create(suggestedText);
        if (tailText != null) {
            elementBuilder = elementBuilder.withTailText(tailText);
        }
        if (className != null) {
            elementBuilder = elementBuilder.withTypeText("in "+className);
        }
        if (icon != null) {
            elementBuilder = elementBuilder.withIcon(icon);
        }
        if (useInsertHandler) {
            elementBuilder = elementBuilder.withInsertHandler(ObjJSelectorInsertHandler.getInstance());
        }
        Logger.getLogger("ObjJSelectorLookupUtil").log(Level.SEVERE, "Creating selector lookup element for <"+suggestedText+">");
        return elementBuilder;
    }

}
