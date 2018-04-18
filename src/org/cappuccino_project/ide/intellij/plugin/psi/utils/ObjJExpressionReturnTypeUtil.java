package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.cappuccino_project.ide.intellij.plugin.exceptions.CannotDetermineException;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyInterruptingException;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJInstanceVariablesByClassIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJVariableAssignment;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjJExpressionReturnTypeUtil {


    public static boolean isReturnTypeInstanceOf(@Nullable ObjJExpr expr, @Nullable String classType, @NotNull Project project) throws CannotDetermineException, IndexNotReadyInterruptingException {
        return isReturnTypeInstanceOf(expr, classType, project, false);
    }

    public static boolean isReturnTypeInstanceOf(@Nullable ObjJExpr expr, @Nullable String classType, @NotNull Project project, boolean defaultValueIfNull) throws IndexNotReadyInterruptingException {
        if (classType == null) {
            return defaultValueIfNull;
        }
        String returnType;
        try {
             returnType = getReturnType(expr, true);
        } catch (MixedReturnTypeException e) {
            return e.returnTypesList.contains(classType);
        }
        if (returnType == null) {
            return defaultValueIfNull;
        }
        if (returnType.equals(UNDETERMINED) || classType.equals(returnType)) {
            return true;
        }

        if (classType.equals(ID)) {
            return !isPrimitive(classType);
        }
        if (classType.equals(LONG)) {
            if (returnType.equals(INT)) {
                return true;
            }
        }
        try {
            return ObjJClassType.isSubclassOrSelf(classType, returnType, project);
        } catch (CannotDetermineException ignored){
            return defaultValueIfNull;
        }
    }

    @Nullable
    public static ExpressionReturnTypeResults getReturnTypes(@Nullable ObjJExpr expr) {
        if (expr == null) {
            return null;
        }
        return getReturnTypes(new ExpressionReturnTypeResults(expr.getProject()), expr);
    }
    @Nullable
    public static ExpressionReturnTypeResults getReturnTypes(@NotNull ExpressionReturnTypeResults results, @Nullable ObjJExpr expr) {
        if (expr == null) {
            return null;
        }
        if (testAllSubExpressions(expr, ObjJExpressionReturnTypeUtil::isReturnTypeString)) {
            results.tick(STRING);
        } else if (testAllSubExpressions(expr, ObjJExpressionReturnTypeUtil::isReturnTypeInteger)) {
            results.tick(INT);
        } else if (testAllSubExpressions(expr, ObjJExpressionReturnTypeUtil::isReturnTypeFloat)) {
            results.tick(FLOAT);
        } else if (testAllSubExpressions(expr, ObjJExpressionReturnTypeUtil::isReturnTypeDouble)) {
            results.tick(DOUBLE);
        } else if (testAllSubExpressions(expr, ObjJExpressionReturnTypeUtil::isReturnTypeBOOL)) {
            results.tick(BOOL);
        }
        for (String varType : getVariableNameType(expr)) {
            results.tick(varType);
        }
        getReturnTypesFromMethodCall(results, expr);
        return results;
    }

    @Nullable
    public static String getReturnType(@Nullable ObjJExpr expr, boolean follow) throws MixedReturnTypeException {
        if (expr == null) {
            return null;
        }
        if (testAllSubExpressions(expr, ObjJExpressionReturnTypeUtil::isReturnTypeString)) {
            return STRING;
        }
        if (testAllSubExpressions(expr, ObjJExpressionReturnTypeUtil::isReturnTypeFloat)) {
            return FLOAT;
        }
        if (testAllSubExpressions(expr, ObjJExpressionReturnTypeUtil::isReturnTypeDouble)) {
            return DOUBLE;
        }
        if (testAllSubExpressions(expr, ObjJExpressionReturnTypeUtil::isReturnTypeInteger)) {
            return INT;
        }
        if (testAllSubExpressions(expr, ObjJExpressionReturnTypeUtil::isReturnTypeBOOL)) {
            return BOOL;
        }
        String returnType = getSelfOrSuper(expr);
        if (returnType != null) {
            return returnType;
        }
        returnType = getReturnTypeFromMethodCall(expr, follow);
        if (returnType != null) {
            return returnType;
        }
        List<String> returnTypes = getVariableNameType(expr);
        if (returnTypes.size() == 1) {
            return returnTypes.get(0);
        } else if (!returnTypes.isEmpty()) {
            try {
                returnTypes = ObjJInheritanceUtil.reduceToDeepestInheritance(returnTypes, expr.getProject());
                if (returnTypes.size() == 1) {
                    return returnTypes.get(0);
                }
                throw new MixedReturnTypeException(returnTypes);
            } catch (IndexNotReadyInterruptingException e) {
                e.printStackTrace();
            }
        }
        return returnType != null ? returnType : UNDETERMINED;
    }

    private static void getReturnTypesFromMethodCall(@NotNull ExpressionReturnTypeResults results, @Nullable ObjJExpr expr) {
        if (expr == null) {
            return;
        }
        String returnType = null;
        for (ObjJExpr exprElementInLoop : getAllSubExpressions(expr, true)) {
            ObjJMethodCall methodCall = null;
            if (exprElementInLoop == null) {
                continue;
            }
            if (exprElementInLoop.getLeftExpr() != null && exprElementInLoop.getLeftExpr().getMethodCall() != null) {
                methodCall = exprElementInLoop.getLeftExpr().getMethodCall();
                try {
                    returnType = getReturnTypeFromMethodCall(methodCall, true, null);
                    if (returnType == null) {
                        continue;
                    }
                    results.tick(returnType);
                } catch (IndexNotReadyInterruptingException ignored) {
                }
            }
        }
    }

    @Nullable
    private static String getReturnTypeFromMethodCall(@Nullable ObjJExpr expr, boolean follow) {
        if (expr == null) {
            return null;
        }
        List<String> returnTypes = new ArrayList<>();
        String returnType = null;
        for (ObjJExpr exprElementInLoop : getAllSubExpressions(expr, true)) {
            if (exprElementInLoop == null) {
                continue;
            }
            ObjJMethodCall methodCall = null;
            if (exprElementInLoop.getLeftExpr() != null && exprElementInLoop.getLeftExpr().getMethodCall() != null) {
                methodCall = exprElementInLoop.getLeftExpr().getMethodCall();
                try {
                    returnType = getReturnTypeFromMethodCall(methodCall, follow, null);
                    if (returnType == null) {
                        continue;
                    }
                    if (!returnTypes.contains(returnType)) {
                        returnTypes.add(returnType);
                    }
                } catch (IndexNotReadyInterruptingException ignored) {
                }
            }
        }
        if (returnTypes.isEmpty()) {
            return null;
        }
        return returnTypes.get(0);
    }


    private static boolean isReturnTypeString(@Nullable ObjJExpr expr) {
        if (expr == null) {
            return false;
        }
        return isLeftExpressionReturnTypeString(expr.getLeftExpr());
    }

    private static boolean isLeftExpressionReturnTypeString(@Nullable ObjJLeftExpr leftExpr) {
        if (leftExpr == null) {
            return false;
        }
        if (leftExpr.getPrimary() == null) {
            return false;
        }
        return leftExpr.getPrimary().getStringLiteral() != null || leftExpr.getRegularExpressionLiteral() != null;
    }

    private static boolean isReturnTypeInteger(@Nullable ObjJExpr expr) throws CannotDetermineException {
        if (expr == null) {
            return false;
        }
        if (expr.getBitNot() != null) {
            return true;
        }
        if (expr.getLeftExpr() == null) {
            return false;
        }
        ObjJLeftExpr leftExpr = expr.getLeftExpr();
        if (leftExpr.getPrimary() != null) {
            ObjJPrimary primary = leftExpr.getPrimary();
            if (primary.getInteger() != null) {
                return true;
            }
        }
        if (leftExpr.getPlusPlus() != null || leftExpr.getMinusMinus() != null) {
            return true;
        }
        for (ObjJRightExpr rightExpr : expr.getRightExprList()) {
            if (isRightSideEvaluateToInteger(rightExpr)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRightSideEvaluateToInteger(ObjJRightExpr rightExpr) throws CannotDetermineException {
        if (rightExpr.getBoolAssignExprPrime() != null) {
            return isReturnTypeInteger(rightExpr.getBoolAssignExprPrime().getIfTrue()) ||
                    isReturnTypeInteger(rightExpr.getBoolAssignExprPrime().getIfFalse());
        }
        if (!rightExpr.getArrayIndexSelectorList().isEmpty()) {
            throw new CannotDetermineException();
        }
        if (rightExpr.getAssignmentExprPrime() != null) {
            return isReturnTypeInteger(rightExpr.getAssignmentExprPrime().getExpr());
        }
        if (rightExpr.getMathExprPrime() != null) {
            if (rightExpr.getMathExprPrime() != null) {
                ObjJMathExprPrime mathExprPrime = rightExpr.getMathExprPrime();
                return mathExprPrime.getBitAnd() != null ||
                        mathExprPrime.getBitNot() != null ||
                        mathExprPrime.getBitOr() != null ||
                        mathExprPrime.getBitXor() != null ||
                        mathExprPrime.getLeftShiftArithmatic() != null ||
                        mathExprPrime.getRightShiftArithmatic() != null ||
                        mathExprPrime.getLeftShiftLogical() != null ||
                        mathExprPrime.getRightShiftLogical() != null ||
                        mathExprPrime.getModulus() != null ||
                        isReturnTypeInteger(mathExprPrime.getExpr());
            }
        }
        if (rightExpr.getBoolAssignExprPrime() != null) {
            return isReturnTypeInteger(rightExpr.getBoolAssignExprPrime().getIfFalse()) ||
                    isReturnTypeInteger(rightExpr.getBoolAssignExprPrime().getIfFalse());
        }
        return false;
    }


    private static boolean isReturnTypeFloat(@Nullable ObjJExpr expr) {
        return testAllSubExpressions(expr, ObjJExpressionReturnTypeUtil::isReturnTypeFloatTest);
    }


    private static boolean isReturnTypeFloatTest(@Nullable ObjJExpr expr) throws CannotDetermineException {
        if (expr == null) {
            return false;
        }

        if (isReturnTypeInteger(expr)) {
            return true;
        }

        if (expr.getLeftExpr() == null) {
            return false;
        }
        if (expr.getLeftExpr().getPrimary() == null) {
            return false;
        }
        return expr.getLeftExpr().getPrimary().getDecimalLiteral() != null;
    }

    private static boolean isReturnTypeDouble(@Nullable ObjJExpr expr) throws CannotDetermineException {
        if (expr == null) {
            return false;
        }
        return isReturnTypeFloat(expr);
    }

    /**
     * Evaluates whether an expression evaluates to BOOL
     * @param expr expression element to test
     * @return <b>true</b> if expression evaluate to bool, <b>false</b> otherwise
     */
    private static boolean isReturnTypeBOOL(@Nullable ObjJExpr expr) {
        try {
            return testAllRightExpressions(expr, ObjJExpressionReturnTypeUtil::isRightExpressionBool);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Evaluates whether a right side expression evaluates to BOOL
     * @param rightExpr right expression to test
     * @return <b>true</b> if right expression evaluate to bool, <b>false</b> otherwise
     */
    @SuppressWarnings("SimplifiableIfStatement")
    private static boolean isRightExpressionBool(@Nullable ObjJRightExpr rightExpr) {
        if (rightExpr == null) {
            return false;
        }

        if (rightExpr.getJoinExprPrime() != null ||
                rightExpr.getInstanceOfExprPrime() != null
        ) {
            return true;
        }
        if (rightExpr.getBoolAssignExprPrime() != null) {
            ObjJBoolAssignExprPrime assignExprPrime = rightExpr.getBoolAssignExprPrime();
            return isReturnTypeBOOL(assignExprPrime.getIfTrue()) ||
                    (assignExprPrime.getIfFalse() != null && isReturnTypeBOOL(assignExprPrime.getIfFalse()));
        }
        if (rightExpr.getBooleanExprPrime() != null) {
            return true;
        }
        if (rightExpr.getAssignmentExprPrime() != null) {
            return isReturnTypeBOOL(rightExpr.getAssignmentExprPrime().getExpr());
        }
        return false;
    }

    @NotNull
    private static List<ObjJExpr> getAllSubExpressions(@Nullable ObjJExpr expr) {
        return getAllSubExpressions(expr, true);
    }
    @NotNull
    private static List<ObjJExpr> getAllSubExpressions(@Nullable ObjJExpr expr, boolean addSelf) {
        if (expr == null) {
            return Collections.emptyList();
        }

        ObjJLeftExpr leftExpr = expr.getLeftExpr();
        List<ObjJExpr> expressions = new ArrayList<>();
        if (addSelf) {
            expressions.add(expr);
        }
        expressions.add(expr.getExpr());
        if (leftExpr != null) {
            if (leftExpr.getVariableDeclaration() != null) {
                expressions.add(leftExpr.getVariableDeclaration().getExpr());
            }
            if (leftExpr.getVariableAssignmentLogical() != null) {
                ObjJVariableAssignmentLogical variableAssignmentLogical = leftExpr.getVariableAssignmentLogical();
                expressions.add(variableAssignmentLogical.getAssignmentExprPrime().getExpr());
            }
        }
        addRightExpressionExpressions(expressions, expr.getRightExprList());
        return expressions;

    }

    private static void addRightExpressionExpressions(@NotNull List<ObjJExpr> expressions, @NotNull List<ObjJRightExpr> rightExpressions) {
       for (ObjJRightExpr rightExpr : rightExpressions) {
           if (rightExpr == null) {
               continue;
           }
           if (rightExpr.getAssignmentExprPrime() != null) {
               expressions.add(rightExpr.getAssignmentExprPrime().getExpr());
           }
           if (rightExpr.getBoolAssignExprPrime() != null) {
               expressions.add(rightExpr.getBoolAssignExprPrime().getIfTrue());
               expressions.add(rightExpr.getBoolAssignExprPrime().getIfFalse());
           }
       }
    }

    private static boolean testAllSubExpressions(@Nullable ObjJExpr expr, @NotNull SubExpressionTest test) {
        if (expr == null) {
            return false;
        }
        try {
            if (test.test(expr)) {
                return true;
            }
        } catch (CannotDetermineException ignored) {}
        try {
            if (testLeftExpression(expr.getLeftExpr(), test)) {
                return true;
            }
        } catch (CannotDetermineException ignored) {}
        for (ObjJRightExpr rightExpr : expr.getRightExprList()) {
            try {
                if (testRightExpressionsExpressions(rightExpr, test)) {
                    return true;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    private static boolean testAllRightExpressions(@Nullable ObjJExpr expression, @NotNull RightExpressionTest test) throws CannotDetermineException {
        if (expression == null) {
            return false;
        }
        List<ObjJExpr> expressions = getAllSubExpressions(expression);
        for (ObjJExpr expressionInLoop : expressions) {
            for (ObjJRightExpr rightExpression : expressionInLoop.getRightExprList()) {
                if (test.test(rightExpression)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean testLeftExpression(@Nullable ObjJLeftExpr leftExpr, @NotNull SubExpressionTest test) throws CannotDetermineException {
        if (leftExpr == null) {
            return false;
        }
        if (leftExpr.getVariableDeclaration() != null) {
            if (test.test(leftExpr.getVariableDeclaration().getExpr())) {
                return true;
            }
        }
        if (leftExpr.getVariableAssignmentLogical() != null) {
            ObjJVariableAssignmentLogical variableAssignmentLogical = leftExpr.getVariableAssignmentLogical();
            return test.test(variableAssignmentLogical.getAssignmentExprPrime().getExpr());
        }
        return false;
    }

    private static boolean testRightExpressionsExpressions(@Nullable ObjJRightExpr rightExpr, @NotNull SubExpressionTest test) throws CannotDetermineException {
        if (rightExpr == null) {
            return false;
        }
        if (rightExpr.getAssignmentExprPrime() != null) {
            return test.test(rightExpr.getAssignmentExprPrime().getExpr());
        }
        if (rightExpr.getBoolAssignExprPrime() != null) {
            return test.test(rightExpr.getBoolAssignExprPrime().getIfTrue()) ||
                    test.test(rightExpr.getBoolAssignExprPrime().getIfFalse());
        }
        return false;
    }

    @Nullable
    private static String getReturnTypeFromPrimary(@NotNull
                                                                           ObjJPrimary primary) throws CannotDetermineException {
        if (primary.getBooleanLiteral() != null) {
            return BOOL;
        }
        if (primary.getDecimalLiteral() != null) {
            return FLOAT;
        }
        if (primary.getInteger() != null) {
            return INT;
        }

        if (primary.getNullLiterals() != null) {
            return NIL;
        }
        if (primary.getStringLiteral() != null) {
            return STRING;
        }
        return null;
    }

    private static boolean isParent(@NotNull
                                                     PsiElement parent, @NotNull ObjJExpr childExpression) {
        return ObjJTreeUtil.isAncestor(parent, childExpression, false);
    }

    @Nullable
    private static String getReturnTypeFromLeftExpression(@Nullable ObjJLeftExpr leftExpr) throws CannotDetermineException {
        if (leftExpr == null) {
            throw new CannotDetermineException();
        }
        if (leftExpr.getArrayLiteral() != null) {
            return ARRAY;
        }
        if (leftExpr.getMinusMinus() != null || leftExpr.getPlusPlus() != null) {
            return INT;
        }

        if (leftExpr.getFunctionLiteral() != null) {
            return JS_FUNCTION;
        }

        if (leftExpr.getSelectorLiteral() != null) {
            return SEL;
        }

        if (leftExpr.getObjectLiteral() != null) {
            return CPOBJECT;
        }

        if (leftExpr.getFunctionCall() != null) {
            throw new CannotDetermineException();
        }

        if (leftExpr.getMethodCall() != null) {
            throw new CannotDetermineException();
        }

        return null;
    }

    @Nullable
    private static String getReturnTypeFromMethodCall(@NotNull ObjJMethodCall methodCall, boolean follow, @Nullable String defaultReturnType) throws IndexNotReadyInterruptingException {
        final Project project = methodCall.getProject();
        if (DumbService.isDumb(project)) {
            throw new IndexNotReadyInterruptingException();
        }
        if (methodCall.getSelectorString().equals("alloc") || methodCall.getSelectorString().equals("new")) {
            if (methodCall.getCallTarget().getText().equals("self") || methodCall.getCallTarget().getText().equals("super")) {
                return methodCall.getContainingClassName();
            } else {
                return methodCall.getCallTarget().getText();
            }
        }
        List<ObjJMethodHeaderDeclaration> methodHeaders = ObjJUnifiedMethodIndex.getInstance().get(methodCall.getSelectorString(), project);
        if (methodHeaders.isEmpty()) {
            return null;
        }
        ExpressionReturnTypeResults results = null;
        if (methodCall.getCallTarget().getText().equals("self")) {
            results = new ExpressionReturnTypeResults(methodCall.getProject());
            List<String> inheritance = ObjJInheritanceUtil.getInheritanceUpAndDown(methodCall.getContainingClassName(), methodCall.getProject());
            results.tick(inheritance);
        } else if (methodCall.getCallTarget().getText().equals("super")){
            if (methodCall.getCallTarget().getText().equals("self")) {
                results = new ExpressionReturnTypeResults(methodCall.getProject());
                String containingClassName = methodCall.getContainingClassName();
                List<String> inheritance = ObjJInheritanceUtil.getAllInheritedClasses(containingClassName, methodCall.getProject());
                inheritance.remove(containingClassName);
                results.tick(inheritance);
            }
        } else if (methodCall.getCallTarget().getExpr() != null) {
             results = ObjJExpressionReturnTypeUtil.getReturnTypes(methodCall.getCallTarget().getExpr());
        }
        List<String> possibleClassNames = results != null ? results.getInheritanceUpAndDown() : ArrayUtils.EMPTY_STRING_ARRAY;
        boolean hasId = false;
        List<String> returnTypes = new ArrayList<>();
        for (ObjJMethodHeaderDeclaration methodHeaderDeclaration : methodHeaders) {
            String returnType = null;
            if (!possibleClassNames.isEmpty() && !possibleClassNames.contains(methodHeaderDeclaration.getContainingClassName())) {
                continue;
            }
            if (methodHeaderDeclaration instanceof ObjJMethodHeader) {
                returnType = ObjJMethodPsiUtils.getReturnType((ObjJMethodHeader)methodHeaderDeclaration, follow);
            }
            if (returnType == null) {
                continue;
            }
            if (returnType.equals("id")) {
                hasId = true;
                continue;
            }
            if (!returnTypes.contains(returnType)) {
                returnTypes.add(returnType);
            } else if (!returnType.equals(UNDETERMINED)) {
                return returnType;
            }
        }
        return hasId ? "id" : defaultReturnType;
    }

    @NotNull
    public static List<String> getReturnTypeFromFormalVariableType(ObjJVariableName variableName) {
        final Project project =variableName.getProject();
        final String variableNameText = variableName.getQualifiedNameText();
        if (variableName.getContainingClass() != null) {
            for (ObjJInstanceVariableDeclaration instanceVariableDeclaration : ObjJInstanceVariablesByClassIndex.getInstance().get(variableName.getContainingClassName(), project)) {
                if (instanceVariableDeclaration.getVariableName() != null && instanceVariableDeclaration.getVariableName().getText().equals(variableNameText)) {
                    return ObjJInheritanceUtil.getAllInheritedClasses(instanceVariableDeclaration.getFormalVariableType().getText(), project);
                }
            }
        }
        ObjJMethodDeclaration methodDeclaration = variableName.getParentOfType(ObjJMethodDeclaration.class);
        if (methodDeclaration != null) {
            ObjJVariableName methodHeaderVariable = ObjJMethodPsiUtils.getHeaderVariableNameMatching(methodDeclaration.getMethodHeader(), variableNameText);
            if (methodHeaderVariable != null) {
                ObjJMethodDeclarationSelector methodHeaderSelector = methodHeaderVariable.getParentOfType(ObjJMethodDeclarationSelector.class);
                if (methodHeaderSelector != null && methodHeaderSelector.getFormalVariableType() != null) {
                    if (methodHeaderSelector.getFormalVariableType().getClassName() != null) {
                        return ObjJInheritanceUtil.getAllInheritedClasses(methodHeaderSelector.getFormalVariableType().getClassName().getText(), methodHeaderVariable.getProject());
                    }
                }
            }
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    private static String getSelfOrSuper(ObjJExpr expr) {
        if (expr.getText().equals("self")) {
            return ObjJPsiImplUtil.getContainingClassName(expr);
        } else if (expr.getText().equals("super")) {
            return ObjJPsiImplUtil.getContainingSuperClassName(expr);
        }
        return null;
    }



    public static List<String> getVariableNameType(@NotNull ObjJExpr expr) {
        List<String> out = new ArrayList<>();
        for (ObjJExpr currentExpr : getAllSubExpressions(expr, false)) {
            if (currentExpr == null || currentExpr.getLeftExpr() == null) {
                continue;
            }
            if (expr.isEquivalentTo(currentExpr)) {
                continue;
            }
            ObjJQualifiedReference reference = currentExpr.getLeftExpr().getQualifiedReference();
            if (reference == null) {
                continue;
            }
            if (reference.getLastVar() == null) {
                continue;
            }
            if (reference.getText().equals("self")) {
                return ObjJInheritanceUtil.getAllInheritedClasses(ObjJHasContainingClassPsiUtil.getContainingClassName(reference), reference.getProject());
            }
            if (reference.getText().equals("super")) {
                String className = ObjJHasContainingClassPsiUtil.getContainingClassName(reference);
                List<String> classNames = ObjJInheritanceUtil.getAllInheritedClasses(className, reference.getProject());
                classNames.remove(className);
                return classNames;
            }
            final String fqName = ObjJVariableNameUtil.getQualifiedNameAsString(reference.getLastVar());
            for (ObjJVariableAssignment variableAssignment : ObjJVariableAssignmentsPsiUtil.getAllVariableAssignmentsMatchingName(expr, fqName)) {
                ObjJExpr assignedValue = variableAssignment.getAssignedValue();
                if (assignedValue.isEquivalentTo(currentExpr)) {
                    continue;
                }
                try {
                    out.add(getReturnType(assignedValue, true));
                } catch (MixedReturnTypeException e) {
                    for (String varType : e.getReturnTypesList()) {
                        if (!out.contains(varType)) {
                            out.add(varType);
                        }
                    }
                }
            }
        }
        return out;
    }

/*
    public static List<String> getVariableNameType(@NotNull ObjJExpr expr) {
        List<String> out = new ArrayList<>();
        for (ObjJExpr currentExpr : getAllSubExpressions(expr)) {
            if (currentExpr == null || currentExpr.getLeftExpr() == null) {
                continue;
            }
            ObjJQualifiedReference reference = currentExpr.getLeftExpr().getQualifiedReference();
            if (reference == null) {
                continue;
            }
            if (reference.getLastVar() == null) {
                continue;
            }
            final String fqName = ObjJVariableNameUtil.getQualifiedNameAsString(reference.getLastVar());
            List<ObjJVariableName> elements = ObjJVariableNameUtil.getAndFilterSiblingVariableNameElements(expr, -1, (varNameElement) -> ObjJVariableNameUtil.getQualifiedNameAsString(varNameElement).equals(fqName));
            for (ObjJVariableName variableName : elements) {
                ObjJVariableDeclaration declaration = variableName.getParentOfType(ObjJVariableDeclaration.class);
                if (declaration == null) {
                    continue;
                }
                boolean isAssigned = false;
                for (ObjJQualifiedReference qualifiedReference : declaration.getQualifiedReferenceList()) {
                    if (variableName.getParent().isEquivalentTo(qualifiedReference)) {
                        isAssigned = true;
                        break;
                    }
                }
                if (isAssigned) {
                    try {
                        out.add(getReturnType(declaration.getExpr(), null));
                    } catch (MixedReturnTypeException e) {
                        for (String varType : e.getReturnTypesList()) {
                            if (!out.contains(varType)) {
                                out.add(varType);
                            }
                        }
                    }
                }
            }
        }
        return out;
    }
*/
    private static interface SubExpressionTest {
        boolean test(ObjJExpr expr) throws CannotDetermineException;
    }

    private interface  RightExpressionTest {
        boolean test(ObjJRightExpr rightExpr) throws CannotDetermineException;
    }

    public static class MixedReturnTypeException extends Exception {
        private final List<String> returnTypesList;
        MixedReturnTypeException(List<String> returnTypesList) {
            super("More than one return type found");
            this.returnTypesList = returnTypesList;
        }

        public List<String> getReturnTypesList() {
            return returnTypesList;
        }
    }

    public static class ExpressionReturnTypeResults {
        private final Project project;
        private final List<ExpressionReturnTypeReference> references;
        private boolean changed = false;
        private List<String> referencedAncestors;

        public ExpressionReturnTypeResults(@NotNull Project project) {
            this(new ArrayList<>(), project);
        }

        private ExpressionReturnTypeResults(@NotNull List<ExpressionReturnTypeReference> references, @NotNull Project project) {
            this.project = project;
            this.references = references;
        }

        @NotNull
        public List<ExpressionReturnTypeReference> getReferences() {
            return references;
        }

        @NotNull
        public List<ExpressionReturnTypeReference> getReferencesAsList() {
            return references;
        }

        public void tick(List<String> refs) {
            for (String ref : refs) {
                tick(ref);
            }
        }

        private void tick(@NotNull String ref, int ticks) {

            ExpressionReturnTypeReference refObject = getReference(ref);
            if (refObject == null) {
                tick(ref);
                ticks -= 1;
                refObject = getReference(ref);
            }
            assert refObject != null;
            refObject.references += ticks;
        }

        public void tick(ExpressionReturnTypeResults results) {
            for (ExpressionReturnTypeReference ref : results.getReferences()) {
                tick(ref.getType(), ref.getReferences());
            }
        }

        public void tick(@NotNull String ref) {
            if (ref.isEmpty()) {
                return;
            }
            ExpressionReturnTypeReference result = getReference(ref);
            if (result != null) {
                result.tick();
            } else {
                changed = true;
                result = new ExpressionReturnTypeReference(ref);
                references.add(result);
            }
        }

        public ExpressionReturnTypeReference getReference(@NotNull String ref) {
            if (ref.isEmpty()) {
                return null;
            }
            for (ExpressionReturnTypeReference result : references) {
                if (result.type.equals(ref)) {
                    return result;
                }
            }
            return null;
        }

        public boolean isReferenced(@NotNull String ref) {
            return getReference(ref) != null || getInheritanceUpAndDown().contains(ref);
        }

        private List<String> getInheritanceUpAndDown() {
            if (referencedAncestors != null && !changed) {
                return referencedAncestors;
            } else if (DumbService.isDumb(project)) {
                return ArrayUtils.EMPTY_STRING_ARRAY;
            } else {
                referencedAncestors = new ArrayList<>();
            }
            changed = false;
            for (ExpressionReturnTypeReference result : references) {
                if (isPrimitive(result.getType())) {
                    continue;
                }
                getInheritanceUpAndDown(referencedAncestors, result.getType());
            }
            return referencedAncestors;
        }

        private void getInheritanceUpAndDown(@NotNull List<String> referencedAncestors, @NotNull String className) {
            if (referencedAncestors.contains(className)) {
                return;
            }
            for (String currentClassName : ObjJInheritanceUtil.getInheritanceUpAndDown(className, project)) {
                if (!referencedAncestors.contains(currentClassName)) {
                    referencedAncestors.add(currentClassName);
                }
            }
        }

        public int numReferences(@NotNull String ref) {
            ExpressionReturnTypeReference result = getReference(ref);
            return result != null ? result.getReferences() : 0;
        }

    }

    public static class ExpressionReturnTypeReference {
        private final String type;
        private int references;
        private ExpressionReturnTypeReference(@NotNull String type) {
            this.type = type;
            references = 1;
        }

        public String getType() {
            return type;
        }

        public int getReferences() {
            return references;
        }

        private int tick() {
            return ++references;
        }
    }

}
