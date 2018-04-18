package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJInstanceVariablesByClassIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJVariableNameByScopeIndex;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils.Filter;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjJVariableNameUtil {

    private static final Logger LOGGER = Logger.getLogger("ObjJVariableNameUtil");
    private static final List<ObjJVariableName> EMPTY_VARIABLE_NAME_LIST = ImmutableList.copyOf(new ObjJVariableName[0]);

    @NotNull
    public static List<ObjJVariableName> getMatchingPrecedingVariableNameElements(final ObjJCompositeElement variableName, int qualifiedIndex) {
        final int startOffset = variableName.getTextRange().getStartOffset();
        String variableNameQualifiedString;
        if (variableName instanceof ObjJVariableName) {
            variableNameQualifiedString = getQualifiedNameAsString((ObjJVariableName)variableName, qualifiedIndex);
        } else {
            //LOGGER.log(Level.WARNING, "Trying to match variable name element to a non variable name. Element is of type: "+variableName.getNode().toString()+"<"+variableName.getText()+">");
            variableNameQualifiedString = variableName.getText();
        }
        boolean hasContainingClass = ObjJHasContainingClassPsiUtil.getContainingClass(variableName) != null;
        return getAndFilterSiblingVariableNameElements(variableName, qualifiedIndex, (thisVariable) -> isMatchingElement(variableNameQualifiedString, thisVariable, hasContainingClass, startOffset, qualifiedIndex));
    }

    @NotNull
    public static List<ObjJVariableName> getMatchingPrecedingVariableAssignmentNameElements(final ObjJCompositeElement variableName, int qualifiedIndex) {
        final int startOffset = variableName.getTextRange().getStartOffset();
        String variableNameQualifiedString;
        if (variableName instanceof ObjJVariableName) {
            variableNameQualifiedString = getQualifiedNameAsString((ObjJVariableName)variableName, qualifiedIndex);
        } else {
            //LOGGER.log(Level.WARNING, "Trying to match variable name element to a non variable name. Element is of type: "+variableName.getNode().toString()+"<"+variableName.getText()+">");
            variableNameQualifiedString = variableName.getText();
        }
        boolean hasContainingClass = ObjJHasContainingClassPsiUtil.getContainingClass(variableName) != null;
        return getAndFilterSiblingVariableAssignmentNameElements(variableName, qualifiedIndex, (thisVariable) -> isMatchingElement(variableNameQualifiedString, thisVariable, hasContainingClass, startOffset, qualifiedIndex));
    }

    private static boolean isMatchingElement(@NotNull String variableNameQualifiedString, ObjJVariableName variableToCheck, boolean hasContainingClass, int startOffset, int qualifiedIndex) {
        if (variableToCheck == null) {
            LOGGER.log(Level.SEVERE, "Variable name to check should not be null");
            return false;
        }
        String thisVariablesFqName = getQualifiedNameAsString(variableToCheck, qualifiedIndex);
        //LOGGER.log(Level.INFO, "getMatchingPrecedingVariableNameElements: <"+variableNameQualifiedString+"> ?= <"+thisVariablesFqName+">");
        if (!variableNameQualifiedString.equals(thisVariablesFqName)) {
            return false;
        }
        LOGGER.log(Level.INFO, "Variable names match for variable: <"+variableNameQualifiedString+">; Is Offset <"+startOffset+" < "+variableToCheck.getTextRange().getStartOffset() + "? " + (variableToCheck.getTextRange().getStartOffset() < startOffset));
        if (variableToCheck.getContainingClass() == null) {
            if (hasContainingClass) {
                return true;
            }
        } else if (hasContainingClass) {
            //return false;
        }
        return variableToCheck.getTextRange().getStartOffset() < startOffset;
    }

    @NotNull
    public static String getQualifiedNameAsString(@NotNull ObjJVariableName variableName) {
        return getQualifiedNameAsString(variableName, -1);
    }

    @NotNull
    public static String getQualifiedNameAsString(@NotNull ObjJVariableName variableName, int stopBeforeIndex) {
        ObjJQualifiedReference qualifiedReference = ObjJTreeUtil.getParentOfType(variableName, ObjJQualifiedReference.class);
        return getQualifiedNameAsString(qualifiedReference, variableName.getText(), stopBeforeIndex);
    }

    public static String getQualifiedNameAsString(@Nullable ObjJQualifiedReference qualifiedReference, @Nullable String defaultValue) {
        return getQualifiedNameAsString(qualifiedReference, defaultValue, -1);
    }

    public static String getQualifiedNameAsString(@Nullable ObjJQualifiedReference qualifiedReference, @Nullable String defaultValue, int stopBeforeIndex) {
        if (qualifiedReference == null) {
            return defaultValue;
        }
        final List<ObjJVariableName> variableNames = qualifiedReference.getVariableNameList();
        if (variableNames.isEmpty()) {
            return defaultValue;
        }
        final int numVariableNames = stopBeforeIndex != -1 && variableNames.size() > stopBeforeIndex ? stopBeforeIndex : variableNames.size();
        final StringBuilder builder = new StringBuilder(variableNames.get(0).getText());
        for (int i=1;i<numVariableNames;i++) {
            builder.append(".").append(variableNames.get(i).getText());
        }
        //LOGGER.log(Level.INFO, "Qualified name is: <"+builder.toString()+"> for var in file: "+variableName.getContainingFile().getVirtualFile().getName()+"> at offset: <"+variableName.getTextRange().getStartOffset()+">");
        return builder.toString();
    }

    @NotNull
    public static List<ObjJVariableName> getPrecedingVariableAssignmentNameElements(final PsiElement variableName, int qualifiedIndex) {
        final int startOffset = variableName.getTextRange().getStartOffset();
        PsiFile file = variableName.getContainingFile();
        //LOGGER.log(Level.INFO, String.format("Qualified Index: <%d>; TextOffset: <%d>; TextRange: <%d,%d>", qualifiedIndex, variableName.getTextOffset(), variableName.getTextRange().getStartOffset(), variableName.getTextRange().getEndOffset()));
        return getAndFilterSiblingVariableAssignmentNameElements(variableName, qualifiedIndex, (var) -> var != variableName && (var.getContainingFile().isEquivalentTo(file) || var.getTextRange().getStartOffset() < startOffset));
    }

    @NotNull
    public static List<ObjJVariableName> getPrecedingVariableNameElements(final PsiElement variableName, int qualifiedIndex) {
        final int startOffset = variableName.getTextRange().getStartOffset();
        PsiFile file = variableName.getContainingFile();
        //LOGGER.log(Level.INFO, String.format("Qualified Index: <%d>; TextOffset: <%d>; TextRange: <%d,%d>", qualifiedIndex, variableName.getTextOffset(), variableName.getTextRange().getStartOffset(), variableName.getTextRange().getEndOffset()));
        return getAndFilterSiblingVariableNameElements(variableName, qualifiedIndex, (var) -> var != variableName && (var.getContainingFile().isEquivalentTo(file) || var.getTextRange().getStartOffset() < startOffset));
    }

    @NotNull
    public static List<ObjJVariableName> getAndFilterSiblingVariableAssignmentNameElements(PsiElement element, int qualifiedNameIndex, Filter<ObjJVariableName> filter) {
        List<ObjJVariableName> rawVariableNameElements = getSiblingVariableAssignmentNameElements(element, qualifiedNameIndex);
        List<ObjJVariableName> out = ArrayUtils.filter(rawVariableNameElements, filter);
        //LOGGER.log(Level.INFO, String.format("Get Siblings by var name before filter. BeforeFilter<%d>; AfterFilter:<%d>", rawVariableNameElements.size(), out.size()));
        return out;
    }

    @NotNull
    public static List<ObjJVariableName> getAndFilterSiblingVariableNameElements(PsiElement element, int qualifiedNameIndex, Filter<ObjJVariableName> filter) {
        List<ObjJVariableName> rawVariableNameElements = getSiblingVariableNameElements(element, qualifiedNameIndex);
        List<ObjJVariableName> out = ArrayUtils.filter(rawVariableNameElements, filter);
        //LOGGER.log(Level.INFO, String.format("Get Siblings by var name before filter. BeforeFilter<%d>; AfterFilter:<%d>", rawVariableNameElements.size(), out.size()));
        return out;
    }


    /**
     * Gets every preceding variable name element, even if it is not an assignment
     * @param element element to find siblings for
     * @param qualifiedNameIndex variable name index in chain, should be 0 for right now
     * @return list of variable name elements
     *
     * todo Allow checking of non 0 qualified name index
     */
    public static List<ObjJVariableName> getSiblingVariableNameElements(PsiElement element, int qualifiedNameIndex) {
        List<ObjJVariableName> result = new ArrayList<>(getAllVariableNamesInContainingBlocks(element, qualifiedNameIndex));
        int currentSize = result.size();
        //LOGGER.log(Level.INFO, "Num from blocks: <"+currentSize+">");
        if (qualifiedNameIndex <= 1) {
            result.addAll(getAllContainingClassInstanceVariables(element));
            //LOGGER.log(Level.INFO, "Num VariableNames after class vars: <"+result.size()+">");
            currentSize = result.size();
        }

        result.addAll(getAllAtGlobalFileVariables(element.getContainingFile()));
        result.addAll(getAllGlobalScopedFileVariables(element.getContainingFile()));
        result.addAll(getAllMethodDeclarationSelectorVars(element));
        result.addAll(getAllIterationVariables(ObjJTreeUtil.getParentOfType(element, ObjJIterationStatement.class)));
        result.addAll(getAllFileScopedVariables(element.getContainingFile(), qualifiedNameIndex));
        result.addAll(getAllFunctionScopeVariables(ObjJTreeUtil.getParentOfType(element, ObjJFunctionDeclarationElement.class)));
        result.addAll(getCatchProductionVariables(ObjJTreeUtil.getParentOfType(element, ObjJCatchProduction.class)));
        result.addAll(getPreprocessorDefineFunctionVariables(ObjJTreeUtil.getParentOfType(element, ObjJPreprocessorDefineFunction.class)));
        //LOGGER.log(Level.INFO, "Num VariableNames after getting file vars: <"+(result.size()-currentSize)+">");
        return result;
    }


    @NotNull
    public static List<ObjJVariableName> getSiblingVariableAssignmentNameElements(PsiElement element, int qualifiedNameIndex) {
        List<ObjJVariableName> result = getAllVariableNamesInAssignmentsInContainingBlocks(element, qualifiedNameIndex);
        int currentSize = result.size();
        //LOGGER.log(Level.INFO, "Num from blocks: <"+currentSize+">");
        if (qualifiedNameIndex <= 1) {
            result.addAll(getAllContainingClassInstanceVariables(element));
            //LOGGER.log(Level.INFO, "Num VariableNames after class vars: <"+result.size()+">");
            currentSize = result.size();
        }

        result.addAll(getAllAtGlobalFileVariables(element.getContainingFile()));
        result.addAll(getAllGlobalScopedFileVariables(element.getContainingFile()));
        result.addAll(getAllMethodDeclarationSelectorVars(element));
        result.addAll(getAllIterationVariables(ObjJTreeUtil.getParentOfType(element, ObjJIterationStatement.class)));
        result.addAll(getAllFileScopedVariables(element.getContainingFile(), qualifiedNameIndex));
        result.addAll(getAllFunctionScopeVariables(ObjJTreeUtil.getParentOfType(element, ObjJFunctionDeclarationElement.class)));
        result.addAll(getCatchProductionVariables(ObjJTreeUtil.getParentOfType(element, ObjJCatchProduction.class)));
        result.addAll(getPreprocessorDefineFunctionVariables(ObjJTreeUtil.getParentOfType(element, ObjJPreprocessorDefineFunction.class)));
        //LOGGER.log(Level.INFO, "Num VariableNames after getting file vars: <"+(result.size()-currentSize)+">");
        return result;
    }


    @Nullable
    public static ObjJVariableName getSiblingVariableAssignmentNameElement(PsiElement element, int qualifiedNameIndex, Filter<ObjJVariableName> filter) {
        ObjJVariableName variableName;
        variableName = getVariableNameDeclarationInContainingBlocks(element, qualifiedNameIndex, filter);
        if (variableName != null) {
            return !variableName.isEquivalentTo(element) ? variableName : null;
        }
        if (qualifiedNameIndex <= 1) {
            variableName = getFirstMatchOrNull(getAllMethodDeclarationSelectorVars(element), filter);
            if (variableName != null) {
                return !variableName.isEquivalentTo(element) ? variableName : null;
            }
            variableName = getFirstMatchOrNull(getAllContainingClassInstanceVariables(element), filter);
            if (variableName != null) {
                return !variableName.isEquivalentTo(element) ? variableName : null;
            }
            variableName = getFirstMatchOrNull(getAllIterationVariables(ObjJTreeUtil.getParentOfType(element, ObjJIterationStatement.class)), filter);
            if (variableName != null) {
                return !variableName.isEquivalentTo(element) ? variableName : null;
            }
            variableName = getFirstMatchOrNull(getAllFunctionScopeVariables(ObjJTreeUtil.getParentOfType(element, ObjJFunctionDeclarationElement.class)), filter);
            if (variableName != null) {
                return !variableName.isEquivalentTo(element) ? variableName : null;
            }
            variableName = getFirstMatchOrNull(getAllGlobalScopedFileVariables(element.getContainingFile()), filter);
            if (variableName != null) {
                return variableName;
            }
            variableName = getFirstMatchOrNull(getAllAtGlobalFileVariables(element.getContainingFile()), filter);
            if (variableName != null) {
                return variableName;
            }
        }

        variableName = getFirstMatchOrNull(getAllFileScopedVariables(element.getContainingFile(), qualifiedNameIndex), filter);
        if (variableName != null) {
            return !variableName.isEquivalentTo(element) ? variableName : null;
        }
        variableName = getFirstMatchOrNull(getCatchProductionVariables(ObjJTreeUtil.getParentOfType(element, ObjJCatchProduction.class)), filter);
        if (variableName != null) {
            return !variableName.isEquivalentTo(element) ? variableName : null;
        }
        variableName = getFirstMatchOrNull(getPreprocessorDefineFunctionVariables(ObjJTreeUtil.getParentOfType(element, ObjJPreprocessorDefineFunction.class)), filter);
        if (variableName != null) {
            return !variableName.isEquivalentTo(element) ? variableName : null;
        }
        if (DumbService.isDumb(element.getProject())) {
            throw new IndexNotReadyRuntimeException();
        }
        List<ObjJGlobalVariableDeclaration> globalVariableDeclarations = ObjJGlobalVariableNamesIndex.getInstance().get(element.getText(), element.getProject());
        if (!globalVariableDeclarations.isEmpty()) {
            return globalVariableDeclarations.get(0).getVariableName();
        }
        ObjJBlock block = ObjJTreeUtil.getParentOfType(element, ObjJBlock.class);
        if (block != null) {
            return getSiblingVariableAssignmentNameElement(block, qualifiedNameIndex, filter);
        }
        return null;
    }
    @Nullable
    private static ObjJVariableName getVariableNameDeclarationInContainingBlocks(PsiElement element, int qualifiedNameIndex, Filter<ObjJVariableName> filter) {
        ObjJBlock block = ObjJTreeUtil.getTopmostParentOfType(element, ObjJBlock.class);
        if (block == null) {
            return null;
        }
        final String varName = element.getText();
        List<ObjJVariableName> variableNames = ObjJVariableNameByScopeIndex.getInstance().getInRange(ObjJFileUtil.getContainingFileName(element.getContainingFile()), block.getTextRange(), element.getProject());
        return getFirstMatchOrNull(variableNames, (variableName) -> {
            if (!variableName.getText().equals(varName)) {
                return false;
            }
            PsiElement parent = variableName.getParent();
            if (!(parent instanceof ObjJQualifiedReference)) {
                return false;
            }
            parent = parent.getParent();
            return parent instanceof ObjJBodyVariableAssignment || parent instanceof ObjJVariableDeclaration;
        });
    }

    /*
    @Nullable
    private static ObjJVariableName getVariableNameDeclarationInContainingBlocks(PsiElement element, int qualifiedNameIndex, Filter<ObjJVariableName> filter) {
        ObjJBlock block = element instanceof ObjJBlock ? ((ObjJBlock)element) : PsiTreeUtil.getParentOfType(element, ObjJBlock.class);
        List<ObjJBodyVariableAssignment> bodyVariableAssignments = ObjJBlockPsiUtil.getBlockChildrenOfType(block, ObjJBodyVariableAssignment.class, true);
        bodyVariableAssignments.addAll(ObjJBlockPsiUtil.getParentBlockChildrenOfType(block, ObjJBodyVariableAssignment.class, true));
        ObjJVariableName out;
        for (ObjJBodyVariableAssignment bodyVariableAssignment : bodyVariableAssignments) {
            ProgressIndicatorProvider.checkCanceled();
            out = getVariableFromBodyVariableAssignment(bodyVariableAssignment, qualifiedNameIndex, filter);
            if (out != null && !out.isEquivalentTo(element)) {
                return out;
            }
        }
        return null;
    }
    */

    public static ObjJVariableName getFirstMatchOrNull(List<ObjJVariableName> variableNameElements, Filter<ObjJVariableName> filter) {
        for (ObjJVariableName variableName : variableNameElements) {
            ProgressIndicatorProvider.checkCanceled();
            if (filter.check(variableName)) {
                return variableName;
            }
        }
        return null;
    }

    private static List<ObjJVariableName> getAllVariableNamesInAssignmentsInContainingBlocks(PsiElement element, int qualifiedNameIndex) {
        List<ObjJVariableName> result = new ArrayList<>();
        ObjJBlock block = element instanceof ObjJBlock ? ((ObjJBlock)element) : PsiTreeUtil.getParentOfType(element, ObjJBlock.class);
        List<ObjJBodyVariableAssignment> bodyVariableAssignments = ObjJBlockPsiUtil.getBlockChildrenOfType(block, ObjJBodyVariableAssignment.class, true);
        bodyVariableAssignments.addAll(ObjJBlockPsiUtil.getParentBlockChildrenOfType(block, ObjJBodyVariableAssignment.class, true));
        for (ObjJBodyVariableAssignment bodyVariableAssignment : bodyVariableAssignments) {
            ProgressIndicatorProvider.checkCanceled();
            result.addAll(getAllVariablesFromBodyVariableAssignment(bodyVariableAssignment, qualifiedNameIndex));
        }
        return result;
    }

    private static List<ObjJVariableName> getAllVariableNamesInContainingBlocks(PsiElement element, int qualifiedNameIndex) {

        ObjJBlock containingBlock = ObjJTreeUtil.getParentOfType(element,ObjJBlock.class);
        ObjJBlock tempBlock = containingBlock;
        while (tempBlock != null) {
            tempBlock = containingBlock.getParentOfType(ObjJBlock.class);
            if (tempBlock == null) {
                break;
            }
            containingBlock = tempBlock;
        }
        if (containingBlock == null) {
            LOGGER.log(Level.INFO, "Variable <"+element.getText()+">  is not in block");
            return EMPTY_VARIABLE_NAME_LIST;
        }
        final PsiFile containingFile = element.getContainingFile();
        final String fileName = ObjJFileUtil.getContainingFileName(containingFile);
        assert fileName != null;
        LOGGER.log(Level.INFO, "Variable <"+element.getText()+"> is in block in file: <"+fileName+"> at offset: "+containingBlock.getTextRange().getStartOffset());
        return ObjJVariableNameByScopeIndex.getInstance().getInRange(fileName, containingBlock.getTextRange(), element.getProject());
    }

    private static List<ObjJVariableName> getAllContainingClassInstanceVariables(PsiElement element) {
        List<ObjJVariableName> result = new ArrayList<>();
        if (DumbService.getInstance(element.getProject()).isDumb()) {
            LOGGER.log(Level.INFO, "Cannot get instance variable as project is in dumb mode");
            return EMPTY_VARIABLE_NAME_LIST;
        }
        final String containingClassName = element instanceof ObjJHasContainingClass ? ((ObjJHasContainingClass)element).getContainingClassName() : null;
        if (containingClassName == null || ObjJMethodCallPsiUtil.isUniversalMethodCaller(containingClassName)) {
            return EMPTY_VARIABLE_NAME_LIST;
        }
        for(String variableHoldingClassName : ObjJInheritanceUtil.getAllInheritedClasses(containingClassName, element.getProject())) {
            ProgressIndicatorProvider.checkCanceled();
            for (ObjJInstanceVariableDeclaration declaration : ObjJInstanceVariablesByClassIndex.getInstance().get(variableHoldingClassName, element.getProject())) {
                ProgressIndicatorProvider.checkCanceled();
                if (declaration.getVariableName() != null) {
                    result.add(declaration.getVariableName());
                }
            }
        }
        return result;
    }

    private static List<ObjJVariableName> getAllMethodDeclarationSelectorVars(PsiElement element) {
        List<ObjJVariableName> result = new ArrayList<>();
        ObjJMethodDeclaration declaration = ObjJTreeUtil.getParentOfType(element, ObjJMethodDeclaration.class);
        if (declaration != null) {
            for (ObjJMethodDeclarationSelector methodDeclarationSelector : declaration.getMethodHeader().getMethodDeclarationSelectorList()) {
                ProgressIndicatorProvider.checkCanceled();
                if (methodDeclarationSelector.getVariableName() == null || methodDeclarationSelector.getVariableName().getText().isEmpty()) {
                    //LOGGER.log(Level.INFO, "Selector variable name is null");
                    continue;
                }
                //LOGGER.log(Level.INFO, "Adding method header selector: "+methodDeclarationSelector.getVariableName().getText());
                result.add(methodDeclarationSelector.getVariableName());
            }
        } else {
            //LOGGER.log(Level.INFO, "Psi element is not within a variable declaration");
        }
        return result;
    }

    public static int getIndexInQualifiedNameParent(@Nullable
                                                             ObjJVariableName variableName) {
        if (variableName == null) {
            return 0;
        }
        ObjJQualifiedReference qualifiedReferenceParent = ObjJTreeUtil.getParentOfType(variableName, ObjJQualifiedReference.class);
        int qualifiedNameIndex = qualifiedReferenceParent != null ? qualifiedReferenceParent.getVariableNameList().indexOf(variableName) : -1;
        if (qualifiedNameIndex < 0) {
            qualifiedNameIndex = 0;
        }
        if (qualifiedNameIndex > 1) {
            ObjJVariableName firstVariable = qualifiedReferenceParent.getPrimaryVar();
            if (firstVariable != null && (firstVariable.getText().equals("self") || firstVariable.getText().equals("super"))) {
                qualifiedNameIndex -= 1;
            }
        }
        return qualifiedNameIndex;
    }

    public static List<ObjJVariableName>  getAllFileScopedVariables(@Nullable PsiFile file, int qualifiedNameIndex) {
        if (file == null) {
            LOGGER.log(Level.INFO, "Cannot get all file scoped variables. File is null");
            return EMPTY_VARIABLE_NAME_LIST;
        }
        List<ObjJVariableName> result = new ArrayList<>();
        List<ObjJBodyVariableAssignment> bodyVariableAssignments = ObjJTreeUtil.getChildrenOfTypeAsList(file, ObjJBodyVariableAssignment.class);
        result.addAll(getAllVariablesFromBodyVariableAssignmentsList(bodyVariableAssignments, qualifiedNameIndex));
        result.addAll(getAllFileScopeGlobalVariables(file));
        result.addAll(getAllPreProcDefinedVariables(file));
        return result;
    }

    private static List<ObjJVariableName> getAllPreProcDefinedVariables(PsiFile file) {
        List<ObjJPreprocessorDefineFunction> definedFunctions;
        if (file instanceof ObjJFile) {
            ObjJFile objJFile = (ObjJFile) file;
            definedFunctions = objJFile.getChildrenOfType(ObjJPreprocessorDefineFunction.class);
        } else {
            definedFunctions = ObjJTreeUtil.getChildrenOfTypeAsList(file, ObjJPreprocessorDefineFunction.class);
        }
        List<ObjJVariableName> out = new ArrayList<>();
        for (ObjJPreprocessorDefineFunction function : definedFunctions) {
            if (function.getVariableName() != null) {
                out.add(function.getVariableName());
            }
        }
        return out;
    }

    private static List<ObjJVariableName> getAllFileScopeGlobalVariables(
            @Nullable
                    PsiFile file) {
        if (file == null) {
            return EMPTY_VARIABLE_NAME_LIST;
        }
        List<ObjJVariableName> result = new ArrayList<>();
        List<ObjJExpr> expressions = ObjJTreeUtil.getChildrenOfTypeAsList(file, ObjJExpr.class);
        for (ObjJExpr expr : expressions) {
            ProgressIndicatorProvider.checkCanceled();
            if (expr == null || expr.getLeftExpr() == null || expr.getLeftExpr().getVariableDeclaration() == null) {
                continue;
            }
            ObjJVariableDeclaration declaration = expr.getLeftExpr().getVariableDeclaration();
            for (ObjJQualifiedReference qualifiedReference : declaration.getQualifiedReferenceList()) {
                if (qualifiedReference.getPrimaryVar() != null) {
                    result.add(qualifiedReference.getPrimaryVar());
                }
            }
        }
        return result;
    }

    private static List<ObjJVariableName> getAllGlobalScopedFileVariables(
            @Nullable
                    PsiFile file) {
        if (file == null) {
            return EMPTY_VARIABLE_NAME_LIST;
        }
        List<ObjJVariableName> result = new ArrayList<>();
        for (ObjJGlobalVariableDeclaration variableDeclaration : ObjJTreeUtil.getChildrenOfTypeAsList(file, ObjJGlobalVariableDeclaration.class)) {
            if (variableDeclaration.getVariableName() != null) {
                result.add(variableDeclaration.getVariableName());
            }
        }
        return result;
    }


    private static List<ObjJVariableName> getAllAtGlobalFileVariables(
            @Nullable
                    PsiFile file) {
        if (file == null) {
            return EMPTY_VARIABLE_NAME_LIST;
        }
        List<ObjJVariableName> result = new ArrayList<>();
        for (ObjJGlobal variableDeclaration : ObjJTreeUtil.getChildrenOfTypeAsList(file, ObjJGlobal.class)) {
            result.add(variableDeclaration.getVariableName());
        }
        return result;
    }

    private static List<ObjJVariableName> getAllVariablesFromBodyVariableAssignmentsList(@NotNull List<ObjJBodyVariableAssignment> bodyVariableAssignments, int qualifiedNameIndex) {
        if (bodyVariableAssignments.isEmpty()) {
            return EMPTY_VARIABLE_NAME_LIST;
        }
        List<ObjJVariableName> result = new ArrayList<>();
        for(ObjJBodyVariableAssignment bodyVariableAssignment : bodyVariableAssignments) {
            ProgressIndicatorProvider.checkCanceled();
            //LOGGER.log(Level.INFO, "Body variable assignment: <"+bodyVariableAssignment.getText()+">");
            result.addAll(getAllVariablesFromBodyVariableAssignment(bodyVariableAssignment, qualifiedNameIndex));
        }
        return result;

    }

    private static List<ObjJVariableName> getAllVariablesFromBodyVariableAssignment(@Nullable ObjJBodyVariableAssignment bodyVariableAssignment, int qualifiedNameIndex) {
        if (bodyVariableAssignment == null) {
            return EMPTY_VARIABLE_NAME_LIST;
        }
        List<ObjJVariableName> result = new ArrayList<>();
        List<ObjJQualifiedReference> references = bodyVariableAssignment.getQualifiedReferenceList();
        for (ObjJVariableDeclaration variableDeclaration : bodyVariableAssignment.getVariableDeclarationList()) {
            //LOGGER.log(Level.INFO,"VariableDec: <"+variableDeclaration.getText()+">");
            references.addAll(variableDeclaration.getQualifiedReferenceList());
        }
        for (ObjJQualifiedReference qualifiedReference : references) {
            ProgressIndicatorProvider.checkCanceled();
            //LOGGER.log(Level.INFO, "Checking variable dec for qualified reference: <"+qualifiedReference.getText()+">");
            if (qualifiedNameIndex == -1) {
                result.addAll(qualifiedReference.getVariableNameList());
            } else if (qualifiedReference.getVariableNameList().size() > qualifiedNameIndex) {
                ObjJVariableName suggestion = qualifiedReference.getVariableNameList().get(qualifiedNameIndex);
                result.add(suggestion);
            } else {
                //LOGGER.log(Level.INFO, "Not adding variable <"+qualifiedReference.getText()+"> as Index is out of bounds.");
            }
        }
        return result;
    }

    private static ObjJVariableName getVariableFromBodyVariableAssignment(@Nullable ObjJBodyVariableAssignment bodyVariableAssignment, int qualifiedNameIndex, Filter<ObjJVariableName> filter) {
        if (bodyVariableAssignment == null) {
            return null;
        }
        List<ObjJQualifiedReference> references = bodyVariableAssignment.getQualifiedReferenceList();
        for (ObjJVariableDeclaration variableDeclaration : bodyVariableAssignment.getVariableDeclarationList()) {
            //LOGGER.log(Level.INFO,"VariableDec: <"+variableDeclaration.getText()+">");
            references.addAll(variableDeclaration.getQualifiedReferenceList());
        }
        for (ObjJQualifiedReference qualifiedReference : references) {
            ProgressIndicatorProvider.checkCanceled();
            //LOGGER.log(Level.INFO, "Checking variable dec for qualified reference: <"+qualifiedReference.getText()+">");
            if (qualifiedNameIndex == -1) {
                for (ObjJVariableName temp : qualifiedReference.getVariableNameList()) {
                    if (filter.check(temp)) {
                        return temp;
                    }
                }
            } else if (qualifiedReference.getVariableNameList().size() > qualifiedNameIndex) {
                ObjJVariableName suggestion = qualifiedReference.getVariableNameList().get(qualifiedNameIndex);
                if (filter.check(suggestion)) {
                    return suggestion;
                }
            } else {
                //LOGGER.log(Level.INFO, "Not adding variable <"+qualifiedReference.getText()+"> as Index is out of bounds.");
            }
        }
        return null;
    }

    private static  List<ObjJVariableName> getAllFunctionScopeVariables(
            @Nullable
                    ObjJFunctionDeclarationElement functionDeclarationElement) {
        if (functionDeclarationElement == null || functionDeclarationElement.getFormalParameterArgList().isEmpty()) {
            return EMPTY_VARIABLE_NAME_LIST;
        }
        List<ObjJVariableName> result = new ArrayList<>();
        for (Object parameterArg : functionDeclarationElement.getFormalParameterArgList()) {
            result.add(((ObjJFormalParameterArg)parameterArg).getVariableName());
        }
        return result;
    }

    @NotNull
    public static List<ObjJVariableName> getAllParentBlockVariables(@Nullable PsiElement element, int qualifiedIndex) {
        List<ObjJVariableName> result = new ArrayList<>();
        if (element == null) {
            return result;
        }
        for (ObjJBodyVariableAssignment declaration : ObjJBlockPsiUtil.getParentBlockChildrenOfType(element, ObjJBodyVariableAssignment.class, true)) {
            ProgressIndicatorProvider.checkCanceled();
            //LOGGER.log(Level.INFO, "Adding all iteration statement variables for dec: <"+declaration.getText()+">");
            result.addAll(getAllVariablesFromBodyVariableAssignment(declaration, qualifiedIndex));
        }
        return result;
    }

    @NotNull
    private static List<ObjJVariableName> getAllIterationVariables(
            @Nullable
                    ObjJIterationStatement iterationStatement) {
        List<ObjJVariableName> result = new ArrayList<>();
        while (iterationStatement != null) {
            ProgressIndicatorProvider.checkCanceled();
            //Get variable if in an `in` statement
            //i.e.  `for (var v in ob)`
            if (iterationStatement.getInExpr() != null) {
                result.add(iterationStatement.getInExpr().getVariableName());
            }

            // get regular variable declarations in iteration statement
            for (ObjJVariableDeclaration declaration : iterationStatement.getVariableDeclarationList()) {
                ProgressIndicatorProvider.checkCanceled();
                //LOGGER.log(Level.INFO, "Adding all iteration statement variables for dec: <"+declaration.getText()+">");
                for (ObjJQualifiedReference qualifiedReference : declaration.getQualifiedReferenceList()) {
                    result.add(qualifiedReference.getPrimaryVar());
                }
            }
            for (ObjJQualifiedReference reference : iterationStatement.getQualifiedReferenceList()) {
                result.add(reference.getPrimaryVar());
            }
            iterationStatement = ObjJTreeUtil.getParentOfType(iterationStatement, ObjJIterationStatement.class);
        }
        return result;
    }

    private static List<ObjJVariableName> getCatchProductionVariables(
            @Nullable
                    ObjJCatchProduction catchProduction) {
        if (catchProduction == null) {
            return EMPTY_VARIABLE_NAME_LIST;
        }
        return Collections.singletonList(catchProduction.getVariableName());
    }

    private static List<ObjJVariableName> getPreprocessorDefineFunctionVariables(
            @Nullable
                    ObjJPreprocessorDefineFunction function) {
        if (function == null || function.getFormalParameterArgList().isEmpty()) {
            return EMPTY_VARIABLE_NAME_LIST;
        }
        List<ObjJVariableName> result = new ArrayList<>();
        for (ObjJFormalParameterArg formalParameterArg : function.getFormalParameterArgList()) {
            result.add(formalParameterArg.getVariableName());
        }
        return result;
    }

    public static boolean isInstanceVarDeclaredInClassOrInheritance(@NotNull final ObjJVariableName variableName) {
        return getFirstMatchOrNull(getAllContainingClassInstanceVariables(variableName), (var) -> var.getText().equals(variableName.getText())) != null;
    }
}
