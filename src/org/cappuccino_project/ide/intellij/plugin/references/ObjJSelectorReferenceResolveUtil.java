package org.cappuccino_project.ide.intellij.plugin.references;

import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.*;
import org.cappuccino_project.ide.intellij.plugin.settings.ObjJPluginSettings;
import org.cappuccino_project.ide.intellij.plugin.contributor.ObjJMethodCallCompletionContributorUtil;
import org.cappuccino_project.ide.intellij.plugin.indices.*;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjJSelectorReferenceResolveUtil {

    private static final Logger LOGGER = Logger.getLogger(ObjJSelectorReferenceResolveUtil.class.getName());
    private static final SelectorResolveResult<PsiElement> EMPTY_RESULT = new SelectorResolveResult<PsiElement>(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    private static final SelectorResolveResult<ObjJSelector> EMPTY_SELECTORS_RESULT = new SelectorResolveResult<>(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    private static final List<String> EMPTY_CLASS_CONSTRAINT_LIST = Collections.emptyList();
    private static final List<String> RETURN_SELF_SELECTORS = Arrays.asList(ObjJMethodPsiUtils.getSelectorString("class"));

    @NotNull
    public static SelectorResolveResult<ObjJSelector> getMethodCallReferences(@NotNull ObjJSelector element) {
        List<String> classConstraints = getClassConstraints(element);
        String selector = element.getSelectorString(false);
        ObjJHasMethodSelector parent = ObjJTreeUtil.getParentOfType(element, ObjJHasMethodSelector.class);
        int selectorIndex = parent != null ? parent.getSelectorStrings().indexOf(selector) : -1;
        String fullSelector = parent != null ? parent.getSelectorString() : null;
        if (fullSelector == null) {
            return EMPTY_SELECTORS_RESULT;
        }
        if (parent instanceof ObjJMethodHeader || parent instanceof ObjJAccessorProperty) {
            return new SelectorResolveResult<>(Collections.singletonList(element), Collections.emptyList(), classConstraints);
        }

        if (DumbService.isDumb(element.getProject())) {
            throw new IndexNotReadyRuntimeException();
        }
        //LOGGER.log(Level.INFO, "Searching for methods matching selector: <"+fullSelector+">, in file: "+element.getContainingFile().getVirtualFile().getName());
        List<ObjJMethodHeaderDeclaration> methodHeaders = ObjJUnifiedMethodIndex.getInstance().get(fullSelector, element.getProject());
        return prune(classConstraints, methodHeaders, element.getText(), selectorIndex);
    }

    @NotNull
    public static SelectorResolveResult<ObjJSelector> getMethodCallPartialReferences(@Nullable ObjJSelector element, boolean includeSelf) {
        if (element == null) {
            return EMPTY_SELECTORS_RESULT;
        }
        List<String> classConstraints = getClassConstraints(element);
        ObjJHasMethodSelector parent = ObjJTreeUtil.getParentOfType(element, ObjJHasMethodSelector.class);
        int selectorIndex = parent != null ? parent.getSelectorList().indexOf(element) : -1;
        String selectorFragment = ObjJPsiImplUtil.getSelectorUntil(element, includeSelf);
        if (selectorFragment == null) {
            return EMPTY_SELECTORS_RESULT;
        }
        if (parent instanceof ObjJMethodHeader || parent instanceof ObjJAccessorProperty) {
            return new SelectorResolveResult<>(Collections.singletonList(element), Collections.emptyList(), classConstraints);
        }

        if (DumbService.isDumb(element.getProject())) {
            throw new IndexNotReadyRuntimeException();
        }
        List<ObjJMethodHeaderDeclaration> methodHeaders = ObjJUnifiedMethodIndex.getInstance().getByPatternFlat(selectorFragment+"(.*)", element.getProject());
        if (!methodHeaders.isEmpty()) {
            return prune(classConstraints, methodHeaders, element.getText(), selectorIndex);
        } else {
            return  getSelectorLiteralReferences(element);
        }
    }

    @Nullable
    public static SelectorResolveResult<ObjJSelector> resolveSelectorReferenceAsPsiElement(@NotNull List<ObjJSelector> selectors, int selectorIndex) {
        RawResult result = resolveSelectorReferenceRaw(selectors, selectorIndex);
        if (result== null) {
            return null;
        }
        if (!result.methodHeaders.isEmpty()) {
            return prune(result.classConstraints, result.methodHeaders, result.baseSelector.getText(), result.index);
        } else {
            return null;
        }
    }

    @Nullable
    private static RawResult resolveSelectorReferenceRaw(@NotNull List<ObjJSelector> selectors, int selectorIndex) {
        //Have to loop over elements as some selectors in list may have been virtually created
        ObjJMethodCall parent = null;
        if (selectors.size() < 1) {
            return null;
        }
        if (selectorIndex < 0 || selectorIndex >= selectors.size()) {
            selectorIndex = selectors.size() - 1;
        }
        ObjJSelector baseSelector = selectors.get(selectorIndex);
        Project project = baseSelector != null ? baseSelector.getProject() : null;
        for (ObjJSelector selector : selectors) {
            if (parent == null) {
                parent = ObjJTreeUtil.getParentOfType(selector, ObjJMethodCall.class);
            }
            if (parent != null && parent.getContainingClassName().equals(ObjJElementFactory.PLACEHOLDER_CLASS_NAME)) {
                parent = null;
            }
        }
        for (int i=selectors.size()-1;i>0;--i) {
            ObjJSelector tempSelector = selectors.get(i);
            if (project == null) {
                project = parent != null ? parent.getProject() : tempSelector != null ? tempSelector.getProject() : null;
            }
            if (baseSelector == null || tempSelector.getText().contains(ObjJMethodCallCompletionContributorUtil.CARET_INDICATOR)) {
                baseSelector = tempSelector;
                selectorIndex = i;
                break;
            }
        }
        if (project == null) {
            LOGGER.log(Level.INFO, "Parent and base selector are null");
            return null;
        }

        String selector = ObjJMethodPsiUtils.getSelectorStringFromSelectorList(selectors).replaceAll("\\s+", "");
        //LOGGER.log(Level.INFO, "Selector for method call is: <"+ObjJMethodPsiUtils.getSelectorStringFromSelectorList(selectors)+">");
        if (selector == null || selector.isEmpty()) {
            //LOGGER.log(Level.INFO, "Selector search failed with empty selector.");
            return null;
        }
        List<String> classConstraints = parent != null ? getClassConstraints(parent) : Collections.emptyList();
        if (parent == null) {
            //LOGGER.log(Level.INFO, "Selector parent for selector <"+selector+"> is null");
            //return EMPTY_RESULT;
        }
        Map<String, List<ObjJMethodHeaderDeclaration>> methodHeaders;
        if (selector.contains(ObjJMethodCallCompletionContributorUtil.CARET_INDICATOR)) {
            String pattern = selector.replace(ObjJMethodCallCompletionContributorUtil.CARET_INDICATOR, "(.+)")+"(.*)";
            methodHeaders = ObjJUnifiedMethodIndex.getInstance().getByPattern(pattern, project);
            //LOGGER.log(Level.INFO, "Getting selectors for selector pattern: <"+pattern+">. Found <"+methodHeaders.size()+"> methods");
        } else {
            methodHeaders = ObjJUnifiedMethodIndex.getInstance().getByPattern(selector, null, project);
            //LOGGER.log(Level.INFO, "Getting selectors with selector beginning: <"+selector+">. Found <"+methodHeaders.size()+"> methods");
        }
        //List<ObjJMethodHeaderDeclaration> methodHeaders = ObjJUnifiedMethodFragmentIndex.getInstance().get(selectorFragment, element.getProject());
        if (!methodHeaders.isEmpty()) {
            return new RawResult(methodHeaders, classConstraints, baseSelector, selectorIndex);
        } else {
            return null;
        }
    }

    @NotNull
    private static SelectorResolveResult<ObjJSelector> prune(List<String> classConstraints, Map<String, List<ObjJMethodHeaderDeclaration>> methodHeaders, String subSelector, int selectorIndex) {
        final Map<String, List<ObjJSelector>> result = new HashMap<>();
        final Map<String, List<ObjJSelector>> others = new HashMap<>();
        ObjJSelector selectorElement;
        for (String key : methodHeaders.keySet()) {
            for (ObjJMethodHeaderDeclaration methodHeader : methodHeaders.get(key)) {
                ProgressIndicatorProvider.checkCanceled();
                selectorElement = ObjJPsiImplUtil.getThisOrPreviousNonNullSelector(methodHeader, subSelector, selectorIndex);
                if (selectorElement == null) {
                    //LOGGER.log(Level.SEVERE, "Method header returned an empty selector in matched header");
                    continue;
                }
                if (sharesContainingClass(classConstraints, methodHeader)) {
                    put(result, key, selectorElement);
                } else {
                    put(others, key, selectorElement);
                }
            }
        }
        List<ObjJSelector> resultOut = getFirstElements(result);
        List<ObjJSelector> othersOut = getFirstElements(others);
        return new SelectorResolveResult<>(resultOut, othersOut, classConstraints);
    }

    private static List<ObjJSelector> getFirstElements(Map<String, List<ObjJSelector>> resultSet) {
        List<ObjJSelector> out = new ArrayList<>();
        for (String key : resultSet.keySet()) {
            List<ObjJSelector> elements = resultSet.get(key);
            if (!elements.isEmpty()) {
                out.add(elements.get(0));
            }
        }
        return out;
    }

    private static void put(Map<String, List<ObjJSelector>> map, String key, ObjJSelector declaration) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        }
        map.get(key).add(declaration);
    }

    @NotNull
    private static SelectorResolveResult<ObjJSelector> prune(List<String> classConstraints, List<ObjJMethodHeaderDeclaration> methodHeaders, String subSelector, int selectorIndex) {
        final List<ObjJSelector> result = new ArrayList<>();
        final List<ObjJSelector> others = new ArrayList<>();
        ObjJSelector selectorElement;
        for (ObjJMethodHeaderDeclaration methodHeader : methodHeaders) {
            ProgressIndicatorProvider.checkCanceled();
            selectorElement = ObjJPsiImplUtil.getThisOrPreviousNonNullSelector(methodHeader, subSelector, selectorIndex);
            if (selectorElement == null) {
                LOGGER.log(Level.SEVERE, "Method header returned an empty selector in matched header");
                continue;
            }
            if (sharesContainingClass(classConstraints, methodHeader)) {
                //LOGGER.log(Level.INFO, "Method selector shares containing class in list <"+ArrayUtils.join(classConstraints)+">");
                result.add(selectorElement);
            } else {
                //LOGGER.log(Level.INFO, "Method does not share containing class");
                others.add(selectorElement);
            }
        }
        //LOGGER.log(Level.INFO, "Finished pruning method headers.");
        return packageResolveResult(result, others, classConstraints);
    }

    private static boolean sharesContainingClass(List<String> classConstraints, ObjJHasContainingClass hasContainingClass) {
        return classConstraints.isEmpty() || classConstraints.contains(ObjJClassType.UNDETERMINED) || classConstraints.contains(hasContainingClass.getContainingClassName());
    }

    public static SelectorResolveResult<ObjJSelector> getSelectorLiteralReferences(@Nullable ObjJSelector selectorElement) {
        if (selectorElement == null) {
            return EMPTY_SELECTORS_RESULT;
        }
        ObjJHasMethodSelector parent = ObjJTreeUtil.getParentOfType(selectorElement, ObjJHasMethodSelector.class);
        int selectorIndex = parent != null ? parent.getSelectorStrings().indexOf(selectorElement.getSelectorString(false)) : -1;
        String fullSelector = parent != null ? parent.getSelectorString() : null;
        if (fullSelector == null) {
            return EMPTY_SELECTORS_RESULT;
        }
        String containingClass = selectorElement.getContainingClassName();
        List<String> containingClasses = !ObjJMethodCallPsiUtil.isUniversalMethodCaller(containingClass) ? ObjJInheritanceUtil.getAllInheritedClasses(containingClass, selectorElement.getProject()) : null;
        List<ObjJSelector> result = new ArrayList<>();
        List<ObjJSelector> otherResults = new ArrayList<>();

        if (DumbService.isDumb(selectorElement.getProject())) {
            throw new IndexNotReadyRuntimeException();
        }
        List<ObjJSelectorLiteral> selectorLiterals = ObjJSelectorInferredMethodIndex.getInstance().get(fullSelector, selectorElement.getProject());
        final String subSelector = selectorElement.getSelectorString(false);
        for (ObjJSelectorLiteral selectorLiteral : selectorLiterals) {
            ProgressIndicatorProvider.checkCanceled();
            ObjJSelector selector = selectorIndex >= 0 ? selectorLiteral.getSelectorList().get(selectorIndex) : ObjJPsiImplUtil.findSelectorMatching(selectorLiteral, subSelector);
            if (selector != null) {
                if (containingClasses == null || containingClasses.contains(selector.getContainingClassName())) {
                    result.add(selector);
                } else {
                    otherResults.add(selector);
                }
            }
        }
        return packageResolveResult(result, otherResults, containingClasses);
    }


    @NotNull
    public static SelectorResolveResult<PsiElement> getInstanceVariableSimpleAccessorMethods(ObjJSelector selectorElement, @NotNull List<String> classConstraints) {
        if (classConstraints.isEmpty()) {
            classConstraints = getClassConstraints(selectorElement);
        }
        if (DumbService.isDumb(selectorElement.getProject())) {
            throw new IndexNotReadyRuntimeException();
        }
        ObjJHasMethodSelector parent = ObjJTreeUtil.getParentOfType(selectorElement, ObjJHasMethodSelector.class);
        String fullSelector = parent != null ? parent.getSelectorString() : null;
        List<PsiElement> result = new ArrayList<>();
        List<PsiElement> otherResult = new ArrayList<>();
        for (ObjJInstanceVariableDeclaration variableDeclaration : ObjJClassInstanceVariableAccessorMethodIndex.getInstance().get(fullSelector, selectorElement.getProject())) {
            ProgressIndicatorProvider.checkCanceled();
            if (sharesContainingClass(classConstraints, variableDeclaration)) {
                result.add(variableDeclaration.getAtAccessors() != null ? variableDeclaration.getAtAccessors() : variableDeclaration);
            } else {
                otherResult.add(variableDeclaration.getAtAccessors());
            }
        }
        return packageResolveResult(result, otherResult, classConstraints);
    }

    @NotNull
    public static SelectorResolveResult<PsiElement> getVariableReferences(@NotNull ObjJSelector selectorElement, @NotNull List<String> classConstraints) {
        String variableName = selectorElement.getSelectorString(false);
        if (classConstraints.isEmpty()) {
            classConstraints = getClassConstraints(selectorElement);
        }
        final List<PsiElement> result = new ArrayList<>();
        final List<PsiElement> otherResult = new ArrayList<>();
        //ProgressIndicatorProvider.checkCanceled();
        if (DumbService.isDumb(selectorElement.getProject())) {
            //throw new IndexNotReadyRuntimeException();
            return EMPTY_RESULT;
        }
        for (ObjJInstanceVariableDeclaration declaration : ObjJInstanceVariablesByNameIndex.getInstance().get(variableName, selectorElement.getProject())) {
            ProgressIndicatorProvider.checkCanceled();
            if (classConstraints.contains(declaration.getContainingClassName())) {
                result.add(declaration.getVariableName());
            } else {
                otherResult.add(declaration.getVariableName());
            }
        }
        return packageResolveResult(result, otherResult, classConstraints);
    }

    private static <T> SelectorResolveResult<T> packageResolveResult(@NotNull List<T> result, @NotNull List<T> otherResult, @Nullable List<String> classConstraints) {
        return new SelectorResolveResult<T>(result, otherResult, classConstraints != null ? classConstraints : Collections.emptyList());
    }

    @NotNull
    public static List<String> getClassConstraints(ObjJSelector element) {
        return getClassConstraints(ObjJTreeUtil.getParentOfType(element, ObjJHasMethodSelector.class));
    }


    @NotNull
    private static List<String> getClassConstraints(@Nullable ObjJHasMethodSelector element) {
        if (element == null || !(element instanceof ObjJMethodCall)) {
            //   LOGGER.log(Level.INFO, "Selector is not in method call.");
            return Collections.emptyList();
        }
        List<String> classConstraints = null;
        ObjJMethodCall methodCall = (ObjJMethodCall) element;
        ObjJCallTarget callTarget = methodCall.getCallTarget();
        String callTargetText = ObjJCallTargetUtil.getCallTargetTypeIfAllocStatement(callTarget);
        //LOGGER.log(Level.INFO, "Getting Call Target Class Constraints for target text: <"+callTargetText+">");
        classConstraints = ObjJCallTargetUtil.getPossibleCallTargetTypesFromMethodCall(methodCall);
        if (!ObjJPluginSettings.validateCallTarget() || !classConstraints.isEmpty()) {
            return classConstraints;
        }
        classConstraints = ObjJCallTargetUtil.getPossibleCallTargetTypes(methodCall.getCallTarget());
        if (!classConstraints.isEmpty()) {
            LOGGER.log(Level.INFO, "Call target: <"+methodCall.getCallTarget().getText()+"> is possibly of type: ["+ArrayUtils.join(classConstraints)+"]");
        } else {
            //   LOGGER.log(Level.INFO, "Failed to infer call target type for target named <"+methodCall.getCallTarget().getText()+">.");
        }
        return classConstraints;
    }


    public static class SelectorResolveMapResult<T> {
        private final boolean natural;
        private final Map<String, T> naturalResult;
        private final Map<String, T> otherResult;
        private final List<String> possibleContainingClassNames;
        private SelectorResolveMapResult(@NotNull final Map<String, T> naturalResult, @NotNull final Map<String, T> otherResult, @NotNull final List<String> possibleContainingClassNames) {
            this.naturalResult = naturalResult;
            this.otherResult = otherResult;
            this.natural = !naturalResult.isEmpty();
            this.possibleContainingClassNames = possibleContainingClassNames;
            //LOGGER.log(Level.INFO, "Selector resolve result has <"+naturalResult.size()+"> natural results, and <"+otherResult.size()+"> other results");
        }

        public boolean isNatural() {
            return natural;
        }

        @NotNull
        public Map<String, T> getResult() {
            return natural ? naturalResult : otherResult;
        }

        @NotNull
        public Map<String, T> getNaturalResult() {
            return naturalResult;
        }

        @NotNull
        public Map<String, T> getOtherResult() {
            return otherResult;
        }

        @NotNull
        public List<String> getPossibleContainingClassNames() {
            return possibleContainingClassNames;
        }

        public boolean isEmpty() {
            return getResult().isEmpty();
        }
    }

    public static class SelectorResolveResult<T> {
        private final boolean natural;
        private final List<T> naturalResult;
        private final List<T> otherResult;
        private final List<String> possibleContainingClassNames;
        private SelectorResolveResult(@NotNull final List<T> naturalResult, @NotNull final List<T> otherResult, @NotNull final List<String> possibleContainingClassNames) {
            this.naturalResult = naturalResult;
            this.otherResult = otherResult;
            this.natural = !naturalResult.isEmpty();
            this.possibleContainingClassNames = possibleContainingClassNames;
            //LOGGER.log(Level.INFO, "Selector resolve result has <"+naturalResult.size()+"> natural results, and <"+otherResult.size()+"> other results");
        }

        public boolean isNatural() {
            return natural;
        }

        @NotNull
        public List<T> getResult() {
            return natural ? naturalResult : otherResult;
        }

        @NotNull
        public List<T> getNaturalResult() {
            return naturalResult;
        }

        @NotNull
        public List<T> getOtherResult() {
            return otherResult;
        }

        @NotNull
        public List<String> getPossibleContainingClassNames() {
            return possibleContainingClassNames;
        }

        public boolean isEmpty() {
            return getResult().isEmpty();
        }
    }

    public static class SelectorPsiElementResolveResult extends SelectorResolveResult<PsiElement> {
        private SelectorPsiElementResolveResult(
                @NotNull
                        List<PsiElement> naturalResult,
                @NotNull
                        List<PsiElement> otherResult,
                @NotNull
                        List<String> possibleContainingClassNames) {
            super(naturalResult, otherResult, possibleContainingClassNames);
        }
    }

    public interface ResultFormatter<T> {
        T format(int selectorIndex, ObjJMethodHeaderDeclaration methodHeaderDeclaration);
    }

    public static class RawResult {
        private final Map<String, List<ObjJMethodHeaderDeclaration>> methodHeaders;
        private final List<String> classConstraints;
        private final int index;
        private final ObjJSelector baseSelector;

        RawResult(Map<String, List<ObjJMethodHeaderDeclaration>> methodHeaders, List<String> classConstraints, ObjJSelector baseSelector, int index) {
            this.methodHeaders = methodHeaders;
            this.classConstraints = classConstraints;
            this.baseSelector = baseSelector;
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public Map<String, List<ObjJMethodHeaderDeclaration>>  getMethodHeaders() {
            return methodHeaders;
        }

        public List<String> getClassConstraints() {
            return classConstraints;
        }

        public ObjJSelector getBaseSelector() {
            return baseSelector;
        }
    }

}
