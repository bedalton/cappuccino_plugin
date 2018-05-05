package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJNamedElement;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjJFunctionDeclarationPsiUtil {

    @NotNull
    public static String getName(ObjJFunctionDeclaration functionDeclaration) {
        return functionDeclaration.getFunctionName() != null ? functionDeclaration.getFunctionName().getText() : "";
    }

    /**
     * Renames function
     *
     * @param functionDeclaration function to rename
     * @param name                new function name
     * @return function name element
     * @throws IncorrectOperationException exception
     */
    @NotNull
    public static ObjJFunctionName setName(
            @NotNull
                    ObjJFunctionDeclaration functionDeclaration,
            @NotNull
                    String name) throws IncorrectOperationException {
        ObjJFunctionName oldFunctionName = functionDeclaration.getFunctionName();
        ObjJFunctionName newFunctionName = ObjJElementFactory.createFunctionName(functionDeclaration.getProject(), name);
        Logger.getInstance(ObjJPsiImplUtil.class).assertTrue(newFunctionName != null);
        if (oldFunctionName == null) {
            if (functionDeclaration.getOpenParen() != null) {
                functionDeclaration.addBefore(functionDeclaration.getOpenParen(), newFunctionName);
            } else {
                functionDeclaration.addBefore(functionDeclaration.getFirstChild(), newFunctionName);
            }
        } else {
            functionDeclaration.getNode().replaceChild(oldFunctionName.getNode(), newFunctionName.getNode());
        }
        return newFunctionName;
    }

    /**
     * Renames function literal node.
     *
     * @param functionLiteral the literal to rename
     * @param name            the new name
     * @return this function literal
     * @throws IncorrectOperationException exception
     */
    @NotNull
    public static ObjJFunctionLiteral setName(
            @NotNull
                    ObjJFunctionLiteral functionLiteral,
            @NotNull
                    String name) throws IncorrectOperationException {
        //Get existing name node.
        PsiElement oldFunctionName = functionLiteral.getFunctionNameNode();
        //Create new name node
        ObjJVariableName newFunctionName = ObjJElementFactory.createVariableName(functionLiteral.getProject(), name);
        Logger.getInstance(ObjJPsiImplUtil.class).assertTrue(newFunctionName != null);

        //Name node is not part of function literal, so name node may not be present.
        //If name node is not present, must exit early.
        Logger.getInstance(ObjJPsiImplUtil.class).assertTrue(oldFunctionName != null);
        //Replace node
        oldFunctionName.getParent().getNode().replaceChild(oldFunctionName.getNode(), newFunctionName.getNode());
        return functionLiteral;
    }


    @NotNull
    public static PsiElement setName(@NotNull ObjJPreprocessorDefineFunction defineFunction, @NotNull String name) {
        if (defineFunction.getFunctionName() != null) {
            ObjJFunctionName functionName = ObjJElementFactory.createFunctionName(defineFunction.getProject(), name);
            if (functionName != null) {
                defineFunction.getNode().replaceChild(defineFunction.getFunctionName().getNode(), functionName.getNode());
            }
        } else if (defineFunction.getOpenParen() != null) {
            ObjJFunctionName functionName = ObjJElementFactory.createFunctionName(defineFunction.getProject(), name);
            if (functionName != null) {
                defineFunction.addBefore(defineFunction.getOpenParen(), functionName);
            }
        } else if (defineFunction.getVariableName() != null) {
            ObjJVariableName newVariableName = ObjJElementFactory.createVariableName(defineFunction.getProject(), name);
            defineFunction.getNode().replaceChild(defineFunction.getVariableName().getNode(), newVariableName.getNode());
        }
        return defineFunction;
    }

    @Nullable
    public static String getQualifiedNameText(ObjJFunctionCall functionCall) {
        if (functionCall.getQualifiedReference() == null) {
            return null;
        }
        return ObjJPsiImplUtil.getPartsAsString(functionCall.getQualifiedReference());
    }

    @NotNull
    public static String getFunctionNameAsString(ObjJFunctionLiteral functionLiteral) {
        if (functionLiteral.getStub() != null) {
            return functionLiteral.getStub().getFqName();
        }
        ObjJVariableDeclaration variableDeclaration = ObjJTreeUtil.getParentOfType(functionLiteral, ObjJVariableDeclaration.class);
        if (variableDeclaration == null || variableDeclaration.getQualifiedReferenceList().isEmpty()) {
            return "";
        }
        return ObjJPsiImplUtil.getPartsAsString(variableDeclaration.getQualifiedReferenceList().get(0));
    }

    @NotNull
    public static String getFunctionNameAsString(ObjJPreprocessorDefineFunction functionDeclaration) {
        if (functionDeclaration.getStub() != null) {
            return functionDeclaration.getStub().getFunctionName();
        }
        return functionDeclaration.getFunctionName() != null ? functionDeclaration.getFunctionName().getText() : functionDeclaration.getVariableName() != null ? functionDeclaration.getVariableName().getText() : "{UNDEF}";
    }

    @NotNull
    public static List<String> getFunctionNamesAsString(ObjJFunctionLiteral functionLiteral) {
        List<String> out = new ArrayList<>();
        ObjJVariableDeclaration variableDeclaration = ObjJTreeUtil.getParentOfType(functionLiteral, ObjJVariableDeclaration.class);
        if (variableDeclaration == null || variableDeclaration.getQualifiedReferenceList().isEmpty()) {
            return Collections.emptyList();
        }
        for (ObjJQualifiedReference reference : variableDeclaration.getQualifiedReferenceList()) {
            String name = ObjJPsiImplUtil.getPartsAsString(reference);
            if (!name.isEmpty()) {
                out.add(name);
            }
        }
        return out;
    }

    @NotNull
    public static String getFunctionNameAsString(ObjJFunctionDeclaration functionDeclaration) {
        if (functionDeclaration.getStub() != null) {
            return functionDeclaration.getStub().getFunctionName();
        }
        return functionDeclaration.getFunctionName() != null ? functionDeclaration.getFunctionName().getText() : "";
    }

    @NotNull
    public static List<ObjJVariableName> getParamNameElements(
            @NotNull
                    ObjJFunctionDeclarationElement functionDeclaration) {
        List<ObjJVariableName> out = new ArrayList<>();
        for (Object parameterArg : functionDeclaration.getFormalParameterArgList()) {
            out.add(((ObjJFormalParameterArg) parameterArg).getVariableName());
        }
        if (functionDeclaration.getLastFormalParameterArg() != null) {
            out.add(functionDeclaration.getLastFormalParameterArg().getVariableName());
        }
        return out;

    }

    @NotNull
    public static List<String> getParamNames(
            @NotNull
                    ObjJFunctionDeclarationElement functionDeclaration) {
        if (functionDeclaration.getStub() != null) {
            //noinspection unchecked
            return ((ObjJFunctionDeclarationElementStub) functionDeclaration.getStub()).getParamNames();
        }
        List<String> out = new ArrayList<>();
        for (Object parameterArg : functionDeclaration.getFormalParameterArgList()) {
            out.add(((ObjJFormalParameterArg) parameterArg).getVariableName().getText());
        }
        if (functionDeclaration.getLastFormalParameterArg() != null) {
            out.add(functionDeclaration.getLastFormalParameterArg().getVariableName().getText());
        }
        return out;
    }

    @NotNull
    public static String getReturnType(
            @NotNull
                    ObjJFunctionDeclaration functionDeclaration) {
        if (functionDeclaration.getStub() != null) {
            functionDeclaration.getStub().getReturnType();
        }
        return ObjJClassType.UNDETERMINED;
    }

    @NotNull
    public static String getReturnType(
            @NotNull
                    ObjJFunctionLiteral functionLiteral) {
        if (functionLiteral.getStub() != null) {
            functionLiteral.getStub().getReturnType();
        }
        return ObjJClassType.UNDETERMINED;
    }

    @NotNull
    public static String getReturnType(@NotNull ObjJPreprocessorDefineFunction functionDefinition) {
        if (functionDefinition.getStub() != null) {
            return functionDefinition.getStub().getReturnType();
        }
        /*
        if (functionDefinition.getPreprocessorDefineBody() != null) {
            ObjJPreprocessorDefineBody defineBody = functionDefinition.getPreprocessorDefineBody();
            List<ObjJReturnStatement> returnStatements = new ArrayList<>();
            if (defineBody.getBlock() != null) {
                ObjJBlock block = defineBody.getBlock();
                returnStatements.addAll(ObjJBlockPsiUtil.getBlockChildrenOfType(block, ObjJReturnStatement.class, true));
            } else if (defineBody.getPreprocessorBodyStatementList() != null) {
                ObjJPreprocessorBodyStatementList statementList = defineBody.getPreprocessorBodyStatementList();
                if (statementList != null) {
                    returnStatements.addAll(statementList.getReturnStatementList());
                    for (ObjJBlock block : statementList.getBlockList()) {
                        java.util.logging.LOGGER.log(Level.INFO, "Looping preprocessor block in block");
                        returnStatements.addAll(ObjJBlockPsiUtil.getBlockChildrenOfType(block, ObjJReturnStatement.class, true));
                    }
                }
            }
            for (ObjJReturnStatement returnStatement : returnStatements) {
                java.util.logging.LOGGER.log(Level.INFO, "Looping return statement: <"+returnStatement.getText()+">");
                if (returnStatement.getExpr() != null) {
                    //todo Figure out how to get the expression return types, when index is not ready.
                    List<String> types = Collections.emptyList();// ObjJVarTypeResolveUtil.getExpressionReturnTypes(returnStatement.getExpr(), true);
                    return !types.isEmpty() ? types.get(0) : ObjJClassType.UNDETERMINED;
                }
            }
        }
        */
        return ObjJClassType.UNDETERMINED;
    }

    @Nullable
    public static ObjJNamedElement getFunctionNameNode(
            @NotNull
                    ObjJFunctionLiteral functionLiteral) {
        ObjJVariableDeclaration variableDeclaration = ObjJTreeUtil.getParentOfType(functionLiteral, ObjJVariableDeclaration.class);
        if (variableDeclaration == null) {
            return null;
        }
        return !variableDeclaration.getQualifiedReferenceList().isEmpty() ? variableDeclaration.getQualifiedReferenceList().get(0).getLastVar() : null;
    }

}
