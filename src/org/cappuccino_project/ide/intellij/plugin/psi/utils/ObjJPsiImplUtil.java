package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import org.cappuccino_project.ide.intellij.plugin.contributor.ObjJMethodCallCompletionContributorUtil;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJIcons;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.*;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.cappuccino_project.ide.intellij.plugin.references.*;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.references.presentation.ObjJSelectorItemPresentation;
import org.cappuccino_project.ide.intellij.plugin.settings.ObjJPluginSettings;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class ObjJPsiImplUtil {

    private static final Logger LOGGER = Logger.getLogger(ObjJPsiImplUtil.class.getName());

    // ============================== //
    // ======= Named Ident ========== //
    // ============================== //

    public static boolean hasText(ObjJVariableName variableName, String text) {
        return ObjJNamedPsiUtil.hasText(variableName, text);
    }

    @NotNull
    public static String getName(@NotNull ObjJVariableName variableName) {
        return ObjJNamedPsiUtil.getName(variableName);
    }

    @NotNull
    public static String getName(ObjJSelector selector) {
        return ObjJMethodPsiUtils.getName(selector);
    }

    @NotNull
    public static String getName(@NotNull ObjJPreprocessorDefineFunction defineFunction) {
        return defineFunction.getFunctionNameAsString();
    }

    @NotNull
    public static String getName(ObjJFunctionName functionName) {
        return functionName.getText();
    }

    @NotNull
    public static String getName(ObjJMethodHeader methodHeader) {
        return ObjJMethodPsiUtils.getName(methodHeader);
    }

    @NotNull
    public static String getName(ObjJInstanceVariableDeclaration variableDeclaration) {
        return variableDeclaration.getVariableName() != null ? variableDeclaration.getVariableName().getText() : "";
    }

    @NotNull
    public static PsiElement getNameIdentifier(@NotNull final ObjJSelector selector) {
        return ObjJMethodPsiUtils.getNameIdentifier(selector);
    }

    @NotNull
    public static TextRange getRangeInElement(@NotNull final ObjJSelector selector) {
        return ObjJMethodPsiUtils.getRangeInElement(selector);
    }

    @NotNull
    public static TextRange getRangeInElement(@NotNull final ObjJVariableName variableName) {
        return new TextRange(0, variableName.getTextLength());
    }

    @NotNull
    public static PsiElement setName(@NotNull ObjJPreprocessorDefineFunction defineFunction, @NotNull String name) {
        return ObjJFunctionDeclarationPsiUtil.setName(defineFunction, name);
    }

    @NotNull
    public static PsiElement setName(@NotNull final ObjJSelector selectorElement, @NotNull final String newSelectorValue) {
        return ObjJMethodPsiUtils.setName(selectorElement, newSelectorValue);
    }

    @NotNull
    public static PsiElement setName(@NotNull ObjJVariableName variableName, @NotNull String newName) {
        return ObjJNamedPsiUtil.setName(variableName, newName);
    }

    @NotNull
    public static PsiElement setName(ObjJHasMethodSelector header, @NotNull String name)  throws IncorrectOperationException {
        return ObjJMethodPsiUtils.setName(header, name);
    }

    @NotNull
    public static PsiElement setName(ObjJInstanceVariableDeclaration instanceVariableDeclaration, @NotNull String newName) {
        return ObjJVariablePsiUtil.setName(instanceVariableDeclaration, newName);
    }


    @NotNull
    public static ObjJFunctionName setName(ObjJFunctionName oldFunctionName, @NotNull String newFunctionName) {
        if (newFunctionName.isEmpty()) {
            return oldFunctionName;
        }
        ObjJFunctionName functionName = ObjJElementFactory.createFunctionName(oldFunctionName.getProject(), newFunctionName);
        if (functionName == null) {
            LOGGER.log(Level.SEVERE, "new function name element is null");
            return oldFunctionName;
        }
        oldFunctionName.getParent().getNode().replaceChild(oldFunctionName.getNode(), functionName.getNode());
        return functionName;
    }

    // ============================== //
    // ====== Navigation Items ====== //
    // ============================== //

    public static ItemPresentation getPresentation(ObjJSelector selector) {
        //LOGGER.log(Level.INFO, "Getting selector item presentation.");
        return new ObjJSelectorItemPresentation(selector);
    }


    // ============================== //
    // =========== String =========== //
    // ============================== //

    @NotNull
    public static String getStringValue(ObjJStringLiteral stringLiteral)
    {
        String rawText = stringLiteral.getText();
        Pattern pattern = Pattern.compile("@?\"(.*)\"|@?'(.*)'");
        MatchResult result = pattern.matcher(rawText);
        try {
            return result.groupCount() > 1 && result.group(1) != null ? result.group(1) : "";
        } catch (Exception e) {
            return "";
        }
    }

    // ============================== //
    // ======= Method Misc ========== //
    // ============================== //

    @NotNull
    public static MethodScope getMethodScope(ObjJMethodHeader methodHeader) {
        return ObjJMethodPsiUtils.getMethodScope(methodHeader);
    }

    @NotNull
    public static MethodScope getMethodScope(ObjJAccessorProperty accessorProperty) {
        return ObjJMethodPsiUtils.getMethodScope(accessorProperty);
    }

    @NotNull
    public static MethodScope getMethodScope(ObjJSelectorLiteral literal) {
        return ObjJMethodPsiUtils.getMethodScope(literal);
    }

    public static boolean isStatic(ObjJHasMethodSelector hasMethodSelector) {
        return ObjJMethodPsiUtils.isStatic(hasMethodSelector);
    }

    @NotNull
    public static List<String> getPossibleCallTargetTypes(ObjJMethodCall callTarget) {
        if (callTarget.getStub() != null) {
            return callTarget.getStub().getPossibleCallTargetTypes();
        }
        return ObjJCallTargetUtil.getPossibleCallTargetTypes(callTarget.getCallTarget());
    }

    // ============================== //
    // ======= Return Types ========= //
    // ============================== //

    @NotNull
    public static String getReturnType(ObjJMethodHeader methodHeader) {
        return ObjJMethodPsiUtils.getReturnType(methodHeader, true);
    }

    @NotNull
    public static String getReturnType(ObjJSelectorLiteral methodHeader) {
        return ObjJMethodPsiUtils.getReturnType(methodHeader);
    }

    @NotNull
    public static String getReturnType(ObjJAccessorProperty accessorProperty) {
        return ObjJMethodPsiUtils.getReturnType(accessorProperty);
    }

    @NotNull
    public static String getCallTargetText(ObjJMethodCall methodCall) {
        return ObjJMethodCallPsiUtil.getCallTargetText(methodCall);
    }


    // ============================== //
    // ========= Selectors ========== //
    // ============================== //

    @Nullable
    public static String getGetter(@NotNull ObjJAccessorProperty property) {
        return ObjJAccessorPropertyPsiUtil.getGetter(property);
    }

    @Nullable
    public static String getSetter(@NotNull ObjJAccessorProperty property) {
        return ObjJAccessorPropertyPsiUtil.getSetter(property);
    }

    @NotNull
    public static String getSelectorString(ObjJMethodHeader methodHeader) {
        return ObjJMethodPsiUtils.getSelectorString(methodHeader);
    }

    @NotNull
    public static String getSelectorString(@NotNull ObjJSelector selector, boolean addSuffix) {
        return ObjJMethodPsiUtils.getSelectorString(selector, addSuffix);
    }

    @NotNull
    public static String getSelectorString(@NotNull ObjJMethodDeclarationSelector selector, boolean addSuffix) {
        return ObjJMethodPsiUtils.getSelectorString(selector, addSuffix);
    }

    @NotNull
    public static String getSelectorString(ObjJAccessorProperty accessorProperty) {
        return ObjJAccessorPropertyPsiUtil.getSelectorString(accessorProperty);
    }

    @NotNull
    public static String getSelectorString(ObjJMethodCall methodCall) {
        return ObjJMethodCallPsiUtil.getSelectorString(methodCall);
    }

    @NotNull
    public static String getSelectorString(ObjJSelectorLiteral selectorLiteral) {
        return ObjJMethodPsiUtils.getSelectorString(selectorLiteral);
    }

    @NotNull
    public static List<String> getSelectorStrings(ObjJMethodCall methodCall) {
        return ObjJMethodCallPsiUtil.getSelectorStrings(methodCall);
    }

    @NotNull
    public static List<String> getSelectorStrings(ObjJMethodHeader methodHeader) {
        return ObjJMethodPsiUtils.getSelectorStrings(methodHeader);
    }

    @NotNull
    public static List<ObjJSelector> getSelectorList(ObjJMethodHeader methodHeader) {
        return ObjJMethodPsiUtils.getSelectorElementsFromMethodDeclarationSelectorList(methodHeader.getMethodDeclarationSelectorList());
    }

    @NotNull
    public static List<ObjJSelector> getSelectorList(ObjJMethodCall methodCall) {
        return ObjJMethodCallPsiUtil.getSelectorList(methodCall);
    }

    @NotNull
    public static List<String> getSelectorStrings(ObjJSelectorLiteral selectorLiteral) {
        if (selectorLiteral.getStub() != null && !selectorLiteral.getStub().getSelectorStrings().isEmpty()) {
            return selectorLiteral.getStub().getSelectorStrings();
        }
        return ObjJMethodPsiUtils.getSelectorStringsFromSelectorList(selectorLiteral.getSelectorList());
    }

    public static List<String> getSelectorStrings(ObjJAccessorProperty accessorProperty) {
        return ObjJAccessorPropertyPsiUtil.getSelectorStrings(accessorProperty);
    }

    @NotNull
    public static List<ObjJSelector> getSelectorList(ObjJAccessorProperty accessorProperty) {
        return ObjJAccessorPropertyPsiUtil.getSelectorList(accessorProperty);
    }

    @Nullable
    public static ObjJSelector findSelectorMatching(ObjJHasMethodSelector method, @NotNull String selectorString) {
        return ObjJMethodPsiUtils.findSelectorMatching(method, selectorString);
    }

    @NotNull
    public static List<ObjJFormalVariableType> getParamTypes(ObjJMethodHeader methodHeader) {
        return ObjJMethodPsiUtils.getParamTypes(methodHeader.getMethodDeclarationSelectorList());
    }

    @NotNull
    public static List<String> getParamTypesAsStrings(ObjJMethodHeader methodHeader) {
        return ObjJMethodPsiUtils.getParamTypesAsString(methodHeader.getMethodDeclarationSelectorList());
    }

    @Nullable
    public static ObjJFormalVariableType getVarType(ObjJMethodDeclarationSelector selector) {
        return ObjJMethodPsiUtils.getVarType(selector);
    }

    @NotNull
    public static List<ObjJMethodHeader> getMethodHeaders(ObjJProtocolDeclaration protocolDeclaration) {
        return ObjJClassDeclarationPsiUtil.getMethodHeaders(protocolDeclaration);
    }

    public static boolean hasMethod(@NotNull ObjJProtocolDeclaration classDeclaration, @NotNull String selector) {
        return ObjJClassDeclarationPsiUtil.hasMethod(classDeclaration, selector);
    }

    @NotNull
    public static List<ObjJMethodHeader> getMethodHeaders(ObjJImplementationDeclaration implementationDeclaration) {
        return ObjJClassDeclarationPsiUtil.getMethodHeaders(implementationDeclaration);
    }

    public static boolean hasMethod(@NotNull ObjJImplementationDeclaration classDeclaration, @NotNull String selector) {
        return ObjJClassDeclarationPsiUtil.hasMethod(classDeclaration, selector);
    }

    // ============================== //
    // ====== Virtual Methods ======= //
    // ============================== //


    @NotNull
    public static List<String> getAccessorPropertyMethods(@NotNull String variableName, @NotNull String varType, ObjJAccessorProperty property) {
        return ObjJAccessorPropertyPsiUtil.getAccessorPropertyMethods(variableName, varType, property);
    }

    @Nullable
    public static ObjJMethodHeaderStub getGetter(ObjJInstanceVariableDeclaration declaration) {
        return ObjJAccessorPropertyPsiUtil.getGetter(declaration);
    }

    @Nullable
    public static String getGetterSelector(@NotNull String variableName, @NotNull String varType, ObjJAccessorProperty property) {
        return ObjJAccessorPropertyPsiUtil.getGetterSelector(variableName, varType, property);
    }

    public static boolean isGetter(ObjJAccessorProperty accessorProperty) {
        return ObjJAccessorPropertyPsiUtil.isGetter(accessorProperty);
    }

    @Nullable
    public static ObjJMethodHeaderStub getSetter(ObjJInstanceVariableDeclaration declaration) {
        return ObjJAccessorPropertyPsiUtil.getSetter(declaration);
    }


    @Nullable
    public static String getSetterSelector(@NotNull String variableName, @NotNull String varType, @NotNull ObjJAccessorProperty property) {
        return ObjJAccessorPropertyPsiUtil.getSetterSelector(variableName, varType, property);
    }

    @Nullable
    public static String getSelectorUntil(ObjJSelector targetSelectorElement, boolean include) {
        return ObjJMethodPsiUtils.getSelectorUntil(targetSelectorElement, include);
    }

    @Nullable
    public static ObjJSelector getThisOrPreviousNonNullSelector(@Nullable ObjJHasMethodSelector hasMethodSelector, @Nullable String subSelector, int selectorIndex) {
        if (hasMethodSelector == null) {
            return null;
        }
        //LOGGER.log(Level.INFO, "Getting thisOrPreviousNonNullSelector: from element of type: <"+hasMethodSelector.getNode().getElementType().toString() + "> with text: <"+hasMethodSelector.getText()+"> ");//declared in <" + getFileName(hasMethodSelector)+">");
        List<ObjJSelector> selectorList = hasMethodSelector.getSelectorList();
        //LOGGER.log(Level.INFO, "Got selector list.");
        if (selectorList.isEmpty()) {
            //LOGGER.log(Level.WARNING, "Cannot get this or previous non null selector when selector list is empty");
            return null;
        }
        int thisSelectorIndex;
        if (selectorIndex < 0 || selectorIndex >= selectorList.size()) {
            thisSelectorIndex = selectorList.size() - 1;
        } else {
            thisSelectorIndex = selectorIndex;
        }
        ObjJSelector selector = selectorList.get(thisSelectorIndex);
        while ((selector == null || selector.getSelectorString(false).isEmpty()) && thisSelectorIndex > 0) {
            selector = selectorList.get(--thisSelectorIndex);
        }
        if (selector != null) {
            return selector;
        }
        Pattern subSelectorPattern = subSelector != null ? Pattern.compile(subSelector.replace(ObjJMethodCallCompletionContributorUtil.CARET_INDICATOR, "(.*)")) : null;
        for (ObjJSelector currentSelector : selectorList) {
            if (currentSelector != null && (subSelectorPattern == null || subSelectorPattern.matcher(currentSelector.getSelectorString(false)).matches())) {
                return currentSelector;
            }
        }
        //LOGGER.log(Level.WARNING, "Failed to find selector matching <"+subSelector+"> or any selector before out of <"+selectorList.size()+"> selectors");
        return null;
    }

    // ============================== //
    // ======== References ========== //
    // ============================== //

    @NotNull
    public static PsiReference getReference(@NotNull ObjJHasMethodSelector hasMethodSelector) {
        return new ObjJMethodCallReferenceProvider(hasMethodSelector);
    }

    @NotNull
    public static PsiReference getReference(@NotNull ObjJSelectorLiteral selectorLiteral) {
        return new ObjJMethodCallReferenceProvider(selectorLiteral);
    }

    @NotNull
    public static PsiReference getReference(@NotNull ObjJSelector selector) {
        return new ObjJSelectorReference(selector);
    }

    @NotNull
    public static PsiReference[] getReferences(@NotNull ObjJSelector selector) {
        //LOGGER.log(Level.INFO, "Getting references(plural) for selector");
        return ReferenceProvidersRegistry.getReferencesFromProviders(selector, PsiReferenceService.Hints.NO_HINTS);
    }

    @NotNull
    public static PsiReference getReference(@NotNull ObjJClassName className) {
        return new ObjJClassNameReference(className);
    }

    @NotNull
    public static PsiReference[] getReferences(ObjJQualifiedReference reference) {
        return PsiReference.EMPTY_ARRAY;
    }

    @NotNull
    public static PsiReference getReference(ObjJVariableName variableName) {
        return new ObjJVariableReference(variableName);
    }

    @NotNull
    public static PsiReference getReference(ObjJFunctionName functionName) {
        return new ObjJFunctionNameReference(functionName);
    }

    @NotNull
    public static PsiReference[] getReferences(@NotNull ObjJClassName className) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(className, PsiReferenceService.Hints.NO_HINTS);
    }

    @Nullable
    public static ObjJSelectorLiteral getSelectorLiteralReference(ObjJHasMethodSelector hasSelectorElement) {
        return ObjJMethodPsiUtils.getSelectorLiteralReference(hasSelectorElement);
    }

    // ============================== //
    // ======== Class Decs ========== //
    // ============================== //

    @NotNull
    public static List<ObjJClassName> getAllClassNameElements(Project project) {
        return ObjJClassDeclarationPsiUtil.getAllClassNameElements(project);
    }

    public static boolean isCategory(@NotNull ObjJImplementationDeclaration implementationDeclaration) {
        return ObjJClassDeclarationPsiUtil.isCategory(implementationDeclaration);
    }

    @Nullable
    public static ObjJClassDeclarationElement getContainingClass(@NotNull PsiElement element) {
        return ObjJHasContainingClassPsiUtil.getContainingClass(element);
    }

    @NotNull
    public static String getContainingClassName(ObjJMethodHeader methodHeader) {
        return ObjJHasContainingClassPsiUtil.getContainingClassName(methodHeader);
    }

    @NotNull
    public static String getContainingClassName(ObjJCompositeElement compositeElement) {
        return ObjJHasContainingClassPsiUtil.getContainingClassName(compositeElement);
    }

    @NotNull
    public static String getContainingClassName(@Nullable ObjJClassDeclarationElement classDeclarationElement) {
        return ObjJHasContainingClassPsiUtil.getContainingClassName(classDeclarationElement);
    }

    @NotNull
    public static String getContainingClassName(@Nullable ObjJSelectorLiteral selectorLiteral) {
        return ObjJHasContainingClassPsiUtil.getContainingClassName(selectorLiteral);
    }

    @NotNull
    public static List<String> getAllInheritedClasses(@NotNull String className, @NotNull Project project) {
        return ObjJInheritanceUtil.getAllInheritedClasses(className, project);
    }

    @Nullable
    public static String getContainingSuperClassName(@NotNull ObjJCompositeElement element) {
        return ObjJHasContainingClassPsiUtil.getContainingSuperClassName(element);
    }

    @Nullable
    public static String getSuperClassName(ObjJImplementationDeclaration implementationDeclaration) {
        return ObjJClassDeclarationPsiUtil.getSuperClassName(implementationDeclaration);
    }

    public static boolean hasContainingClass(ObjJHasContainingClass hasContainingClass, @Nullable String className) {
        return className != null && Objects.equals(hasContainingClass.getContainingClassName(), className);
    }

    // ============================== //
    // ========= Var Types ========== //
    // ============================== //


    @Nullable
    public static String getVarType(ObjJAccessorProperty accessorProperty) {
        return ObjJAccessorPropertyPsiUtil.getVarType(accessorProperty);
    }

    @Nullable
    public static ObjJVariableName getInstanceVarDeclarationFromDeclarations(@NotNull List<ObjJInstanceVariableDeclaration> instanceVariableDeclarations, @NotNull String variableName) {
        return ObjJVariablePsiUtil.getInstanceVarDeclarationFromDeclarations(instanceVariableDeclarations, variableName);
    }

    @Nullable
    public static ObjJVariableName getLastVar(ObjJQualifiedReference qualifiedReference) {
        return ObjJVariablePsiUtil.getLastVar(qualifiedReference);
    }

    @NotNull
    public static String getIdType(@NotNull ObjJVarTypeId varTypeId) {
        return ObjJMethodPsiUtils.getIdReturnType(varTypeId);
    }

    @NotNull
    public static String getIdType(@NotNull ObjJVarTypeId varTypeId, boolean follow) {
        return ObjJMethodPsiUtils.getIdReturnType(varTypeId, follow);
    }

    // ============================== //
    // =========== Blocks =========== //
    // ============================== //

    @Nullable
    public static ObjJBlock getBlock(ObjJExpr expr) {
        return ObjJBlockPsiUtil.getBlock(expr);
    }

    @NotNull
    public static List<ObjJBlock> getBlockList(ObjJTryStatement expr) {
        return ObjJBlockPsiUtil.getTryStatementBlockList(expr);
    }

    @NotNull
    public static List<ObjJBlock> getBlockList(ObjJCompositeElement element) {
        return ObjJTreeUtil.getChildrenOfTypeAsList(element, ObjJBlock.class);
    }

    @NotNull
    public static List<ObjJBlock> getBlockList(ObjJCaseClause element) {
        return Collections.singletonList(element.getBlock());
    }

    @Nullable
    public static ObjJBlock getBlock(@NotNull ObjJPreprocessorDefineFunction function) {
        return function.getPreprocessorDefineBody() != null ? function.getPreprocessorDefineBody().getBlock() : null;
    }

    @Nullable
    public static ObjJBlock getOpenBrace(@SuppressWarnings("unused") @NotNull ObjJPreprocessorIfStatement ifStatement) {
        return null;
    }

    // ============================== //
    // ========== Function ========== //
    // ============================== //

    @NotNull
    public static String getName(ObjJFunctionDeclaration functionDeclaration) {
        return ObjJFunctionDeclarationPsiUtil.getName(functionDeclaration);
    }

    /**
     * Renames function
     * @param functionDeclaration function to rename
     * @param name new function name
     * @return new function name
     * @throws IncorrectOperationException exception
     */
    @NotNull
    public static ObjJFunctionName setName(@NotNull ObjJFunctionDeclaration functionDeclaration, @NotNull String name) throws IncorrectOperationException {
        return ObjJFunctionDeclarationPsiUtil.setName(functionDeclaration, name);
    }

    /**
     * Renames function literal node.
     * @param functionLiteral the literal to rename
     * @param name the new name
     * @return this function literal
     * @throws IncorrectOperationException exception
     */
    @NotNull
    public static ObjJFunctionLiteral setName(@NotNull ObjJFunctionLiteral functionLiteral, @NotNull String name) throws IncorrectOperationException {
        return ObjJFunctionDeclarationPsiUtil.setName(functionLiteral, name);
    }



    @Nullable
    public static ObjJNamedElement getFunctionNameNode(@NotNull ObjJFunctionLiteral functionLiteral) {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameNode(functionLiteral);
    }

    @Nullable
    public static ObjJNamedElement getFunctionNameNode(@NotNull ObjJPreprocessorDefineFunction functionDec) {
        return functionDec.getFunctionName();
    }

    @NotNull
    public static String getQualifiedNameText(ObjJFunctionCall functionCall) {
        return ObjJFunctionDeclarationPsiUtil.getQualifiedNameText(functionCall);
    }

    @NotNull
    public static String getFunctionNameAsString(ObjJFunctionLiteral functionLiteral) {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameAsString(functionLiteral);
    }

    @NotNull
    public static List<String> getFunctionNamesAsString(ObjJFunctionLiteral functionLiteral) {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNamesAsString(functionLiteral);
    }

    @NotNull
    public static String getFunctionNameAsString(ObjJFunctionDeclaration functionDeclaration) {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameAsString(functionDeclaration);
    }

    @NotNull
    public static String getFunctionNameAsString(ObjJPreprocessorDefineFunction functionDeclaration) {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameAsString(functionDeclaration);
    }

    @NotNull
    public static List<ObjJVariableName> getParamNameElements(@NotNull
                                                                 ObjJFunctionDeclarationElement functionDeclaration) {
        return ObjJFunctionDeclarationPsiUtil.getParamNameElements(functionDeclaration);
    }

    @NotNull
    public static List<String> getParamNames(@NotNull ObjJFunctionDeclarationElement functionDeclaration) {
        return ObjJFunctionDeclarationPsiUtil.getParamNames(functionDeclaration);
    }

    @NotNull
    public static String getReturnType(@NotNull ObjJFunctionDeclaration functionDeclaration) {
        return ObjJFunctionDeclarationPsiUtil.getReturnType(functionDeclaration);
    }

    @NotNull
    public static String getReturnType(@NotNull ObjJFunctionLiteral functionLiteral) {
        return ObjJFunctionDeclarationPsiUtil.getReturnType(functionLiteral);
    }

    @NotNull
    public static String getReturnType(@NotNull ObjJPreprocessorDefineFunction functionDefinition) {
        return ObjJFunctionDeclarationPsiUtil.getReturnType(functionDefinition);
    }


    // ============================== //
    // ===== QualifiedReference ===== //
    // ============================== //

    @NotNull
    public static String getPartsAsString(ObjJQualifiedReference qualifiedReference) {
        return (qualifiedReference.getMethodCall() != null ? "{?}" : "") + getPartsAsString(ObjJTreeUtil.getChildrenOfTypeAsList(qualifiedReference, ObjJQualifiedNamePart.class));
    }

    @NotNull
    public static List<String> getPartsAsStringArray(ObjJQualifiedReference qualifiedReference) {
        return getPartsAsStringArray(ObjJTreeUtil.getChildrenOfTypeAsList(qualifiedReference, ObjJQualifiedNamePart.class));
    }

    @NotNull
    public static List<String> getPartsAsStringArray(@Nullable List<ObjJQualifiedNamePart> qualifiedNameParts) {
        if (qualifiedNameParts == null) {
            return Collections.emptyList();
        }
        List<String> out = new ArrayList<>();
        for (ObjJQualifiedNamePart part : qualifiedNameParts){
            out.add(part.getQualifiedNameText() != null ? part.getQualifiedNameText() : "");
        }
        return out;
    }

    @NotNull
    public static String getPartsAsString(List<ObjJQualifiedNamePart> qualifiedNameParts) {
        return ArrayUtils.join(getPartsAsStringArray(qualifiedNameParts), ".");
    }

    @NotNull
    public static String getQualifiedNameText(ObjJVariableName variableName) {
        return variableName.getText();
    }

    @NotNull
    public static String toString(ObjJVariableName variableName) {
        return ObjJVariablePsiUtil.toString(variableName);
    }

    public static String getDescriptiveText(PsiElement psiElement) {
        if (psiElement instanceof ObjJSelector) {
            return getSelectorDescriptiveName((ObjJSelector) psiElement);
        } else if (psiElement instanceof ObjJVariableName) {
            return ((ObjJVariableName)psiElement).getText();
        } else if (psiElement instanceof ObjJClassName) {
            return getClassDescriptiveText((ObjJClassName) psiElement);
        } else if (psiElement instanceof  ObjJFunctionName) {
            return psiElement.getText();
        }
        return "";
    }

    private static String getClassDescriptiveText(ObjJClassName classNameElement)
    {
        ObjJClassDeclarationElement classDeclarationElement = classNameElement.getParentOfType(ObjJClassDeclarationElement.class);
        String className = classNameElement.getText();
        if (classDeclarationElement == null || !classDeclarationElement.getClassNameString().equals(className)) {
            return className;
        }
        if (classDeclarationElement instanceof ObjJImplementationDeclaration) {
            final ObjJImplementationDeclaration implementationDeclaration = ((ObjJImplementationDeclaration) classDeclarationElement);
            if (implementationDeclaration.getCategoryName() != null) {
                className += " (" + implementationDeclaration.getCategoryName().getClassName().getText() + ")";
            }
        }
        return className;
    }

    @NotNull
    public static String getSelectorDescriptiveName(@NotNull ObjJSelector selector) {
        ObjJSelectorLiteral selectorLiteral = ObjJTreeUtil.getParentOfType(selector, ObjJSelectorLiteral.class);
        if (selectorLiteral != null) {
            return "@selector("+selectorLiteral.getSelectorString()+")";
        }
        ObjJInstanceVariableDeclaration variableDeclaration = ObjJTreeUtil.getParentOfType(selector, ObjJInstanceVariableDeclaration.class);
        if (variableDeclaration != null) {
            String className = variableDeclaration.getContainingClassName();
            final ObjJAccessorProperty property = ObjJTreeUtil.getParentOfType(selector, ObjJAccessorProperty.class);
            final String propertyString = property != null ? property.getAccessorPropertyType().getText() + "=" : "";
            final String returnType = variableDeclaration.getStub() != null ? variableDeclaration.getStub().getVarType():variableDeclaration.getFormalVariableType().getText();
            return "- ("+returnType+") @accessors(" + propertyString + selector.getSelectorString(false)+")";
        }
        ObjJMethodCall methodCall = ObjJTreeUtil.getParentOfType(selector, ObjJMethodCall.class);
        String selectorString = null;
        String className = null;
        if (methodCall != null) {
            selectorString = methodCall.getSelectorString();
        }
        if (selectorString == null) {
            ObjJMethodHeaderDeclaration methodHeader = ObjJTreeUtil.getParentOfType(selector, ObjJMethodHeaderDeclaration.class);
            if (methodHeader != null) {
                selectorString = methodHeader instanceof  ObjJMethodHeader ? getFormattedSelector((ObjJMethodHeader)methodHeader) : methodHeader.getSelectorString();
                String methodScopeString = methodHeader.isStatic() ? "+" :"-";
                return  methodScopeString + " ("+methodHeader.getReturnType()+")" + selectorString;
            }
        }
        selectorString = selectorString != null ? selectorString : selector.getSelectorString(true);
        return "[* " + selectorString + "]";
    }

    private static String getFormattedSelector(ObjJMethodHeader methodHeader) {
        StringBuilder builder = new StringBuilder();
        for (ObjJMethodDeclarationSelector selector : methodHeader.getMethodDeclarationSelectorList()) {
            if (selector.getSelector() != null) {
                builder.append(selector.getSelector().getSelectorString(false));
            }
            builder.append(ObjJMethodPsiUtils.SELECTOR_SYMBOL);
            if (selector.getFormalVariableType() != null) {
                builder.append("(").append(selector.getFormalVariableType().getText()).append(")");
            }
            if (selector.getVariableName() != null) {
                builder.append(selector.getVariableName().getText());
            }
            builder.append(" ");
        }
        return builder.substring(0, builder.length()-1);
    }

    // ============================== //
    // ========== Imports =========== //
    // ============================== //

    @NotNull
    public static String getFileName(@NotNull ObjJFrameworkReference reference) {
        return ObjJImportPsiUtils.getFileName(reference);
    }

    @NotNull
    public static String getFileName(@NotNull ObjJImportFramework framework) {
        return ObjJImportPsiUtils.getFileName(framework);
    }

    @NotNull
    public static String getFileName(@NotNull ObjJIncludeFramework framework) {
        return ObjJImportPsiUtils.getFileName(framework);
    }

    @NotNull
    public static String getFileName(@NotNull ObjJImportFile framework) {
        return ObjJImportPsiUtils.getFileName(framework);
    }

    @NotNull
    public static String getFileName(@NotNull ObjJIncludeFile framework) {
        return ObjJImportPsiUtils.getFileName(framework);
    }

    @Nullable
    public static String getFrameworkName(@NotNull ObjJFrameworkReference reference) {
        return ObjJImportPsiUtils.getFrameworkName(reference);
    }


    @Nullable
    public static String getFrameworkName(@NotNull ObjJImportFramework framework) {
        return ObjJImportPsiUtils.getFrameworkName(framework);
    }

    @Nullable
    public static String getFrameworkName(@NotNull ObjJIncludeFile framework) {
        return ObjJImportPsiUtils.getFrameworkName(framework);
    }

    @Nullable
    public static String getFrameworkName(@NotNull ObjJImportFile framework) {
        return ObjJImportPsiUtils.getFrameworkName(framework);
    }

    @Nullable
    public static String getFrameworkName(@NotNull ObjJIncludeFramework framework) {
        return ObjJImportPsiUtils.getFrameworkName(framework);
    }

    // ============================== //
    // ===== Global Variables ======= //
    // ============================== //
    @Nullable
    public static String getFileName(@NotNull ObjJGlobalVariableDeclaration declaration) {
        return ObjJVariablePsiUtil.getFileName(declaration);
    }

    @NotNull
    public static String getVariableNameString(@NotNull ObjJGlobalVariableDeclaration declaration) {
       return ObjJVariablePsiUtil.getVariableNameString(declaration);
    }

    @Nullable
    public static String getVariableType(@NotNull ObjJGlobalVariableDeclaration declaration) {
        return ObjJVariablePsiUtil.getVariableType(declaration);
    }

    // ============================== //
    // ===== VariableAssignments ==== //
    // ============================== //

    @NotNull
    public static ObjJExpr getAssignedValue(@NotNull ObjJVariableAssignmentLogical assignmentLogical) {
        return ObjJVariableAssignmentsPsiUtil.getAssignedValue(assignmentLogical);
    }

    @NotNull
    public static List<ObjJQualifiedReference> getQualifiedReferenceList(@NotNull ObjJVariableAssignmentLogical assignmentLogical) {
        return Collections.singletonList(assignmentLogical.getQualifiedReference());
    }

    // ============================== //
    // ====== Iterator Elements ===== //
    // ============================== //

    @Nullable
    public static ObjJExpr getConditionalExpression(@Nullable ObjJDoWhileStatement doWhileStatement) {
        if (doWhileStatement == null || doWhileStatement.getConditionExpression() == null) {
            return null;
        }
        return doWhileStatement.getConditionExpression().getExpr();
    }

    // ============================== //
    // =========== Misc ============= //
    // ============================== //

    @Nullable
    public static String getFileName(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }
        if (element.getContainingFile() == null || element.getContainingFile().getVirtualFile() == null) {
            return null;
        }
        return element.getContainingFile().getVirtualFile().getName();
    }

    // ============================== //
    // ====== Should Resolve ======== //
    // ============================== //

    public static boolean shouldResolve(@Nullable PsiElement psiElement) {
        return ObjJResolveableElementUtil.shouldResolve(psiElement);
    }

    public static boolean shouldResolve(@Nullable ObjJClassDeclarationElement psiElement) {
        return ObjJResolveableElementUtil.shouldResolve(psiElement);
    }

    public static boolean shouldResolve(@Nullable PsiElement psiElement, @Nullable String shouldNotResolveLoggingStatement) {
        return ObjJResolveableElementUtil.shouldResolve(psiElement, shouldNotResolveLoggingStatement);
    }

    public static boolean shouldResolve(@Nullable ObjJHasContainingClass hasContainingClass) {
        return ObjJResolveableElementUtil.shouldResolve(hasContainingClass);
    }

    // ============================== //
    // =========== PARSER =========== //
    // ============================== //

    public static boolean eos(@Nullable PsiElement compositeElement) {
        if (compositeElement == null) {
            return false;
        }
        ASTNode ahead = ObjJTreeUtil.getNextNode(compositeElement);
        if (ahead == null && compositeElement.getParent() != null) {
            return eos(compositeElement.getParent());
        }
        boolean hadLineTerminator = false;
        while (ahead != null && (ahead.getElementType() == com.intellij.psi.TokenType.WHITE_SPACE || ahead.getElementType() == ObjJTypes.ObjJ_LINE_TERMINATOR)) {
            if (ahead == ObjJTypes.ObjJ_LINE_TERMINATOR) {
                hadLineTerminator = true;
            }
            while (ahead.getTreeNext() == null && ahead.getTreeParent() != null) {
                ahead = ahead.getTreeParent();
            }
            ahead = ahead.getTreeNext();
        }
        return ahead != null && eosToken(ahead.getElementType(), hadLineTerminator);
    }

    public static boolean eosToken(@Nullable IElementType ahead, boolean hadLineTerminator) {

        if (ahead == null) {
            LOGGER.log(Level.INFO, "EOS assumed as ahead == null");
            return true;
        }

        // Check if the token is, or contains a line terminator.
        //LOGGER.log(Level.INFO, String.format("LineTerminatorAheadToken: <%s>; CurrentToken <%s> Is Line Terminator?:  <%b>", ahead, builder_.getTokenText(), isLineTerminator));
        boolean isLineTerminator =
                (ahead == ObjJTypes.ObjJ_BLOCK_COMMENT) ||
                        (ahead == ObjJTypes.ObjJ_SINGLE_LINE_COMMENT)   ||
                        (ahead == ObjJTypes.ObjJ_ELSE) ||
                        (ahead == ObjJTypes.ObjJ_IF) ||
                        (ahead == ObjJTypes.ObjJ_CLOSE_BRACE) ||
                        (ahead == ObjJTypes.ObjJ_WHILE) ||
                        (ahead == ObjJTypes.ObjJ_DO) ||
                        (ahead == ObjJTypes.ObjJ_PP_PRAGMA) ||
                        (ahead == ObjJTypes.ObjJ_PP_IF) ||
                        (ahead == ObjJTypes.ObjJ_PP_ELSE) ||
                        (ahead == ObjJTypes.ObjJ_PP_ELSE_IF) ||
                        (ahead == ObjJTypes.ObjJ_PP_END_IF) ||
                        (ahead == ObjJTypes.ObjJ_SEMI_COLON);
        if (isLineTerminator || !ObjJPluginSettings.inferEOS()) {
            if (!isLineTerminator) {
                //LOGGER.log(Level.INFO, "Failed EOS check. Ahead token is <"+ahead.toString()+">");
            }
            return isLineTerminator;
        }
        isLineTerminator = hadLineTerminator && (
                (ahead == ObjJTypes.ObjJ_BREAK) ||
                        (ahead == ObjJTypes.ObjJ_VAR) ||
                        (ahead == ObjJTypes.ObjJ_AT_IMPLEMENTATION) ||
                        (ahead == ObjJTypes.ObjJ_AT_IMPORT) ||
                        (ahead == ObjJTypes.ObjJ_AT_GLOBAL) ||
                        (ahead == ObjJTypes.ObjJ_TYPE_DEF) ||
                        (ahead == ObjJTypes.ObjJ_FUNCTION) ||
                        (ahead == ObjJTypes.ObjJ_AT_PROTOCOL) ||
                        (ahead == ObjJTypes.ObjJ_CONTINUE) ||
                        (ahead == ObjJTypes.ObjJ_CONST) ||
                        (ahead == ObjJTypes.ObjJ_RETURN) ||
                        (ahead == ObjJTypes.ObjJ_SWITCH) ||
                        (ahead == ObjJTypes.ObjJ_LET) ||
                        (ahead == ObjJTypes.ObjJ_CASE)
        );
        return isLineTerminator;
    }

    public static boolean hasNodeType(@Nullable PsiElement element, @NotNull IElementType elementType) {
        return element != null && element.getNode().getElementType() == elementType;
    }

    // ============================== //
    // ======= Presentation ========= //
    // ============================== //

    public static ItemPresentation getPresentation(@NotNull ObjJImplementationDeclaration implementationDeclaration) {
        return ObjJClassDeclarationPsiUtil.getPresentation(implementationDeclaration);
    }

    public static ItemPresentation getPresentation(@NotNull ObjJProtocolDeclaration protocolDeclaration) {
        return ObjJClassDeclarationPsiUtil.getPresentation(protocolDeclaration);
    }

    public static Icon getIcon(PsiElement element) {
        if (element instanceof ObjJClassName) {
            ObjJClassDeclarationElement classDeclarationElement = ((ObjJClassName)element).getParentOfType(ObjJClassDeclarationElement.class);

            String className = element.getText();
            if (classDeclarationElement == null || !classDeclarationElement.getClassNameString().equals(className)) {
                return null;
            }
            if (classDeclarationElement instanceof ObjJImplementationDeclaration) {
                ObjJImplementationDeclaration implementationDeclaration = ((ObjJImplementationDeclaration) element.getParent());
                if (implementationDeclaration.isCategory()) {
                    return ObjJIcons.CATEGORY_ICON;
                } else {
                    return ObjJIcons.CLASS_ICON;
                }
            } else if (classDeclarationElement instanceof ObjJProtocolDeclaration) {
                return ObjJIcons.PROTOCOL_ICON;
            }
            return null;
        }

        if (element instanceof ObjJFunctionName) {
            return ObjJIcons.FUNCTION_ICON;
        }

        if (element instanceof ObjJVariableName) {
            return ObjJIcons.VARIABLE_ICON;
        }

        if (element instanceof ObjJSelector) {
            if (isIn(element, ObjJMethodHeaderDeclaration.class)) {
                return ObjJIcons.METHOD_ICON;
            }
            if (isIn(element, ObjJInstanceVariableDeclaration.class)) {
                return ObjJIcons.ACCESSOR_ICON;
            }
            if (isIn(element, ObjJSelectorLiteral.class)) {
                return ObjJIcons.SELECTOR_ICON;
            }
        }
        return null;
    }

    public static <PsiT extends PsiElement>  boolean isIn(PsiElement element, Class<PsiT> parentClass) {
        return ObjJTreeUtil.getParentOfType(element, parentClass) != null;
    }

}
