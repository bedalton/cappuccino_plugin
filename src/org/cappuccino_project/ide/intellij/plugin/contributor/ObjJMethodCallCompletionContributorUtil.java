package org.cappuccino_project.ide.intellij.plugin.contributor;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.cappuccino_project.ide.intellij.plugin.contributor.utils.ObjJSelectorLookupUtil;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassInstanceVariableAccessorMethodIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJInstanceVariablesByNameIndex;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJIcons;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJResolveableElementUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil;
import org.cappuccino_project.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil.SelectorResolveResult;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    static void addSelectorLookupElementsFromSelectorList(
            @NotNull
                    CompletionResultSet result,
            @Nullable
                    PsiElement psiElement) {
        if (psiElement == null) {
            LOGGER.log(Level.SEVERE, "Cannot add selector lookup elements. Selector element is null");
            return;
        }
        ObjJMethodCall methodCall = ObjJTreeUtil.getParentOfType(psiElement, ObjJMethodCall.class);
        if (methodCall == null) {
            //LOGGER.log(Level.INFO, "Cannot get completion parameters. Method call is null.");
            return;
        }
        addMethodCallCompletions(result, psiElement, methodCall);

    }

    private static void addMethodCallCompletions(@NotNull
                                                         CompletionResultSet result, @NotNull PsiElement psiElement, @Nullable ObjJMethodCall elementsParentMethodCall) {

        //LOGGER.log(Level.INFO, "Add method call completions");
        if (elementsParentMethodCall == null) {
            LOGGER.log(Level.SEVERE, "Cannot add method call completions. Method call parent element is null");
            return;
        }
        List<ObjJSelector> selectors = getSelectorsFromIncompleteMethodCall(psiElement, elementsParentMethodCall);
        if (selectors.isEmpty()) {
            //selectors = Collections.singletonList(ObjJElementFactory.createSelector(psiElement.getProject(), psiElement.getText()));
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
            addAccessorLookupElements(result, psiElement.getProject(), selector != null ? ObjJSelectorReferenceResolveUtil.getClassConstraints(selector) : Collections.emptyList() , selectorString);
        }
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

    /**
     * Adds lookup elements from matching method headers
     * @param result completion result set
     * @param selectors current method call selector list
     * @param selectorIndex index of currently editing selector
     */
    private static void addMethodDeclarationLookupElements(@NotNull CompletionResultSet result, @NotNull List<ObjJSelector> selectors, int selectorIndex) {
        if (selectors.isEmpty()) {
            return;
        }
        PsiFile file = selectors.get(0).getContainingFile();
        String selectorString = ObjJMethodPsiUtils.getSelectorStringFromSelectorList(selectors);
        ObjJSelectorReferenceResolveUtil.SelectorResolveResult<ObjJSelector> resolveResult = ObjJSelectorReferenceResolveUtil.resolveSelectorReferenceAsPsiElement(selectors, selectorIndex);
        if (resolveResult == null) {
            //LOGGER.log(Level.INFO, "Resolve result is null");
            if (selectors.size()<=selectorIndex) {
                //LOGGER.log(Level.INFO, "Cannot add method selector elements to result set. Selector index out of bounds.");
                return;
            }
            SelectorResolveResult<ObjJSelector> selectorResolveResult = ObjJSelectorReferenceResolveUtil.getSelectorLiteralReferences(selectors.get(selectorIndex));
            if (selectorResolveResult.isEmpty()) {
                //LOGGER.log(Level.INFO, "Selector literal resolve result is empty for selector <"+selectorString+">");
                return;
            }
            for (ObjJSelector selector : ObjJResolveableElementUtil.onlyResolveableElements(selectorResolveResult.getNaturalResult(), file)) {
                ProgressIndicatorProvider.checkCanceled();
                //LOGGER.log(Level.INFO, "Adding natural result: <"+selector.getText()+">");
                addSelectorLookupElement(result, selector, selectorIndex, ObjJCompletionContributor.TARGETTED_METHOD_SUGGESTION_PRIORITY);
            }
            for (ObjJSelector selector : ObjJResolveableElementUtil.onlyResolveableElements(selectorResolveResult.getOtherResult(), file)) {
                ProgressIndicatorProvider.checkCanceled();
                //LOGGER.log(Level.INFO, "Adding other result: <"+selector.getText()+">");
                addSelectorLookupElement(result, selector, selectorIndex, ObjJCompletionContributor.GENERIC_METHOD_SUGGESTION_PRIORITY);
            }
            return;
        }
        LOGGER.log(Level.INFO, "Searching for selector completions. Found <"+resolveResult.getNaturalResult().size()+"> natural results and <"+resolveResult.getOtherResult().size()+"> general results, for selector: <"+selectorString+">");
        addSelectorLookupElementsFromSelectorList(result, ObjJResolveableElementUtil.onlyResolveableElements(resolveResult.getNaturalResult(), file), selectorIndex, ObjJCompletionContributor.TARGETTED_METHOD_SUGGESTION_PRIORITY);
        addSelectorLookupElementsFromSelectorList(result, ObjJResolveableElementUtil.onlyResolveableElements(resolveResult.getOtherResult(), file), selectorIndex, ObjJCompletionContributor.GENERIC_METHOD_SUGGESTION_PRIORITY);
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
            //LOGGER.log(Level.SEVERE, "Adding Selector lookup element: <"+selector.getText()+">");
            ObjJInstanceVariableDeclaration instanceVariableDeclaration = selector.getParentOfType(ObjJInstanceVariableDeclaration.class);
            if (instanceVariableDeclaration != null) {
                addInstanceVariableAccessorMethods(resultSet, instanceVariableDeclaration, priority);
            } else {
                ObjJSelectorLookupUtil.addSelectorLookupElement(resultSet, selector, selectorIndex, priority);
            }
        }
    }


    /**
     * adds instance variables as lookup elements to result set
     * @param result completion result set
     * @param project containing project
     * @param possibleContainingClassNames possible class names to determine completion priority
     * @param selectorString selector string to match
     */
    private static void addAccessorLookupElements(@NotNull CompletionResultSet result, @NotNull Project project, @NotNull List<String> possibleContainingClassNames, @Nullable String selectorString) {
        if (selectorString == null || selectorString.isEmpty()) {
            return;
        }
        final int caretIndicatorIndex = selectorString.indexOf(CARET_INDICATOR);
        selectorString = caretIndicatorIndex >= 0 ? selectorString.replace(CARET_INDICATOR, "(.*)") : selectorString;
        List<ObjJInstanceVariableDeclaration> declarations;
        if (caretIndicatorIndex >= 0) {
            declarations = ObjJInstanceVariablesByNameIndex.getInstance().getByPatternFlat(selectorString, project);
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

            String className = instanceVariableDeclaration.getContainingClassName();
            String variableName = instanceVariableDeclaration.getVariableName().getName();
            String variableType = instanceVariableDeclaration.getFormalVariableType().getText();
            addSelectorLookupElement(result, variableName, className, "<" + variableType + ">", priority, false, ObjJIcons.VARIABLE_ICON);
        }

        List<ObjJInstanceVariableDeclaration> accessors;
        if (caretIndicatorIndex >= 0) {
            accessors = ObjJClassInstanceVariableAccessorMethodIndex.getInstance().getByPatternFlat(selectorString, project);
        } else {
            accessors = ObjJClassInstanceVariableAccessorMethodIndex.getInstance().get(selectorString, project);
        }
        //LOGGER.log(Level.INFO, "Found <"+accessors.size()+"> accessor properties");
        for (ObjJInstanceVariableDeclaration instanceVariable : accessors) {

            String className = instanceVariable.getContainingClassName();
            double priority = possibleContainingClassNames.contains(className) ?
                    ObjJCompletionContributor.TARGETTED_METHOD_SUGGESTION_PRIORITY  : ObjJCompletionContributor.GENERIC_METHOD_SUGGESTION_PRIORITY ;
            addInstanceVariableAccessorMethods(result, instanceVariable, priority);
        }
    }


    private static void addInstanceVariableAccessorMethods(@NotNull CompletionResultSet result, @NotNull ObjJInstanceVariableDeclaration instanceVariable, double priority) {
        String className = instanceVariable.getContainingClassName();
        ObjJMethodHeaderStub getter = instanceVariable.getGetter();
        ObjJMethodHeaderStub setter = instanceVariable.getSetter();
        if (getter != null) {
            addSelectorLookupElement(result, getter.getSelectorString(), className, "<" + instanceVariable.getFormalVariableType().getText() + ">", priority, false, ObjJIcons.ACCESSOR_ICON);
        }
        if (setter != null) {
            addSelectorLookupElement(result, setter.getSelectorString(), className, "<" + instanceVariable.getFormalVariableType().getText() + ">", priority, false, ObjJIcons.ACCESSOR_ICON);
        }
    }

    /*
    /**
     * adds instance variables as lookup elements to result set
     * @param result completion result set
     * @param project containing project
     * @param possibleContainingClassNames possible class names to determine completion priority
     * @param selectorString selector string to match
     * /
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
    }*/

}
