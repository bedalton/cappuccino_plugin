package org.cappuccino_project.ide.intellij.plugin.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.cappuccino_project.ide.intellij.plugin.annotator.IgnoreUtil.ElementType;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.*;
import org.cappuccino_project.ide.intellij.plugin.settings.ObjJPluginSettings;
import org.cappuccino_project.ide.intellij.plugin.indices.*;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil;
import org.cappuccino_project.ide.intellij.plugin.references.ObjJSelectorReferenceResolveUtil.SelectorResolveResult;
import org.cappuccino_project.ide.intellij.plugin.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Annotator for method calls
 */
public class ObjJMethodCallAnnotatorUtil {
    private static final String CPSTRING_INIT_WITH_FORMAT = "initWithFormat";
    private static final String CPSTRING_STRING_WITH_FORMAT = "stringWithFormat";

    private static final Logger LOGGER = Logger.getLogger(ObjJMethodCallAnnotatorUtil.class.getName());

    /**
     * Responsible for annotating method calls
     * @param methodCall method call to annotate
     * @param holder annotation holder used for markup
     */
    public static void annotateMethodCall(@NotNull final ObjJMethodCall methodCall,  @NotNull final AnnotationHolder holder) {
        //First validate that all selector sub elements are present
        validateMissingSelectorElements(methodCall, holder);
        //Validate the method for selector exists. if not, stop annotation
        if (!validMethodSelector(methodCall, holder))  {
            //Method call is invalid, stop annotations
            return;
        }
        //Check that call target for method call is valid
        //Only used is setting for validate call target is set.
        if (ObjJPluginSettings.validateCallTarget()) {// && !IgnoreUtil.shouldIgnore(methodCall, ElementType.CALL_TARGET)) {
            validateCallTarget(methodCall, holder);
        }

        //Annotate static vs instance method calls.
        annotateStaticMethodCall(methodCall, holder);
        if (isCPStringWithFormat(methodCall)) {
            annotateStringWithFormat(methodCall, holder);
        }

    }

    /**
     * Validates and annotates missing children of selector elements
     * somewhat of a hack for the way selector elements are handled in the psi tree
     * @param methodCall method call to evaluate
     * @param holder annotation holder
     */
    private static void validateMissingSelectorElements(@NotNull ObjJMethodCall methodCall, @NotNull AnnotationHolder holder) {
        if (methodCall.getSelectorList().size() > 1) {
            for (ObjJQualifiedMethodCallSelector selector : methodCall.getQualifiedMethodCallSelectorList()) {
                if (selector.getExprList().isEmpty() && selector.getSelector() != null) {
                    holder.createErrorAnnotation(selector.getSelector(), "Missing expression");
                    return;
                }
            }
        }
    }

    /**
     * Validates and annotates static vs instance method calls.
     * @param methodCall method call to annotate
     * @param holder annotation holder
     */
    private static void annotateStaticMethodCall(@NotNull final ObjJMethodCall methodCall, @NotNull final AnnotationHolder holder) {
        if(false && IgnoreUtil.shouldIgnore(methodCall, ElementType.METHOD_SCOPE)) {
            return;
        }
        String callTarget = methodCall.getCallTarget().getText();
        final List<String> possibleCallTargetClassTypes = ObjJPluginSettings.validateCallTarget() ? ObjJCallTargetUtil.getPossibleCallTargetTypes(methodCall.getCallTarget()) : null;
        if (callTarget.equals("self") || callTarget.equals("super") || (possibleCallTargetClassTypes != null && possibleCallTargetClassTypes.contains(ObjJClassType.UNDETERMINED))) {
            return;
        }
        boolean isStaticReference = possibleCallTargetClassTypes != null ? possibleCallTargetClassTypes.contains(callTarget) : !ObjJClassDeclarationsIndex.getInstance().get(callTarget, methodCall.getProject()).isEmpty();
        if (DumbService.isDumb(methodCall.getProject())) {
            return;
        }
        final List<ObjJMethodHeaderDeclaration> methodHeaderDeclarations = ObjJUnifiedMethodIndex.getInstance().get(methodCall.getSelectorString(), methodCall.getProject());
        boolean annotate = false;
        for (ObjJMethodHeaderDeclaration declaration : methodHeaderDeclarations) {
            ProgressIndicatorProvider.checkCanceled();
            if (possibleCallTargetClassTypes != null && !possibleCallTargetClassTypes.contains(declaration.getContainingClassName())) {
                continue;
            }
            if (declaration.isStatic() == isStaticReference || (declaration.isStatic() && (possibleCallTargetClassTypes == null || possibleCallTargetClassTypes.contains(ObjJClassType.CLASS)))) {
                return;
            } else {
                annotate = true;
            }
        }
        if (!annotate) {
            return;
        }
        for (ObjJSelector selector : methodCall.getSelectorList()) {
            String warning = !isStaticReference && possibleCallTargetClassTypes != null ? "Static method called from class instance with inferred type of ["+ArrayUtils.join(possibleCallTargetClassTypes, ", ", false)+"]": "Instance method called from static reference";
            holder.createWeakWarningAnnotation(selector, warning);
        }
    }

    /**
     * Validates and annotates a selector literal
     * @param selectorLiteral selector literal
     * @param holder annotation holder
     */
    @SuppressWarnings("unused")
    public static void annotateSelectorLiteral (@NotNull final ObjJSelectorLiteral selectorLiteral, @NotNull final AnnotationHolder holder) {
        //TODO annotations for selector literals are in some cases selector contracts or declarations
        /*
        final Project project = selectorLiteral.getProject();
        final List<ObjJSelector> selectors = selectorLiteral.getSelectorList();
        annotateSelectorReference(project, selectors, holder);
        */
    }

    /**
     * Validates and annotates method selector signature
     * @param methodCall method call
     * @param holder annotation holder
     * @return <b>true</b> if valid, <b>false</b> otherwise
     */
    private static boolean validMethodSelector(@NotNull ObjJMethodCall methodCall, @NotNull final AnnotationHolder holder) {
        //noinspection PointlessBooleanExpression
        if (false && IgnoreUtil.shouldIgnore(methodCall, ElementType.METHOD)) {
            return true;
        }
        //Checks that there are selectors
        final List<ObjJSelector> selectors = methodCall.getSelectorList();
        if (selectors.isEmpty()) {
            return false;
        }
        final Project project = methodCall.getProject();
        //Get full selector signature
        final String fullSelector = ObjJMethodPsiUtils.getSelectorStringFromSelectorList(selectors);
        //Check that method selector signature is valid, and return if it is
        if (isValidMethodCall(fullSelector, project)) {
            return true;
        } else {
            //LOGGER.log(Level.INFO, "Failed to find indexed selector matching : <" + fullSelector + ">;");
        }
        if (selectors.size() == 1) {
            ObjJSelector selector = selectors.get(0);
            holder.createErrorAnnotation(selector, "Failed to find selector matching <" + selector.getSelectorString(true) + ">");
            return false;
        }
        //Selector is invalid, so find first non-matching selector
        int failIndex = getSelectorFailedIndex(methodCall.getSelectorStrings(), project);

        //If fail index is less than one, mark all selectors and return;
        if (failIndex < 0) {
            //LOGGER.log(Level.INFO, "Selector fail index returned a negative index.");
            holder.createErrorAnnotation(methodCall, "Failed to find selector matching <" + fullSelector + ">");
            return false;
        }

        StringBuilder selectorToFailPoint = new StringBuilder(ObjJMethodPsiUtils.getSelectorStringFromSelectorList(selectors.subList(0, failIndex)));
        List<ObjJQualifiedMethodCallSelector> methodCallSelectors = methodCall.getQualifiedMethodCallSelectorList();
        //assert methodCallSelectors.size() == methodCall.getSelectorStrings().size() : "Method call is returning difference lengthed selector lists. Call: <"+methodCall.getText()+">. Qualified: <"+methodCallSelectors.size()+">; Strings: <"+methodCall.getSelectorStrings().size()+">";
        int numSelectors = methodCallSelectors.size();
        ObjJQualifiedMethodCallSelector selector;
        PsiElement failPoint;
        //Markup invalid
        for (int i = failIndex; i < numSelectors; i++) {
            selector = methodCallSelectors.get(i);
            failPoint = selector.getSelector() != null && !selector.getSelector().getText().isEmpty() ? selector.getSelector() : selector.getColon();
            selectorToFailPoint.append(ObjJMethodPsiUtils.getSelectorString(selectors.get(i), true));
            holder.createErrorAnnotation(failPoint, "Failed to find selector matching <" + selectorToFailPoint.toString() + ">");
        }

        return false;
    }

    /**
     * Brute force method to check if method call is valid
     * @param fullSelector full selector for method call
     * @param project project
     * @return true if method selector is valid in any place, false otherwise
     */
    private static boolean isValidMethodCall(@NotNull String fullSelector, @NotNull Project project) {
        return  !ObjJUnifiedMethodIndex.getInstance().get(fullSelector, project).isEmpty() ||
                !ObjJSelectorInferredMethodIndex.getInstance().get(fullSelector, project).isEmpty() ||
                !ObjJInstanceVariablesByNameIndex.getInstance().get(fullSelector.substring(0, fullSelector.length()-1), project).isEmpty() ||
                !ObjJClassInstanceVariableAccessorMethodIndex.getInstance().get(fullSelector, project).isEmpty() ||
                !ObjJClassInstanceVariableAccessorMethodIndex.getInstance().get(fullSelector, project).isEmpty();
    }

    /**
     * Gets selector index where selector stops being valid.
     * @param selectors selector list
     * @param project project
     * @return index of first invalid selector
     */
    private static int getSelectorFailedIndex(@NotNull List<String> selectors, @NotNull Project project) {
        if (selectors.size() < 2 || DumbService.isDumb(project)) {
            return 0;
        }
        StringBuilder builder = new StringBuilder();
        String selector;
        for (int i=0;i<selectors.size();i++) {
            selector = selectors.get(i);
            builder.append(selector).append(ObjJMethodPsiUtils.SELECTOR_SYMBOL);
            selector = builder.toString();
            if (
                    !ObjJUnifiedMethodIndex.getInstance().getStartingWith(selector, project).isEmpty() ||
                    !ObjJSelectorInferredMethodIndex.getInstance().getStartingWith(selector, project).isEmpty())
            {
                continue;
            }
            //LOGGER.log(Level.INFO, "Selector match failed at index: <"+i+"> in with selector: <"+builder.toString()+">");
            return i;
        }
        return 0;
    }

    /**
     * Validates and annotates the call target of a method call to ensure method call is possible.
     * @param methodCall method call to check
     * @param holder annotation holder
     */
    private static void validateCallTarget(@NotNull ObjJMethodCall methodCall, @NotNull final AnnotationHolder holder) {
        if (methodCall.getSelectorList().isEmpty()) {
            return;
        }
        List<ObjJSelector> selectorList = methodCall.getSelectorList();
        int lastIndex = selectorList.size() -1;
        ObjJSelector lastSelector = selectorList.get(lastIndex);
        while (lastSelector == null && lastIndex > 0) {
            lastSelector = selectorList.get(--lastIndex);
        }
        if (lastSelector == null) {
            return;
        }
        SelectorResolveResult<ObjJSelector> selectorResult = ObjJSelectorReferenceResolveUtil.getMethodCallReferences(lastSelector);
        String weakWarning = null;
        List<String> childClasses = new ArrayList<>();
        List<String> expectedContainingClasses;
        if (!selectorResult.isEmpty()) {
            if (selectorResult.isNatural()) {
                return;
            }
            expectedContainingClasses = selectorResult.getPossibleContainingClassNames();
            for (String containingClass : expectedContainingClasses) {
                ProgressIndicatorProvider.checkCanceled();
                childClasses.addAll(ObjJClassInheritanceIndex.getInstance().getChildClassesAsStrings(containingClass, methodCall.getProject()));
            }
            List<String> actualContainingClasses = ObjJHasContainingClassPsiUtil.getContainingClassNamesFromSelector(selectorResult.getResult());
            List<String> childContainingClasses = new ArrayList<>();
            for (String className : actualContainingClasses) {
                if (childClasses.contains(className) && !childContainingClasses.contains(className)) {
                    childContainingClasses.add(className);
                }
            }
            if (!childContainingClasses.isEmpty()) {
                weakWarning = "Method found in possible child classes ["+ArrayUtils.join(childContainingClasses)+ "] of ["+ArrayUtils.join(expectedContainingClasses)+ "]";
            } else {
                weakWarning = "Method found in classes [" + ArrayUtils.join(actualContainingClasses) + "], not in inferred classes [" + ArrayUtils.join(expectedContainingClasses) + "]";
            }
        }
        selectorResult = ObjJSelectorReferenceResolveUtil.getSelectorLiteralReferences(lastSelector);
        if (!selectorResult.isEmpty()) {
            if (selectorResult.isNatural()) {
                return;
            } else if (weakWarning == null) {
                weakWarning = "Method seems to reference a selector literal, but not in enclosing class";
            }
        }
        if (selectorList.size() == 1) {
            SelectorResolveResult<PsiElement> psiElementResult = ObjJSelectorReferenceResolveUtil.getInstanceVariableSimpleAccessorMethods(selectorList.get(0), selectorResult.getPossibleContainingClassNames());
            if (!psiElementResult.isEmpty()) {
                if (psiElementResult.isNatural()) {
                    return;
                } else if (weakWarning == null) {
                    List<String> actualContainingClasses = ObjJHasContainingClassPsiUtil.getContainingClassNames(psiElementResult.getResult());
                    weakWarning = "Selector seems to reference an accessor method in classes [" + ArrayUtils.join(actualContainingClasses) + "], not in the inferred classes [" + ArrayUtils.join(psiElementResult.getPossibleContainingClassNames()) + "]";
                }
            } else {
                psiElementResult = ObjJSelectorReferenceResolveUtil.getVariableReferences(selectorList.get(0), psiElementResult.getPossibleContainingClassNames());
                if (!psiElementResult.isEmpty()) {
                    if (psiElementResult.isNatural()) {
                        return;
                    } else if (weakWarning == null) {
                        List<String> actualContainingClasses = ObjJHasContainingClassPsiUtil.getContainingClassNames(psiElementResult.getResult());
                        weakWarning = "Selector seems to reference an instance variable in [" + ArrayUtils.join(actualContainingClasses) + "], not in the inferred classes [" + ArrayUtils.join(psiElementResult.getPossibleContainingClassNames()) + "]";
                    }
                }
            }
        }
        if (weakWarning != null) {
            for (ObjJSelector selector : methodCall.getSelectorList()) {
                holder.createWeakWarningAnnotation(selector,weakWarning);
            }
        }
    }

    /**
     * Validates and annotates CPString formatter
     * Simply checks that there are enough arguments in the list for all wildcards in string format
     * @param methodCall method call
     * @param annotationHolder annotation holder
     */
    private static void annotateStringWithFormat(ObjJMethodCall methodCall, AnnotationHolder annotationHolder) {
        if (!isCPStringWithFormat(methodCall)) {
            return;
        }
        if (methodCall.getQualifiedMethodCallSelectorList().isEmpty()) {
            return;
        }
        List<ObjJExpr> expressions = methodCall.getQualifiedMethodCallSelectorList().get(0).getExprList();
        if (expressions.size() < 1) {
            annotationHolder.createWarningAnnotation(methodCall, "String with format requires first parameter to be a non-nil string");
        }
        ObjJExpr format = expressions.remove(0);
        String formatVarType;
        try {
            formatVarType = ObjJExpressionReturnTypeUtil.getReturnType(format, true);
        } catch (ObjJExpressionReturnTypeUtil.MixedReturnTypeException e) {
            formatVarType = e.getReturnTypesList().get(0);
        }
        if (formatVarType == null) {
            return;
        }
        if (!ObjJMethodCallPsiUtil.isUniversalMethodCaller(formatVarType) && !formatVarType.equals(ObjJClassType.STRING)) {
            annotationHolder.createWarningAnnotation(format, "First parameter should be of type CPString");
            return;
        }
        if (format.getLeftExpr() == null || format.getLeftExpr().getPrimary() == null || format.getLeftExpr().getPrimary().getStringLiteral() == null) {
         //   LOGGER.log(Level.INFO, "[CPString initWithFormat] should have string expression first, but does not. Actual text: <"+format.getText()+">");
            return;
        }

        String formatString = format.getLeftExpr().getPrimary().getStringLiteral().getText();
        Pattern pattern = Pattern.compile("%([^%])*");
        Matcher matchResult = pattern.matcher(formatString);
        List<String> matches = new ArrayList<>();
        while (matchResult.find()) {
            matches.add(matchResult.group());
        }
        int numMatches = matches.size();
        int numExpressions = expressions.size();
        if (numMatches > numExpressions) {
            int elementOffset = format.getLeftExpr().getPrimary().getStringLiteral().getTextRange().getStartOffset();
            String[] parts = formatString.split("%([^%])");
            String part;
            StringBuilder builder = new StringBuilder();
            int offset;
            int lastIndex = parts.length - (formatString.lastIndexOf("%") == formatString.length() - 2 && formatString.lastIndexOf("%") != formatString.length() - 1 ? 0 : 1);
            for (int i=0;i<lastIndex;i++) {
                part = parts[i];
                builder.append(part);
                offset = elementOffset + builder.length();
                builder.append("%?");
                if (i<numExpressions) {
                    continue;
                }
                //LOGGER.log(Level.INFO, "Current substring = <"+builder.toString()+">");
                annotationHolder.createWarningAnnotation(TextRange.create(offset, offset+2), String.format("Not enough values for format. Expected <%d>, found <%d>", numMatches, numExpressions));
            }
        } else if (numMatches < numExpressions) {
            for (int i=numMatches;i<numExpressions;i++) {
                annotationHolder.createWarningAnnotation(expressions.get(i), String.format("Too many arguments found for string format. Expected <%d>, found <%d>", numMatches, numExpressions));
            }
        }
        for (int i=1;i<=numMatches;i++) {
            if (expressions.size() < 1) {
                break;
            }
            @SuppressWarnings("unused")
            ObjJExpr expr = expressions.remove(0);
            //TODO check var type for match
        }
    }

    /**
     * Checks whether method call is a CPString format method call.
     * @param methodCall method call
     * @return true if method call is string formatting method call, false otherwise.
     */
    private static boolean isCPStringWithFormat(ObjJMethodCall methodCall) {
        if (methodCall.getCallTargetText().equals(ObjJClassType.STRING) && methodCall.getSelectorList().size() == 1) {
            final String selectorText = methodCall.getSelectorList().get(0).getText();
            return selectorText.equals(CPSTRING_INIT_WITH_FORMAT) || selectorText.equals(CPSTRING_STRING_WITH_FORMAT);
        }
        return false;
    }

}
