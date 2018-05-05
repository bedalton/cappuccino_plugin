package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJSelectorInferredMethodIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJVarTypeIdStub;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.*;
import static org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils.EMPTY_STRING_ARRAY;

public class ObjJMethodPsiUtils {

    private static final Logger LOGGER = Logger.getLogger(ObjJMethodPsiUtils.class.getCanonicalName());
    public static final String SELECTOR_SYMBOL = ":";
    public static final String EMPTY_SELECTOR = getSelectorString("{EMPTY}");
    public static final String ALLOC_SELECTOR = getSelectorString("alloc");


    @NotNull
    @Contract(pure = true)
    public static String getSelectorString(@Nullable String selector) {
        return (selector != null ? selector : "") + SELECTOR_SYMBOL;
    }

    @NotNull
    public static String getSelectorStringFromSelectorStrings(@NotNull List<String> selectors) {
        return ArrayUtils.join(selectors, SELECTOR_SYMBOL, true);
    }

    @NotNull
    public static String getSelectorStringFromSelectorList(@NotNull List<ObjJSelector> selectors) {
        return getSelectorStringFromSelectorStrings(getSelectorStringsFromSelectorList(selectors));
    }

    @NotNull
    public static List<ObjJSelector> getSelectorElementsFromMethodDeclarationSelectorList(List<ObjJMethodDeclarationSelector> declarationSelectors) {
        if (declarationSelectors == null || declarationSelectors.isEmpty()) {
            return Collections.emptyList();
        }
        List<ObjJSelector> out = new ArrayList<>();
        for (ObjJMethodDeclarationSelector selector : declarationSelectors) {
            out.add(selector.getSelector());
        }
        return out;
    }

    @Contract("null -> !null")
    public static List<ObjJFormalVariableType> getParamTypes(@Nullable List<ObjJMethodDeclarationSelector> declarationSelectors) {
        if (declarationSelectors == null || declarationSelectors.isEmpty()) {
            return Collections.emptyList();
        }
        List<ObjJFormalVariableType> out = new ArrayList<>();
        for (ObjJMethodDeclarationSelector selector : declarationSelectors) {
            out.add(selector.getVarType());
        }
        return out;
    }


    @Contract("null -> !null")
    public static List<String> getParamTypesAsString(@Nullable List<ObjJMethodDeclarationSelector> declarationSelectors) {
        if (declarationSelectors == null || declarationSelectors.isEmpty()) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> out = new ArrayList<>();
        for (ObjJMethodDeclarationSelector selector : declarationSelectors) {
            out.add(selector.getVarType() != null ? selector.getVarType().getText() : null);
        }
        return out;
    }

    @NotNull
    public static List<String> getSelectorStringsFromSelectorList(@NotNull final List<ObjJSelector> selectorElements) {
        if (selectorElements.isEmpty()) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> selectorStrings = new ArrayList<>();
        for (ObjJSelector selector : selectorElements) {
            selectorStrings.add(getSelectorString(selector, false));
        }
        return selectorStrings;
    }

    @NotNull
    private static List<String> getSelectorStringsFromMethodDeclarationSelectorList(
            @NotNull
            final List<ObjJMethodDeclarationSelector> selectorElements) {
        if (selectorElements.isEmpty()) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> selectorStrings = new ArrayList<>();
        for (ObjJMethodDeclarationSelector selectorElement : selectorElements) {
            selectorStrings.add(getSelectorString(selectorElement, false));
        }
        return selectorStrings;
    }

    @NotNull
    public static String getSelectorString(final @Nullable ObjJMethodDeclarationSelector selectorElement, boolean addSuffix) {
        final ObjJSelector selector = selectorElement != null ? selectorElement.getSelector()  : null;
        return getSelectorString(selector, addSuffix);
    }

    @Contract(pure = true)
    @NotNull
    public static String getSelectorString(@Nullable final ObjJSelector selectorElement, boolean addSuffix) {
        final String selector = selectorElement != null ? selectorElement.getText() : "";
        if (addSuffix) {
            return selector + SELECTOR_SYMBOL;
        } else {
            return selector;
        }
    }

    @NotNull
    public static String getSelectorString(ObjJSelectorLiteral selectorLiteral) {
        if (selectorLiteral.getStub() != null) {
            return selectorLiteral.getStub().getSelectorString();
        }
        return getSelectorStringFromSelectorStrings(selectorLiteral.getSelectorStrings());
    }


    @Nullable
    public static String getSelectorUntil(ObjJSelector targetSelectorElement, boolean include) {
        final ObjJHasMethodSelector parent = ObjJTreeUtil.getParentOfType(targetSelectorElement, ObjJHasMethodSelector.class);
        if (parent == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        @SuppressWarnings("unchecked")
        List<ObjJSelector> selectors = parent.getSelectorList();
        for (ObjJSelector subSelector : selectors) {
            if (subSelector == targetSelectorElement) {
                if (include) {
                    builder.append(subSelector.getSelectorString(true));
                }
                break;
            } else {
                builder.append(ObjJMethodPsiUtils.getSelectorString(subSelector, true));
            }
        }
        return builder.toString();
    }

    /**
     * Gets all selector sibling selector strings after the given index
     * @param selector base selector
     * @param selectorIndex selector index
     * @return list of trailing sibling selectors as strings
     */
    public static List<String> getTrailingSelectorStrings(@NotNull ObjJSelector selector, int selectorIndex) {
        ObjJMethodHeaderDeclaration methodHeaderDeclaration = ObjJTreeUtil.getParentOfType(selector, ObjJMethodHeaderDeclaration.class);
        List<String> temporarySelectorsList = methodHeaderDeclaration != null ? methodHeaderDeclaration.getSelectorStrings() : EMPTY_STRING_ARRAY;
        int numSelectors = temporarySelectorsList.size();
        return numSelectors > selectorIndex ? temporarySelectorsList.subList(selectorIndex+1, numSelectors) : Collections.EMPTY_LIST;
    }


    @Nullable
    public static ObjJSelectorLiteral getSelectorLiteralReference(ObjJHasMethodSelector hasSelectorElement) {
        final String containingClassName = hasSelectorElement.getContainingClassName();
        //ProgressIndicatorProvider.checkCanceled();
        if (DumbService.getInstance(hasSelectorElement.getProject()).isDumb()) {
            return null;
        }
        for (ObjJSelectorLiteral selectorLiteral : ObjJSelectorInferredMethodIndex.getInstance().get(containingClassName, hasSelectorElement.getProject())) {
            if (Objects.equals(selectorLiteral.getContainingClassName(),containingClassName)) {
                return selectorLiteral;
            }
        }
        return null;
    }

    // ============================== //
    // ======== Return Type ========= //
    // ============================== //


    @NotNull
    public static String getReturnType(@NotNull ObjJMethodHeader methodHeader, boolean follow) {
        if (methodHeader.getStub() != null) {
            return methodHeader.getStub().getReturnTypeAsString();
        }
        final ObjJMethodHeaderReturnTypeElement returnTypeElement = methodHeader.getMethodHeaderReturnTypeElement();
        if (returnTypeElement == null) {
            return UNDETERMINED;
        }
        if (returnTypeElement.getAtAction() != null) {
            return AT_ACTION;
        }
        if (returnTypeElement.getFormalVariableType() != null) {
            final ObjJFormalVariableType formalVariableType = returnTypeElement.getFormalVariableType();
            if (formalVariableType.getVarTypeId() != null) {
                if (follow) {
                    String returnType = formalVariableType.getVarTypeId().getIdType(false);
                    //LOGGER.log(Level.INFO, "Found return type id to be: <"+returnType+">");
                    return returnType;
                }
            }
            return formalVariableType.getText();
        }
        if (returnTypeElement.getVoid() != null) {
            return VOID_CLASS_NAME;
        }
        return UNDETERMINED;
    }

    @NotNull
    public static String getIdReturnType(@NotNull ObjJVarTypeId varTypeId) {
        return getIdReturnType(varTypeId, true);
    }
    @NotNull
    public static String getIdReturnType(@NotNull ObjJVarTypeId varTypeId, boolean follow) {
        if (varTypeId.getStub() != null) {
            ObjJVarTypeIdStub stub = varTypeId.getStub();
            if (!ObjJMethodCallPsiUtil.isUniversalMethodCaller(stub.getIdType()) && stub.getIdType() != null && !stub.getIdType().equals("id")) {
                return stub.getIdType();
            }
        }
        if (varTypeId.getClassName() != null) {
            return varTypeId.getClassName().getText();
        }
        return varTypeId.getText();
        /*
        final ObjJMethodDeclaration declaration = varTypeId.getParentOfType(ObjJMethodDeclaration.class);
        if (declaration == null) {
            //LOGGER.log(Level.INFO, "VarTypeId: Not Contained in a method declaration");
            return ObjJClassType.ID;
        }
        String returnType;
        try {
            returnType = getReturnTypeFromReturnStatements(declaration, follow);
        } catch (ObjJExpressionReturnTypeUtil.MixedReturnTypeException e) {
            returnType = e.getReturnTypesList().get(0);
        }
        if (Objects.equals(returnType,UNDETERMINED)) {
            returnType = null;
        }
        /*
        if (returnType != null) {
            LOGGER.log(Level.INFO, !returnType.equals("id") ? "VarTypeId: id <" + returnType + ">" : "VarTypeId: failed to infer var type");
        } else {
            LOGGER.log(Level.INFO, "VarTypeId: getTypeFromReturnStatements returned null");
        }* /
        return returnType != null ? returnType : varTypeId.getText();*/
    }

    @Nullable
    private static String getReturnTypeFromReturnStatements(ObjJMethodDeclaration declaration, boolean follow) throws ObjJExpressionReturnTypeUtil.MixedReturnTypeException {
        String returnType;
        List<String> returnTypes = new ArrayList<>();
        List<ObjJReturnStatement> returnStatements = ObjJBlockPsiUtil.getBlockChildrenOfType(declaration.getBlock(), ObjJReturnStatement.class, true);
        if (returnStatements.isEmpty()) {
            //LOGGER.log(Level.INFO, "Cannot get return type from return statements, as no return statements exist");
            return null;
        } else {
            //LOGGER.log(Level.INFO, "Found <"+returnStatements.size()+"> return statements");
        }
        for (ObjJReturnStatement returnStatement : returnStatements) {
            if (returnStatement.getExpr() == null) {
                continue;
            }
            returnType = ObjJExpressionReturnTypeUtil.getReturnType(returnStatement.getExpr(), false);
            if (returnType == null) {
                continue;
            }
            if (returnType.equals(ObjJClassType.UNDETERMINED)) {
                continue;
            }
            if (returnTypes.contains(returnType)) {
                return returnType;
            }
            returnTypes.add(returnType);
        }
        if (returnTypes.size() == 1) {
            return returnTypes.get(0);
        }
        if (returnTypes.size() > 1) {
            //LOGGER.log(Level.INFO, "Found more than one possible return type");
            return ArrayUtils.join(returnTypes);
        }
        return UNDETERMINED;
    }

    @NotNull
    public static String getReturnType(ObjJAccessorProperty accessorProperty) {
        if (accessorProperty.getStub() != null) {
            return accessorProperty.getStub().getReturnTypeAsString();
        }
        String variableType = accessorProperty.getVarType();
        return variableType != null ? variableType : UNDETERMINED;
    }

    @NotNull
    public static String getReturnType(
            @SuppressWarnings("unused")
                    ObjJSelectorLiteral methodHeader) {
        return UNDETERMINED;
    }

    // ============================== //
    // ===== Selector Functions ===== //
    // ============================== //
    @NotNull
    public static List<String> getSelectorStrings(ObjJMethodHeader methodHeader) {
        if (methodHeader.getStub() != null) {
            return methodHeader.getStub().getSelectorStrings();
        }
        return ObjJMethodPsiUtils.getSelectorStringsFromMethodDeclarationSelectorList(methodHeader.getMethodDeclarationSelectorList());
    }



    @Nullable
    public static ObjJSelector findSelectorMatching(ObjJHasMethodSelector method, @NotNull String selectorString) {
        for (Object selectorOb : method.getSelectorList()) {
            if (!(selectorOb instanceof ObjJSelector)) {
                continue;
            }
            ObjJSelector selector = (ObjJSelector)selectorOb;
            if (selector.getSelectorString(false).equals(selectorString) || selector.getSelectorString(true).equals(selectorString)) {
                return selector;
            }
        }
        return null;
    }

    @NotNull
    public static String getSelectorString(ObjJMethodHeader methodHeader) {
        if (methodHeader.getStub() != null) {
            return methodHeader.getStub().getSelectorString();
        }
        return ObjJMethodPsiUtils.getSelectorStringFromSelectorStrings(getSelectorStrings(methodHeader));
    }

    @NotNull
    public static PsiElement setName(@NotNull final ObjJSelector selectorElement, @NotNull final String newSelectorValue) {
        ObjJSelector newSelector = ObjJElementFactory.createSelector(selectorElement.getProject(), newSelectorValue);
        if (newSelector == null) {
            return selectorElement;
        }
        return selectorElement.replace(newSelector);
    }

    @NotNull
    public static String getName(ObjJMethodHeader methodHeader) {
        return getSelectorString(methodHeader);
    }

    @NotNull
    public static PsiElement setName(ObjJHasMethodSelector header, @NotNull String name)  throws IncorrectOperationException {
        ObjJHasMethodSelector copy = (ObjJHasMethodSelector)header.copy();
        String[]newSelectors = name.split(ObjJMethodPsiUtils.SELECTOR_SYMBOL);
        @SuppressWarnings("unchecked")
        List<ObjJSelector> selectorElements = copy.getSelectorList();
        if (newSelectors.length != selectorElements.size()) {
            throw new AssertionError("Selector lists invalid for rename");
        }
        for (int i=0;i<newSelectors.length;i++) {
            final String selectorString = newSelectors[i];
            final ObjJSelector selectorElement = selectorElements.get(i);
            if (selectorString.equals(selectorElement.getText())) {
                continue;
            }
            setName(selectorElement, selectorString);
        }
        return copy;
    }


    @NotNull
    public static PsiElement getNameIdentifier(@NotNull final ObjJSelector selector) {
        return selector;
    }

    @NotNull
    public static TextRange getRangeInElement(@NotNull final ObjJSelector selector) {
        //LOGGER.log(Level.INFO,"Getting selector range for full selector text of <"+selector.getText()+">");
        return selector.getTextRange();
    }

    @NotNull
    public static String getName(ObjJSelector selector) {
        return selector.getText();
    }

    @Nullable
    public static ObjJFormalVariableType getVarType(ObjJMethodDeclarationSelector selector) {
        return selector.getFormalVariableType();
    }

    @NotNull
    public static MethodScope getMethodScope(@NotNull ObjJMethodHeader methodHeader) {
        if (methodHeader.getStub() != null) {
            return methodHeader.getStub().isStatic() ? MethodScope.STATIC : MethodScope.INSTANCE;
        }
        return MethodScope.getScope(methodHeader.getMethodScopeMarker().getText());
    }

    @NotNull
    public static MethodScope getMethodScope(
            @SuppressWarnings("unused")
                    ObjJAccessorProperty accessorProperty) {
        return MethodScope.INSTANCE;
    }

    @NotNull
    public static MethodScope getMethodScope(
            @SuppressWarnings("unused") ObjJSelectorLiteral literal) {
        return MethodScope.INSTANCE;
    }

    public static boolean isStatic(ObjJHasMethodSelector hasMethodSelector) {
        if (hasMethodSelector instanceof ObjJMethodHeader) {
            ObjJMethodHeader methodHeader = (ObjJMethodHeader) hasMethodSelector;
            return methodHeader.getStub() != null ? methodHeader.getStub().isStatic() : getMethodScope((ObjJMethodHeader) hasMethodSelector) == MethodScope.STATIC;
        }
        return false;
    }


    public static boolean isClassMethod(@NotNull final ObjJMethodHeaderDeclaration methodHeader, @NotNull final List<String> possibleClasses) {
        if (possibleClasses.isEmpty() || possibleClasses.contains(ObjJClassType.UNDETERMINED)) {
            return true;
        }
        final String methodContainingClass = methodHeader.getContainingClassName();
        for (String className : possibleClasses) {
            if (possibleClasses.contains(methodContainingClass) || ObjJHasContainingClassPsiUtil.isSimilarClass(methodContainingClass, className)) {
                return true;
            }
        }
        return false;
    }


    public static boolean methodRequired(@NotNull ObjJMethodHeader methodHeader) {
        final ObjJProtocolScopedBlock scopedBlock = ObjJTreeUtil.getParentOfType(methodHeader, ObjJProtocolScopedBlock.class);
        return scopedBlock == null || scopedBlock.getAtOptional() == null;
    }

    @Nullable
    public static ObjJVariableName getHeaderVariableNameMatching(@Nullable ObjJMethodHeader methodHeader, @NotNull String variableName) {
        if (methodHeader == null) {
            return null;
        }
        for (ObjJMethodDeclarationSelector selector : methodHeader.getMethodDeclarationSelectorList()) {
            if (selector.getVariableName() != null && selector.getVariableName().getText().equals(variableName)) {
                return selector.getVariableName();
            }
        }
        return null;
    }



    /**
     * Method scope enum.
     * Flags method as either static or instance
     */
    public enum MethodScope {
        STATIC("+"),
        INSTANCE("-"),
        INVALID(null);
        private final String scopeMarker;
        MethodScope(String scopeMarker) {
            this.scopeMarker = scopeMarker;
        }

        public static MethodScope getScope(String scopeMarker) {
            if (Objects.equals(scopeMarker, STATIC.scopeMarker)) {
                return STATIC;
            } else if (Objects.equals(scopeMarker, INSTANCE.scopeMarker)) {
                return INSTANCE;
            } else {
                return INVALID;
            }
        }

    }

}
