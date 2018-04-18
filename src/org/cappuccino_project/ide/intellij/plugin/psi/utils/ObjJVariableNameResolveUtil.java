package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjJVariableNameResolveUtil {

    private static final Logger LOGGER = Logger.getLogger(ObjJVariableNameResolveUtil.class.getName());

    @Nullable
    public static PsiElement getVariableDeclarationElement(@NotNull ObjJVariableName variableNameElement, @SuppressWarnings("unused")
            boolean mustBeLast) {
        final String variableNameString = variableNameElement.getText();
        if (variableNameElement.getParent() instanceof ObjJPropertyAssignment) {
            return null;
        }
        if (ObjJTreeUtil.getParentOfType(variableNameElement, ObjJMethodHeader.class) != null) {
            return null;
        }

        if (variableNameString.equals("class")) {
            return null;
        }

        if (variableNameString.equals("this")) {
            return null;
        }

        ObjJClassName className = getClassNameIfVariableNameIsStaticReference(variableNameElement);
        if (className != null) {
            return className;
        }

        return ObjJVariableNameUtil.getSiblingVariableAssignmentNameElement(variableNameElement, 0, (var) -> isPrecedingVar(variableNameElement, var));
    }


    private static ObjJClassName getClassNameIfVariableNameIsStaticReference(ObjJVariableName variableNameElement) {
        ObjJClassName classNameElement = null;
        String className = variableNameElement.getText();
        ObjJClassDeclarationElement containingClass = ObjJPsiImplUtil.getContainingClass(variableNameElement);
        if (className.equals("self")) {
            //LOGGER.log(Level.INFO, "Var name matches 'self'.");
            if (containingClass != null) {
                LOGGER.log(Level.INFO, "Var name 'self' resolves to <" + variableNameElement.getText() + ">");
                return containingClass.getClassName();
            }
        }
        if (variableNameElement.getText().equals("super")) {
            classNameElement = ObjJClassDeclarationPsiUtil.getContainingSuperClass(variableNameElement, true);
            if (classNameElement == null && containingClass != null) {
                classNameElement = containingClass.getClassName();
            }
        }
        if (classNameElement != null) {
            className = classNameElement.getText();
        }

        /*
            Tries to find the most relevant class reference,
            if variable name element is part of a method call
         */
        if (!DumbService.isDumb(variableNameElement.getProject())) {
            List<ObjJClassDeclarationElement> classDeclarationElements = ObjJClassDeclarationsIndex.getInstance().get(className, variableNameElement.getProject());
            if (!classDeclarationElements.isEmpty()) {
                ObjJMethodCall methodCall = variableNameElement.getParentOfType(ObjJMethodCall.class);
                final String methodCallSelectorString = methodCall != null ?  methodCall.getSelectorString() : null;
                for (ObjJClassDeclarationElement classDeclarationElement : classDeclarationElements) {
                    if (methodCallSelectorString != null) {
                        if (classDeclarationElement.hasMethod(methodCallSelectorString)) {
                            return classDeclarationElement.getClassName();
                        }
                    } else if (classDeclarationElement instanceof ObjJImplementationDeclaration) {
                        if (!((ObjJImplementationDeclaration)classDeclarationElement).isCategory()) {
                            return classDeclarationElement.getClassName();
                        }
                    } else {
                        return classDeclarationElement.getClassName();
                    }
                }
            }
        }
        return null;
    }


    private static boolean isPrecedingVar(@NotNull ObjJVariableName baseVar, @NotNull ObjJVariableName possibleFirstDeclaration) {
        LOGGER.log(Level.INFO, "BaseVar: "+baseVar.getText() + "@"+baseVar.getTextRange() +" VS. OtherVar: "+possibleFirstDeclaration.getText() + "@"+possibleFirstDeclaration.getTextRange().getStartOffset());
        return baseVar.getText().equals(possibleFirstDeclaration.getText()) && (baseVar.getContainingFile() != possibleFirstDeclaration.getContainingFile() || baseVar.getTextRange().getStartOffset() > possibleFirstDeclaration.getTextRange().getStartOffset());
    }


    /*
    @Nullable
    public static PsiElement getVariableDeclarationElement(@NotNull ObjJVariableName variableNameElement, boolean mustBeLast) {
        final String variableNameString = variableNameElement.getText();
        if (variableNameElement.getParent() instanceof ObjJPropertyAssignment) {
            return variableNameElement;
        }
        if (ObjJTreeUtil.getParentOfType(variableNameElement, ObjJMethodHeader.class) != null) {
            return variableNameElement;
        }

        if (variableNameString.equals("class")) {
            return variableNameElement;
        }

        if (variableNameString.equals("self")) {
            LOGGER.log(Level.INFO, "Var name matches 'self'.");
            ObjJClassDeclarationElement containingClass = ObjJPsiImplUtil.getContainingClass(variableNameElement);
            if (containingClass != null) {
                LOGGER.log(Level.INFO, "Var name 'self' resolves to <"+variableNameString+">");
                return containingClass.getClassName();
            }
        }
        if (variableNameString.equals("super")) {
            return ObjJClassDeclarationPsiUtil.getContainingSuperClass(variableNameElement, true);
        }
        if (!DumbService.isDumb(variableNameElement.getProject())) {
            LOGGER.log(Level.INFO, "Service is not dumb, getting class dec elements for var: <"+variableNameString+">");
            List<ObjJClassDeclarationElement> classDeclarationElements = ObjJClassDeclarationsIndex.getInstance().get(variableNameString, variableNameElement.getProject());
            if (!classDeclarationElements.isEmpty()) {
                LOGGER.log(Level.INFO, "Found instance variable matching var name: <"+variableNameString+">");
                return classDeclarationElements.get(0);
            }
            LOGGER.log(Level.INFO, "Failed to find instance var matching var name: <"+variableNameString+">");
        }
        LOGGER.log(Level.INFO, "Finding sibling var names matching: <"+variableNameString+">");
        ObjJVariableName referencedVariableName = getMatchingSiblingFromBodyVariableAssignments(variableNameElement, mustBeLast);
        if (referencedVariableName != null) {
            LOGGER.log(Level.INFO, "Found sibling var names matching: <"+variableNameString+">");
            return referencedVariableName;
        }

        LOGGER.log(Level.INFO, "Finding method header var names matching: <"+variableNameString+">");
        ObjJMethodDeclaration methodDeclaration = ObjJTreeUtil.getParentOfType(variableNameElement, ObjJMethodDeclaration.class);
        if (methodDeclaration != null) {
            ObjJVariableName headerVariableName = ObjJMethodPsiUtils.getHeaderVariableNameMatching(methodDeclaration, variableNameString);
            if (headerVariableName != null) {
                return headerVariableName;
            }
        }

        LOGGER.log(Level.INFO, "Finding function var name matching: <"+variableNameString+">");
        ObjJFormalParameterArg functionParameterArg = ObjJTreeUtil.getParentOfType(variableNameElement, ObjJFormalParameterArg.class);
        if (functionParameterArg != null) {
            LOGGER.log(Level.INFO, "Variable <"+variableNameString+"> references self in function header");
            return variableNameElement;
        }
        LOGGER.log(Level.INFO, "Finding containing class of var name matching: <"+variableNameString+">");
        String containingClassName = variableNameElement.getContainingClassName();
        if (!ObjJMethodCallPsiUtil.isUniversalMethodCaller(containingClassName)) {
            LOGGER.log(Level.INFO, "Found containing class of var name matching: <" + variableNameString + ">");
            LOGGER.log(Level.INFO, "Finding instance var matching: <" + variableNameString + ">");
            referencedVariableName = getInstanceVarDeclarationAcrossInheritance(containingClassName, variableNameElement.getProject(), variableNameString);
            if (referencedVariableName != null) {
                LOGGER.log(Level.INFO, "Found instance var matching: <" + variableNameString + ">");
                return referencedVariableName;
            }
        }

        List<ObjJVariableName> temp = new ArrayList<>();

        ObjJVariableNameCompletionContributorUtil.addAllFunctionScopedVariables(temp, ObjJTreeUtil.getParentOfType(variableNameElement, ObjJFunctionDeclarationElement.class));
        ObjJVariableNameCompletionContributorUtil.addAllIterationVariables(temp, ObjJTreeUtil.getParentOfType(variableNameElement, ObjJIterationStatement.class));
        ObjJVariableNameCompletionContributorUtil.addCatchProductionVariables(temp, ObjJTreeUtil.getParentOfType(variableNameElement, ObjJCatchProduction.class));
        ObjJVariableNameCompletionContributorUtil.addPreprocessorDefineFunctionVariables(temp, ObjJTreeUtil.getParentOfType(variableNameElement, ObjJPreprocessorDefineFunction.class));
        PsiFile file = variableNameElement.getContainingFile();
        return file != null ? getVariableDeclarationFromFile(file, variableNameElement, mustBeLast) : null;
    }

    @Nullable
    public static ObjJVariableName getMatchingSiblingFromBodyVariableAssignments(ObjJVariableName variableNameElement, boolean mustBeLast) {
        LOGGER.log(Level.INFO, "Getting matching variable assignments from body variable assignments for var name <"+variableNameElement.getText()+">.");
        ObjJBlock block = ObjJTreeUtil.getParentOfType(variableNameElement, ObjJBlock.class);
        ObjJVariableName referencedVariableName;
        List<ObjJBodyVariableAssignment> bodyVariableAssignments = ObjJBlockPsiUtil.getBlockChildrenOfType(block, ObjJBodyVariableAssignment.class, true);
        for (ObjJBodyVariableAssignment assignment : bodyVariableAssignments) {
            LOGGER.log(Level.INFO, "Checking body assignments: <"+assignment.getText()+">");
            referencedVariableName = getVariableDeclaration(assignment, variableNameElement, mustBeLast);
            if (referencedVariableName != null) {
                LOGGER.log(Level.INFO, "Body variable assignment matches: <"+referencedVariableName.getText()+">");
                return referencedVariableName;
            }
        }
        return null;
    }

    @Nullable
    public static ObjJVariableName getInstanceVarDeclarationAcrossInheritance(@NotNull String containingClassName, @NotNull Project project, @NotNull String variableName) {
        if (DumbServiceImpl.isDumb(project)) {
            return null;
        }
        List<ObjJInstanceVariableDeclaration> classDeclarationElements = new ArrayList<>(ObjJInstanceVariablesByNameIndex.getInstance().get(variableName, project));
        ObjJVariableName referencedVariable = getInstanceVariableInClass(classDeclarationElements, containingClassName);
        if (referencedVariable != null) {
            return referencedVariable;
        }
        List<String> inheritedClassNames =  ObjJPsiImplUtil.getAllInheritedClasses(containingClassName, project);

        //Class was checked before loop, no need to check again
        inheritedClassNames.remove(containingClassName);

        //Check instance vars matching class
        for (String className : inheritedClassNames) {
            if (className.equals(containingClassName)) {
                continue;
            }
            referencedVariable = getInstanceVariableInClass(classDeclarationElements, className);
            if (referencedVariable != null) {
                return referencedVariable;
            }
        }
        return null;
    }

    @Nullable
    public static ObjJVariableName getInstanceVariableInClass(@NotNull List<ObjJInstanceVariableDeclaration> declarations, @NotNull String containingClass) {
        if (ObjJMethodCallPsiUtil.isUniversalMethodCaller(containingClass)) {
            return null;
        }
        for (ObjJInstanceVariableDeclaration instanceVariableDeclaration : declarations) {
            if (    instanceVariableDeclaration.getVariableName() != null &&
                    instanceVariableDeclaration.getContainingClassName().equals(containingClass)
            ) {
                return instanceVariableDeclaration.getVariableName();
            }
        }
        return null;
    }


    public static boolean isInstanceVarDeclaredInClassOrInheritance(@NotNull String containingClassName, @NotNull Project project, @NotNull String variableName) {
        return getInstanceVarDeclarationAcrossInheritance(containingClassName, project, variableName) != null;
    }

    @Nullable
    public static ObjJVariableName getVariableWithNameFromBodyVariableAssignment(ObjJBodyVariableAssignment variableAssignment, ObjJVariableName variableName, boolean mustBeLast) {
        if (variableAssignment == null) {
            return null;
        }
        for (ObjJVariableDeclaration variableDeclaration : variableAssignment.getVariableDeclarationList()) {
            ObjJVariableName referencedVar = getVariableWithNameFromVariableDeclaration(variableDeclaration, variableName, mustBeLast);
            if (referencedVar != null) {
                return referencedVar;
            }
        }
        return null;
    }

    @Nullable
    private static ObjJVariableName getVariableWithNameFromVariableDeclaration(ObjJVariableDeclaration variableDeclaration, @NotNull ObjJVariableName variableName, boolean mustBeLast) {
        if (variableDeclaration == null) {
            return null;
        }
        ObjJVariableName reference = getVariableWithNameFromQualifiedReferenceList(variableDeclaration.getQualifiedReferenceList(), variableName, mustBeLast);
        if (reference != null) {
            return reference;
        }
        return null;
    }

    @Nullable
    private static ObjJVariableName getVariableWithNameFromQualifiedReferenceList(@NotNull List<ObjJQualifiedReference> references, ObjJVariableName variableName, boolean mustBeLast) {
        for (ObjJQualifiedReference reference : references) {
            ObjJVariableName variableNameElement = getVariableWithNameFromQualifiedReference(reference, variableName, mustBeLast);
            if (variableNameElement != null) {
                return variableNameElement;
            }
        }
        return null;
    }

    @Nullable
    private static ObjJVariableName getVariableWithNameFromQualifiedReference(@NotNull ObjJQualifiedReference qualifiedReference, @Nullable ObjJVariableName variableName, boolean mustBeLast) {
        if (variableName == null) {
            return null;
        }
        List<ObjJVariableName> variableNames = qualifiedReference.getVariableNameList();
        if (variableNames.size() < 1) {
            return null;
        }
        if (mustBeLast) {
            ObjJVariableName lastVariableName = variableNames.get(variableNames.size()-1);
            return lastVariableName.getText().equals(variableName.getText()) ? lastVariableName : null;
        }
        for (ObjJVariableName variableNameElement : qualifiedReference.getVariableNameList()) {
            if (variableNameElement.hasText(variableName.getText())) {
                if (variableNameElement.isEquivalentTo(variableName)) {
                    //LOGGER.log(Level.INFO,"Var Name Element match <"+variableNameElement.getText()+"> is reference to self");
                    continue;
                }
                return variableNameElement;
            }

        }
        return null;
    }

    @Nullable
    private static ObjJVariableName getVariableDeclarationFromInstanceVariableList(List<ObjJInstanceVariableDeclaration> instanceVariableDeclarations, String variableNameString) {
        for (ObjJInstanceVariableDeclaration variableDeclaration : instanceVariableDeclarations) {
            ObjJVariableName declarationVariableNameElement = variableDeclaration.getVariableName();
            if (declarationVariableNameElement.hasText(variableNameString)) {
                return declarationVariableNameElement;
            }
        }
        return null;
    }

    @Nullable
    public static ObjJVariableName getVariableDeclarationFromFile(
            @NotNull
                    PsiFile file,
            @NotNull
                    ObjJVariableName variableName, boolean mustBeLast) {
        PsiElement[] fileChildren = file.getChildren();
        List<ObjJBodyVariableAssignment> assignments = ArrayUtils.filter(Arrays.asList(fileChildren), ObjJBodyVariableAssignment.class);
        if (assignments.isEmpty()) {
            //LOGGER.log(Level.INFO, "No body variable assignments found in file. First Child: "+(fileChildren.length >0 ? fileChildren[0]:"null"));
            return null;
        }
        //LOGGER.log(Level.INFO, "Found "+Assignments.size()+" body variable assignments in file.");
        ObjJVariableName referencedVar;
        for (ObjJBodyVariableAssignment bodyVariableAssignment : assignments) {
            referencedVar = getVariableDeclaration(bodyVariableAssignment, variableName, false, mustBeLast);
            if (referencedVar != null && !referencedVar.isEquivalentTo(variableName)) {
                return referencedVar;
            }
        }
        List<ObjJExpr> expressions = ObjJTreeUtil.getChildrenOfTypeAsList(file, ObjJExpr.class);
        for (ObjJExpr expression : expressions) {
            if (expression.getLeftExpr() == null || expression.getLeftExpr().getVariableDeclaration() == null) {
                continue;
            }
            for (ObjJQualifiedReference reference : expression.getLeftExpr().getVariableDeclaration().getQualifiedReferenceList()) {
                if (mustBeLast) {
                    if (reference.getLastVar() != null && reference.getLastVar() != variableName && reference.getLastVar().getText().equals(variableName.getText())) {
                        return reference.getLastVar();
                    }
                }
                for (ObjJVariableName refVariableName : reference.getVariableNameList()) {
                    if (refVariableName != variableName && refVariableName.getText().equals(variableName.getText())) {
                        return refVariableName;
                    }
                }
            }
        }
        return null;
    }
    @Nullable
    private static ObjJVariableName getVariableDeclaration(ObjJBodyVariableAssignment bodyVariableAssignment,
                                                      @NotNull
                                                              ObjJVariableName variableName, boolean mustBeLast) {
        return getVariableDeclaration(bodyVariableAssignment, variableName, true, mustBeLast);
    }

    @Nullable
    public static ObjJVariableName getVariableDeclaration(ObjJBodyVariableAssignment bodyVariableAssignment, @NotNull ObjJVariableName variableName, boolean strict, boolean mustBeLast) {
        if (strict && bodyVariableAssignment.getVarModifier() == null) {
            return null;
        }
        ObjJVariableName referencedVar = getVariableWithNameFromBodyVariableAssignment(bodyVariableAssignment, variableName, mustBeLast);
        if (referencedVar != null) {
            return referencedVar;
        }
        return null;
    }

    @Nullable
    private static ObjJVariableName getVariableDeclaration(
            @NotNull
                    ObjJMethodHeader methodHeader, String variableName) {
        for (ObjJMethodDeclarationSelector selector : methodHeader.getMethodDeclarationSelectorList()) {
            ObjJVariableName methodHeaderVar = selector.getVariableName();
            if (methodHeaderVar != null && methodHeaderVar.hasText(variableName)) {
                return methodHeaderVar;
            }
        }
        return null;
    }
*/


}
