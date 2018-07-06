package cappuccino.ide.intellij.plugin.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNeedsSemiColon
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptyNode

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
            if (psiElement is ObjJFragment) {
                annotationHolder.createErrorAnnotation(psiElement, "invalid directive")
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
        if (elementType === ObjJTypes.ObjJ_CASE && element.getParentOfType( ObjJSwitchStatement::class.java) == null) {
            annotationHolder.createErrorAnnotation(element, "Case statement used outside of switch statement")
            return
        }
        if (element.text == ";") {
            val node = element.getPreviousNonEmptyNode(true)
            if (node != null && node.text == ";") {
                annotationHolder.createWarningAnnotation(element, "extraneous colon")
            }
        }
    }

    private fun validateAndAnnotateContinueStatement(element: PsiElement, annotationHolder: AnnotationHolder) {
        //LOGGER.log(Level.INFO, "Validating continue element");
        if (element.getParentOfType( ObjJIterationStatement::class.java) == null) {
            annotationHolder.createErrorAnnotation(element, "Continue is used outside of loop.")
        }
    }

    private fun validateBreakStatement(element: PsiElement, annotationHolder: AnnotationHolder) {
        //LOGGER.log(Level.INFO, "Validating break element");
        if (element.getParentOfType( ObjJIterationStatement::class.java) != null || element.getParentOfType( ObjJCaseClause::class.java) != null) {
            return
        }
        annotationHolder.createErrorAnnotation(element, "Break used outside of loop or switch statement")
    }


    private fun validateBlock(block: ObjJBlock, annotationHolder: AnnotationHolder) {
        validateBlockReturnStatements(block, annotationHolder)
    }

    private fun validateBlockReturnStatements(block: ObjJBlock, annotationHolder: AnnotationHolder) {
        val returnStatementsList = block.getBlockChildrenOfType(ObjJReturnStatement::class.java, true)
        if (returnStatementsList.isEmpty()) {
            return
        }
        val isFunction:Boolean = block.getParentOfType(ObjJFunctionDeclarationElement::class.java) != null
        val isMethod = block is ObjJMethodBlock
        if (!isFunction && !isMethod) {
            return
        }
        val returnsWithExpression = ArrayList<ObjJReturnStatement>()
        val returnsWithoutExpression = ArrayList<ObjJReturnStatement>()
        for (returnStatement in returnStatementsList) {
            if (isFunction) {
                if(returnStatement.getParentOfType(ObjJFunctionDeclarationElement::class.java) == null) {
                    continue
                }
            } else if (isMethod) {
                if (returnStatement.getParentOfType(ObjJMethodBlock::class.java) == null) {
                    continue
                }
            }
            if (returnStatement.expr != null) {
                returnsWithExpression.add(returnStatement)
            } else {
                returnsWithoutExpression.add(returnStatement)
            }
        }
        val methodDeclaration = block.getParentOfType(ObjJMethodDeclaration::class.java)
        if (isFunction) {
            annotateBlockReturnStatements(returnsWithExpression, returnsWithoutExpression, annotationHolder)
        } else if (methodDeclaration != null) {
            annotateBlockReturnStatements(methodDeclaration, returnsWithExpression, returnsWithoutExpression, annotationHolder)
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
            if (returnStatement.expr?.leftExpr?.functionCall != null) {
                continue
            }
            annotationHolder.createWarningAnnotation(returnStatement.expr ?: returnStatement.`return`, errorAnnotation)
        }
    }

    private fun annotateBlockReturnStatements(returnsWithExpression: List<ObjJReturnStatement>,
                                              returnsWithoutExpression: List<ObjJReturnStatement>,
                                              annotationHolder: AnnotationHolder) {
        if (returnsWithExpression.isNotEmpty()) {
            for (returnStatement in returnsWithoutExpression) {
                //var annotationElement: PsiElement? = functionDeclarationElement.functionNameNode
                annotationHolder.createWarningAnnotation(returnStatement.expr ?: returnStatement.`return`, "Not all return statements return a value")
            }
        }
    }

    private fun validateReturnStatement(element: ObjJReturnStatement, annotationHolder: AnnotationHolder) {
        if (element.getParentOfType(ObjJBlock::class.java) == null) {
            annotationHolder.createWarningAnnotation(element, "return used outside of block")
        } else if (element.expr != null &&
                element.getParentOfType(ObjJFunctionDeclarationElement::class.java) == null &&
                element.getParentOfType(ObjJMethodDeclaration::class.java) == null) {
            annotationHolder.createWarningAnnotation(element, "Return value not captured")
        }
    }

}
