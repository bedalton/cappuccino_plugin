package org.cappuccino_project.ide.intellij.plugin.annotator

import com.intellij.lang.ASTNode
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJNeedsSemiColon
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJBlockPsiUtil
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil

import java.util.ArrayList

class ObjJAnnotator : Annotator {

    //private static final Logger LOGGER = Logger.getLogger(ObjJAnnotator.class.getName());

    override fun annotate(
            psiElement: PsiElement,
            annotationHolder: AnnotationHolder) {
        if (DumbService.getInstance(psiElement.project).isDumb) {
            return
        }
        //LOGGER.log(Level.INFO, "Annotating element of type: "+psiElement.getNode().getElementType().toString());
        try {
            //Annotate Method calls
            if (psiElement is ObjJMethodCall) {
                ObjJMethodCallAnnotatorUtil.annotateMethodCall(psiElement, annotationHolder)
            } else if (psiElement is ObjJSelectorLiteral) {
                ObjJMethodCallAnnotatorUtil.annotateSelectorLiteral(psiElement, annotationHolder)
            } else if (psiElement is ObjJVariableName) {
                ObjJVariableAnnotatorUtil.annotateVariable(psiElement, annotationHolder)
            } else if (psiElement is ObjJImplementationDeclaration) {
                ObjJImplementationDeclarationAnnotatorUtil.annotateImplementationDeclaration(psiElement, annotationHolder)
            } else if (psiElement is ObjJProtocolDeclaration) {
                ObjJProtocolDeclarationAnnotatorUtil.annotateProtocolDeclaration(psiElement, annotationHolder)
            } else if (psiElement is ObjJBlock) {
                validateBlock(psiElement, annotationHolder)
            } else if (psiElement is ObjJReturnStatement) {
                validateReturnStatement(psiElement, annotationHolder)
            } else {
                validateMiscElement(psiElement, annotationHolder)
            }
            if (psiElement is ObjJNeedsSemiColon) {
                ObjJSemiColonAnnotatorUtil.annotateMissingSemiColons(psiElement, annotationHolder)
            }
        } catch (ignored: IndexNotReadyRuntimeException) {
        }

    }

    private fun validateMiscElement(element: PsiElement?, annotationHolder: AnnotationHolder) {
        if (element == null) {
            return
        }
        val elementType = element.node.elementType
        if (elementType === ObjJTypes.ObjJ_CONTINUE) {
            validateAndAnnotateContinueStatement(element, annotationHolder)
            return
        }
        if (elementType === ObjJTypes.ObjJ_BREAK) {
            validateBreakStatement(element, annotationHolder)
            return
        }
        if (elementType === ObjJTypes.ObjJ_CASE && ObjJTreeUtil.getParentOfType(element, ObjJSwitchStatement::class.java) == null) {
            annotationHolder.createErrorAnnotation(element, "Case statement used outside of switch statement")
            return
        }
        if (element.text == ";") {
            val node = ObjJTreeUtil.getPreviousNonEmptyNode(element, true)
            if (node != null && node.text == ";") {
                annotationHolder.createWarningAnnotation(element, "extraneous colon")
            }
        }
    }

    private fun validateAndAnnotateContinueStatement(element: PsiElement, annotationHolder: AnnotationHolder) {
        //LOGGER.log(Level.INFO, "Validating continue element");
        if (ObjJTreeUtil.getParentOfType(element, ObjJIterationStatement::class.java) == null) {
            annotationHolder.createErrorAnnotation(element, "Continue is used outside of loop.")
        }
    }

    private fun validateBreakStatement(element: PsiElement, annotationHolder: AnnotationHolder) {
        //LOGGER.log(Level.INFO, "Validating break element");
        if (ObjJTreeUtil.getParentOfType(element, ObjJIterationStatement::class.java) != null || ObjJTreeUtil.getParentOfType(element, ObjJCaseClause::class.java) != null) {
            return
        }
        annotationHolder.createErrorAnnotation(element, "Break used outside of loop or switch statement")
    }


    private fun validateBlock(block: ObjJBlock, annotationHolder: AnnotationHolder) {
        validateBlockReturnStatements(block, annotationHolder)
    }

    private fun validateBlockReturnStatements(block: ObjJBlock, annotationHolder: AnnotationHolder) {
        val returnStatementsList = ObjJBlockPsiUtil.getBlockChildrenOfType(block, ObjJReturnStatement::class.java, true)
        if (returnStatementsList.isEmpty()) {
            return
        }
        val returnsWithExpression = ArrayList<ObjJReturnStatement>()
        val returnsWithoutExpression = ArrayList<ObjJReturnStatement>()
        for (returnStatement in returnStatementsList) {
            if (returnStatement.expr != null) {
                returnsWithExpression.add(returnStatement)
            } else {
                returnsWithoutExpression.add(returnStatement)
            }
        }
        val methodDeclaration = block.getParentOfType(ObjJMethodDeclaration::class.java)
        if (methodDeclaration != null) {
            annotateBlockReturnStatements(methodDeclaration, returnsWithExpression, returnsWithoutExpression, annotationHolder)
        } else if (block.parent is ObjJFunctionDeclarationElement<*>) {
            annotateBlockReturnStatements(block.parent as ObjJFunctionDeclarationElement<*>, returnsWithExpression, returnsWithoutExpression, annotationHolder)
        }
    }

    private fun annotateBlockReturnStatements(methodDeclaration: ObjJMethodDeclaration,
                                              returnsWithExpression: List<ObjJReturnStatement>,
                                              returnsWithoutExpression: List<ObjJReturnStatement>,
                                              annotationHolder: AnnotationHolder) {
        val returnType = methodDeclaration.methodHeader.returnType
        val shouldHaveReturnExpression = returnType != ObjJClassType.VOID_CLASS_NAME
        val statementsToMark = if (shouldHaveReturnExpression) returnsWithoutExpression else returnsWithExpression
        val errorAnnotation = if (shouldHaveReturnExpression) "Return statement is missing return element. Element should be of type: <$returnType>" else "Method with return type void should not return a value."
        for (returnStatement in statementsToMark) {
            annotationHolder.createErrorAnnotation(returnStatement, errorAnnotation)
        }
    }

    private fun annotateBlockReturnStatements(functionDeclarationElement: ObjJFunctionDeclarationElement<*>,
                                              returnsWithExpression: List<ObjJReturnStatement>,
                                              returnsWithoutExpression: List<ObjJReturnStatement>,
                                              annotationHolder: AnnotationHolder) {
        if (returnsWithExpression.size > 0) {
            for (returnStatement in returnsWithoutExpression) {
                var annotationElement: PsiElement? = functionDeclarationElement.functionNameNode
                if (annotationElement == null) {
                    annotationElement = returnStatement
                }
                annotationHolder.createWarningAnnotation(annotationElement, "Not all return statements return a value")
            }
        }
    }

    private fun validateReturnStatement(element: ObjJReturnStatement, annotationHolder: AnnotationHolder) {
        if (element.getParentOfType(ObjJBlock::class.java) == null) {
            annotationHolder.createWarningAnnotation(element, "return used outside of block")
        } else if (element.expr != null &&
                element.getParentOfType(ObjJFunctionDeclarationElement<*>::class.java) == null &&
                element.getParentOfType(ObjJMethodDeclaration::class.java) == null) {
            annotationHolder.createWarningAnnotation(element, "Return value not captured")
        }
    }

}
