package org.cappuccino_project.ide.intellij.plugin.contributor;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import org.cappuccino_project.ide.intellij.plugin.contributor.utils.ObjJSelectorLookupUtil;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJInstanceVariablesByNameIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil;
import org.cappuccino_project.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil.SelectorResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cappuccino_project.ide.intellij.plugin.contributor.utils.ObjJSelectorLookupUtil.addSelectorLookupElement;

public class ObjJMethodCallCompletionContributorUtil {

    private static final Logger LOGGER = Logger.getLogger(ObjJMethodCallCompletionContributorUtil.class.getName());
    public static final String CARET_INDICATOR = ObjJCompletionContributor.CARET_INDICATOR;

    /**
     * Method to manage adding selector lookup elements to result set
     * @param result completion result set
     * @param psiElement currently editing psi element
     */
    public static void addSelectorLookupElementsFromSelectorList(@NotNull
                                                         CompletionResultSet result, @Nullable PsiElement psiElement) {
        if (psiElement == null) {
            return;
        }
        ObjJMethodCall methodCall = ObjJTreeUtil.getParentOfType(psiElement, ObjJMethodCall.class);
        if (methodCall == null) {
            return;
        }
        addMethodCallCompletions(result, psiElement, methodCall);

    }

    private static void addMethodCallCompletions(@NotNull
                                                         CompletionResultSet result, @NotNull PsiElement psiElement, @Nullable ObjJMethodCall elementsParentMethodCall) {
        if (elementsParentMethodCall == null) {
            return;
        }
        List<ObjJSelector> selectors = getSelectorsFromIncompleteMethodCall(psiElement, elementsParentMethodCall);
        if (selectors.isEmpty()) {
            return;
        }
        String selectorString = ObjJMethodPsiUtils.getSelectorStringFromSelectorList(selectors);
        int selectorIndex = selectors.size() - 1;
        String thisSelectorTrimmedText = psiElement.getText().replaceAll("\\s+", "");
        for (int i=0;i<selectors.size();i++) {
            if (selectors.get(i) != null && selectors.get(i).getText().equals(thisSelectorTrimmedText)) {
                selectorIndex = i;
                break;
            }
        }
        ObjJSelector selector = selectorIndex >= 0 && selectorIndex < selectors.size() ? selectors.get(selectorIndex) : null;
        addMethodDeclarationLookupElements(result, selectors, selectorIndex);
        if (selectors.size() == 1 && !DumbService.isDumb(psiElement.getProject())) {
            addInstanceVariableLookupElements(result, psiElement.getProject(), selector != null ? ObjJSelectorReferenceResolveUtil.getClassConstraints(selector) : Collections.emptyList() , selectorString);
        }
    }

    @Nullable
    public static TextRange getIncompleteMethodCallTextRange(ObjJMethodCall methodCall) {
        int startingRange = methodCall.getTextRange().getStartOffset();
        int methodCallTextLength = methodCall.getText().replace(CARET_INDICATOR, "").length();
        List<PsiElement> methodCallChildren = getIncompleteMethodCallPsiElements(methodCall);
        PsiErrorElement errorElement = ObjJTreeUtil.getChildOfType(methodCall, PsiErrorElement.class);
        int endRange = errorElement != null ? errorElement.getTextRange().getStartOffset() : !methodCallChildren.isEmpty() ? methodCallChildren.get(methodCallChildren.size()-1).getTextRange().getEndOffset() : -1;
        if (endRange > startingRange+methodCallTextLength) {
            endRange = startingRange+methodCallTextLength;
        }
        return endRange > -1 ? TextRange.create(startingRange, endRange) : null;
    }

    /**
     * Gets selectors from incomplete method call
     * Used while building completion results
     * @param psiElement currently editing psi element
     * @return array of selectors in this editing psi element
     */
    private static List<ObjJSelector> getSelectorsFromIncompleteMethodCall(@NotNull PsiElement psiElement, @Nullable ObjJMethodCall selectorParentMethodCall) {
        final Project project = psiElement.getProject();

        //Check to ensure this element is part of a method
        if (selectorParentMethodCall == null) {
            //LOGGER.log(Level.INFO, "PsiElement is not a selector in a method call element");
            return Collections.emptyList();
        }

        // If element is selector or direct parent is a selector,
        // then element is well formed, and can return the basic selector list
        // which will hold selectors up to self, which is what is needed for completion
        int selectorIndex;
        final List<ObjJSelector> selectors = selectorParentMethodCall.getSelectorList();
        if (psiElement instanceof ObjJSelector || psiElement.getParent() instanceof ObjJSelector) {
            return selectors;
        }

        // If psi parent is a qualified method call,
        // find it's index in selector array for autocompletion
        if (psiElement.getParent() instanceof ObjJQualifiedMethodCallSelector) {
            final ObjJQualifiedMethodCallSelector qualifiedMethodCallSelector = ((ObjJQualifiedMethodCallSelector) psiElement.getParent());
            if (qualifiedMethodCallSelector.getSelector() != null) {
                selectorIndex = selectors.indexOf(qualifiedMethodCallSelector.getSelector());
            } else {
                selectorIndex = selectors.size() - 1;
            }
        } else {
            selectorIndex = selectors.size() - 1;
        }

        // Find orphaned elements in method call
        // and create selector elements for later use.
        final List<PsiElement> orphanedSelectors = ObjJTreeUtil.getChildrenOfType(psiElement.getParent(), ObjJTypes.ObjJ_ID);
        for (PsiElement subSelector : orphanedSelectors) {
            if (subSelector.getText().isEmpty()) {
                continue;
            }
            selectors.add(++selectorIndex, ObjJElementFactory.createSelector(project, subSelector.getText().replaceAll("\\s+", "")));
        }
        //Finally add the current psi element as a selector
        selectors.add(ObjJElementFactory.createSelector(project, psiElement.getText().replaceAll("\\s+", "")));
        return selectors;
    }

    private static List<PsiElement> getIncompleteMethodCallPsiElements(ObjJMethodCall methodCall) {
        final List<PsiElement> selectors = new ArrayList<>(methodCall.getSelectorList());
        selectors.addAll(ObjJTreeUtil.getChildrenOfType(methodCall, ObjJTypes.ObjJ_ID));
        return selectors;
    }

    /**
     * Adds lookup elements from matching method headers
     * @param result completion result set
     * @param selectors current method call selector list
     * @param selectorIndex index of currently editing selector
     */
    private static void addMethodDeclarationLookupElements(@NotNull CompletionResultSet result, @NotNull List<ObjJSelector> selectors, int selectorIndex) {

        String selectorString = ObjJMethodPsiUtils.getSelectorStringFromSelectorList(selectors);

        ObjJSelectorReferenceResolveUtil.SelectorResolveResult<ObjJSelector> resolveResult = ObjJSelectorReferenceResolveUtil.resolveSelectorReferenceAsPsiElement(selectors, selectorIndex);
        if (resolveResult == null) {
            if (selectors.size()<=selectorIndex) {
                return;
            }
            SelectorResolveResult<ObjJSelector> selectorResolveResult = ObjJSelectorReferenceResolveUtil.getSelectorLiteralReferences(selectors.get(selectorIndex));
            if (selectorResolveResult.isEmpty()) {
                return;
            }
            for (ObjJSelector selector : selectorResolveResult.getNaturalResult()) {
                ProgressIndicatorProvider.checkCanceled();
                //LOGGER.log(Level.INFO, "Adding natural result: <"+selector.getText()+">");
                addSelectorLookupElement(result, selector, selectorIndex, ObjJCompletionContributor.TARGETTED_METHOD_SUGGESTION_PRIORITY);
            }
            for (ObjJSelector selector : selectorResolveResult.getOtherResult()) {
                ProgressIndicatorProvider.checkCanceled();
                //LOGGER.log(Level.INFO, "Adding other result: <"+selector.getText()+">");
                addSelectorLookupElement(result, selector, selectorIndex, ObjJCompletionContributor.GENERIC_METHOD_SUGGESTION_PRIORITY);
            }
            return;
        }
        //LOGGER.log(Level.INFO, "Searching for selector completions. Found <"+resolveResult.getNaturalResult().size()+"> natural results and <"+resolveResult.getOtherResult().size()+"> general results, for selector: <"+selectorString+">");
        addSelectorLookupElementsFromSelectorList(result, resolveResult.getNaturalResult(), selectorIndex, ObjJCompletionContributor.TARGETTED_METHOD_SUGGESTION_PRIORITY);
        addSelectorLookupElementsFromSelectorList(result, resolveResult.getOtherResult(), selectorIndex, ObjJCompletionContributor.GENERIC_METHOD_SUGGESTION_PRIORITY);
    }

    /**
     * Adds selector lookup elements from a list of possible selector completions
     * @param resultSet completion result set
     * @param elements selectors to add to completion set
     * @param selectorIndex index of currently editing selector
     * @param priority suggestion placement priority
     */
    private static void addSelectorLookupElementsFromSelectorList(CompletionResultSet resultSet, List<ObjJSelector> elements, int selectorIndex, double priority) {
        for (ObjJSelector selector : elements) {
            ProgressIndicatorProvider.checkCanceled();
            ObjJSelectorLookupUtil.addSelectorLookupElement(resultSet, selector, selectorIndex, priority);
        }
    }

    /**
     * adds instance variables as lookup elements to result set
     * @param result completion result set
     * @param project containing project
     * @param possibleContainingClassNames possible class names to determine completion priority
     * @param selectorString selector string to match
     */
    private static void addInstanceVariableLookupElements(@NotNull CompletionResultSet result, @NotNull Project project, @NotNull List<String> possibleContainingClassNames, @Nullable String selectorString) {
        if (selectorString == null || selectorString.isEmpty()) {
            return;
        }
        final int caretIndicatorIndex = selectorString.indexOf(CARET_INDICATOR);
        List<ObjJInstanceVariableDeclaration> declarations;
        if (caretIndicatorIndex >= 0) {
            declarations = ObjJInstanceVariablesByNameIndex.getInstance().getByPatternFlat(selectorString.replace(CARET_INDICATOR, "(.*)"), project);
        } else {
            declarations = ObjJInstanceVariablesByNameIndex.getInstance().get(selectorString, project);
        }
        for (ObjJInstanceVariableDeclaration instanceVariableDeclaration : declarations) {
            ProgressIndicatorProvider.checkCanceled();
            if (instanceVariableDeclaration.getVariableName() == null) {
                continue;
            }
            //ProgressIndicatorProvider.checkCanceled();
            double priority = possibleContainingClassNames.contains(instanceVariableDeclaration.getContainingClassName()) ?
                    ObjJCompletionContributor.TARGETTED_INSTANCE_VAR_SUGGESTION_PRIORITY : ObjJCompletionContributor.GENERIC_INSTANCE_VARIABLE_SUGGESTION_PRIORITY;
            addSelectorLookupElement(result, instanceVariableDeclaration.getVariableName(),"<"+instanceVariableDeclaration.getFormalVariableType()+">", priority,false, null);
        }
    }

}
