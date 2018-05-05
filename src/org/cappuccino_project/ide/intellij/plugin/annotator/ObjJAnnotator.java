package org.cappuccino_project.ide.intellij.plugin.annotator;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJNeedsSemiColon;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJBlockPsiUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ObjJAnnotator implements Annotator {

    //private static final Logger LOGGER = Logger.getLogger(ObjJAnnotator.class.getName());

    @Override
    public void annotate(
            @NotNull
                    PsiElement psiElement,
            @NotNull
                    AnnotationHolder annotationHolder) {
        if (DumbService.getInstance(psiElement.getProject()).isDumb()) {
            return;
        }
        //LOGGER.log(Level.INFO, "Annotating element of type: "+psiElement.getNode().getElementType().toString());
        try {
            //Annotate Method calls
            if (psiElement instanceof ObjJMethodCall) {
                ObjJMethodCallAnnotatorUtil.annotateMethodCall((ObjJMethodCall) psiElement, annotationHolder);
            } else if (psiElement instanceof ObjJSelectorLiteral) {
                ObjJMethodCallAnnotatorUtil.annotateSelectorLiteral((ObjJSelectorLiteral) psiElement, annotationHolder);
            } else if (psiElement instanceof ObjJVariableName) {
                ObjJVariableAnnotatorUtil.annotateVariable((ObjJVariableName) psiElement, annotationHolder);
            } else if (psiElement instanceof ObjJImplementationDeclaration) {
                ObjJImplementationDeclarationAnnotatorUtil.annotateImplementationDeclaration((ObjJImplementationDeclaration) psiElement, annotationHolder);
            } else if (psiElement instanceof ObjJProtocolDeclaration) {
                ObjJProtocolDeclarationAnnotatorUtil.annotateProtocolDeclaration((ObjJProtocolDeclaration) psiElement, annotationHolder);
            } else if (psiElement instanceof ObjJBlock) {
                validateBlock((ObjJBlock) psiElement, annotationHolder);
            } else if (psiElement instanceof ObjJReturnStatement) {
                validateReturnStatement(((ObjJReturnStatement)psiElement), annotationHolder);
            } else {
                validateMiscElement(psiElement, annotationHolder);
            }
            if (psiElement instanceof ObjJNeedsSemiColon) {
                ObjJSemiColonAnnotatorUtil.annotateMissingSemiColons((ObjJNeedsSemiColon) psiElement, annotationHolder);
            }
        } catch (IndexNotReadyRuntimeException ignored) {

        }
    }

    private static void validateMiscElement(@Nullable PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        if (element == null) {
            return;
        }
        IElementType elementType = element.getNode().getElementType();
        if (elementType == ObjJTypes.ObjJ_CONTINUE) {
            validateAndAnnotateContinueStatement(element, annotationHolder);
            return;
        }
        if (elementType == ObjJTypes.ObjJ_BREAK) {
            validateBreakStatement(element, annotationHolder);
            return;
        }
        if (elementType == ObjJTypes.ObjJ_CASE && ObjJTreeUtil.getParentOfType(element, ObjJSwitchStatement.class) == null) {
            annotationHolder.createErrorAnnotation(element, "Case statement used outside of switch statement");
            return;
        }
        if (element.getText().equals(";")) {
            ASTNode node = ObjJTreeUtil.getPreviousNonEmptyNode(element, true);
            if (node != null && node.getText().equals(";")) {
                annotationHolder.createWarningAnnotation(element, "extraneous colon");
            }
        }
    }

    private static void validateAndAnnotateContinueStatement(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        //LOGGER.log(Level.INFO, "Validating continue element");
        if (ObjJTreeUtil.getParentOfType(element, ObjJIterationStatement.class) == null) {
            annotationHolder.createErrorAnnotation(element, "Continue is used outside of loop.");
        }
    }

    private static void validateBreakStatement(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        //LOGGER.log(Level.INFO, "Validating break element");
        if (ObjJTreeUtil.getParentOfType(element, ObjJIterationStatement.class) != null ||
                ObjJTreeUtil.getParentOfType(element, ObjJCaseClause.class) != null) {
            return;
        }
        annotationHolder.createErrorAnnotation(element, "Break used outside of loop or switch statement");
    }


    private static void validateBlock(@NotNull ObjJBlock block, @NotNull AnnotationHolder annotationHolder) {
        validateBlockReturnStatements(block, annotationHolder);
    }

    private static void validateBlockReturnStatements(@NotNull ObjJBlock block, @NotNull AnnotationHolder annotationHolder) {
        List<ObjJReturnStatement> returnStatementsList = ObjJBlockPsiUtil.getBlockChildrenOfType(block, ObjJReturnStatement.class, true);
        if (returnStatementsList.isEmpty()) {
            return;
        }
        List<ObjJReturnStatement> returnsWithExpression = new ArrayList<>();
        List<ObjJReturnStatement> returnsWithoutExpression = new ArrayList<>();
        for (ObjJReturnStatement returnStatement : returnStatementsList) {
            if (returnStatement.getExpr() != null) {
                returnsWithExpression.add(returnStatement);
            } else {
                returnsWithoutExpression.add(returnStatement);
            }
        }
        ObjJMethodDeclaration methodDeclaration = block.getParentOfType(ObjJMethodDeclaration.class);
        if (methodDeclaration != null) {
            annotateBlockReturnStatements(methodDeclaration, returnsWithExpression, returnsWithoutExpression, annotationHolder);
        } else if (block.getParent() instanceof ObjJFunctionDeclarationElement) {
            annotateBlockReturnStatements(((ObjJFunctionDeclarationElement)block.getParent()), returnsWithExpression, returnsWithoutExpression, annotationHolder);
        }
    }

    private static void annotateBlockReturnStatements(@NotNull ObjJMethodDeclaration methodDeclaration,
                                                      @NotNull List<ObjJReturnStatement> returnsWithExpression,
                                                      @NotNull List<ObjJReturnStatement> returnsWithoutExpression,
                                                      @NotNull AnnotationHolder annotationHolder) {
        final String returnType = methodDeclaration.getMethodHeader().getReturnType();
        final boolean shouldHaveReturnExpression = !returnType.equals(ObjJClassType.VOID_CLASS_NAME);
        final List<ObjJReturnStatement> statementsToMark = shouldHaveReturnExpression ? returnsWithoutExpression : returnsWithExpression;
        final String errorAnnotation = shouldHaveReturnExpression ? "Return statement is missing return element. Element should be of type: <"+returnType+">" : "Method with return type void should not return a value.";
        for (ObjJReturnStatement returnStatement : statementsToMark) {
            annotationHolder.createErrorAnnotation(returnStatement, errorAnnotation);
        }
    }
    private static void annotateBlockReturnStatements(@NotNull ObjJFunctionDeclarationElement functionDeclarationElement,
                                                      @NotNull List<ObjJReturnStatement> returnsWithExpression,
                                                      @NotNull List<ObjJReturnStatement> returnsWithoutExpression,
                                                      @NotNull AnnotationHolder annotationHolder) {
        if (returnsWithExpression.size() > 0) {
            for (ObjJReturnStatement returnStatement : returnsWithoutExpression) {
                PsiElement annotationElement = functionDeclarationElement.getFunctionNameNode();
                if (annotationElement == null) {
                    annotationElement = returnStatement;
                }
                annotationHolder.createWarningAnnotation(annotationElement, "Not all return statements return a value");
            }
        }
    }

    private static void validateReturnStatement(@NotNull ObjJReturnStatement element, @NotNull AnnotationHolder annotationHolder) {
        if (element.getParentOfType(ObjJBlock.class) == null) {
            annotationHolder.createWarningAnnotation(element, "return used outside of block");
        } else if (element.getExpr() != null &&
                element.getParentOfType(ObjJFunctionDeclarationElement.class) == null &&
                element.getParentOfType(ObjJMethodDeclaration.class) == null) {
            annotationHolder.createWarningAnnotation(element, "Return value not captured");
        }
    }

}
