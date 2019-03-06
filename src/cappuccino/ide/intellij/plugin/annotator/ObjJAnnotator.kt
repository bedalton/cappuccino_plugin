package cappuccino.ide.intellij.plugin.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getNextSiblingOfType
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptyNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil

class ObjJAnnotator : Annotator {

    //private static final Logger LOGGER = Logger.getLogger(ObjJAnnotator.class.getName());

    override fun annotate(
            element: PsiElement,
            annotationHolder: AnnotationHolder) {
        if (DumbService.getInstance(element.project).isDumb) {
            return
        }
        //LOGGER.log(Level.INFO, "Annotating element of type: "+element.getNode().getElementType().toString());
        try {
            //Annotate Method calls
            when (element) {
                is ObjJMethodCall -> ObjJMethodCallAnnotatorUtil.annotateMethodCall(element, annotationHolder)
                is ObjJSelectorLiteral -> {
                    //ObjJMethodCallAnnotatorUtil.annotateSelectorLiteral(element, annotationHolder)
                }
                is ObjJVariableName -> {
                    // ObjJVariableAnnotatorUtil.annotateVariable(element, annotationHolder)
                }
                is ObjJImplementationDeclaration -> ObjJImplementationDeclarationAnnotatorUtil.annotateImplementationDeclaration(element, annotationHolder)
                is ObjJProtocolDeclaration -> ObjJProtocolDeclarationAnnotatorUtil.annotateProtocolDeclaration(element, annotationHolder)
                //is ObjJBlock -> validateBlock(element, annotationHolder)
                is ObjJReturnStatement -> {
                    //validateReturnStatement(element, annotationHolder)
                }
                is ObjJMethodHeader -> ObjJMethodDeclarationAnnotator.annotateMethodHeaderDeclarations(element, annotationHolder)
                is ObjJVariableDeclaration -> validateVariableDeclaration(element, annotationHolder)
                else -> validateMiscElement(element, annotationHolder)
            }
            if (element is ObjJNeedsSemiColon) {
                ObjJSemiColonAnnotatorUtil.annotateMissingSemiColons(element, annotationHolder)
            }
            if (element is ObjJFragment) {
                annotationHolder.createErrorAnnotation(element, "Invalid directive")
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
        if (hasIterationStatementParent(element)) {
            return
        }
        annotationHolder.createErrorAnnotation(element, "Continue is used outside of loop.")
    }

    private fun validateBreakStatement(element: PsiElement, annotationHolder: AnnotationHolder) {
        //LOGGER.log(Level.INFO, "Validating break element");
        if (hasIterationStatementParent(element) || element.getParentOfType( ObjJCaseClause::class.java) != null) {
            return
        }
        annotationHolder.createErrorAnnotation(element, "Break used outside of loop or switch statement")
    }

    private fun hasIterationStatementParent(element: PsiElement) : Boolean {
        return PsiTreeUtil.findFirstParent(element) {
            it is ObjJIterationStatement ||
            it is ObjJForStatement ||
            it is ObjJWhileStatement ||
            it is ObjJDoWhileStatement ||
            it is ObjJDebuggerStatement
        } != null
    }

    private fun validateVariableDeclaration(variableDeclaration: ObjJVariableDeclaration, annotationHolder: AnnotationHolder) {

        // Only check if direct parent if body variable assignment.
        // Looking for any inherited parent of caused problems with nested declarations
        val inBodyVariableAssignment:Boolean = (variableDeclaration.parent as? ObjJBodyVariableAssignment)?.varModifier != null

        for (qualifiedReference in variableDeclaration.qualifiedReferenceList) {
            // Check that method call is not being assigned to directly
            // Values can only be assigned to (.) or [array] expressions
            if (qualifiedReference.methodCall != null && qualifiedReference.qualifiedNameParts.isEmpty()) {
                annotationHolder.createErrorAnnotation(qualifiedReference.getNextSiblingOfType(ObjJTypes.ObjJ_EQUALS)?:qualifiedReference, "Cannot assign value to method call")
                return
            }
            // Check that there is not a qualified reference in a 'var' declaration
            if (inBodyVariableAssignment && qualifiedReference.qualifiedNameParts.size > 1) {
                val textRange:TextRange
                val firstDot = qualifiedReference.firstChild.getNextSiblingOfType(ObjJTypes.ObjJ_DOT)
                if (firstDot != null) {
                    textRange = firstDot.textRange
                } else {
                    val startOffsetTemp = qualifiedReference.firstChild.getNextSiblingOfType(ObjJTypes.ObjJ_DOT)?.textRange?.startOffset
                            ?: qualifiedReference.qualifiedNameParts.getOrNull(1)?.textRange?.startOffset
                    val startOffset: Int
                    startOffset = if (startOffsetTemp != null) {
                        startOffsetTemp - 1
                    } else {
                        qualifiedReference.textRange.startOffset
                    }
                    textRange = TextRange.create(startOffset, variableDeclaration.textRange.endOffset)
                }
                annotationHolder.createErrorAnnotation(textRange, "Cannot use qualified reference with 'var' assignment keyword")
                return
            }
            // Check that the last part of a qualified name is not a function call
            // as these cannot be assigned to
            val lastChild = qualifiedReference.qualifiedNameParts.last() ?: return
            if (lastChild is ObjJFunctionCall) {
                annotationHolder.createErrorAnnotation(TextRange(lastChild.textRange.startOffset, variableDeclaration.textRange.endOffset), "Cannot assign value to function call")
            }
        }
    }
}
