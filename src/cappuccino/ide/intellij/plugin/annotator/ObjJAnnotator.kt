package cappuccino.ide.intellij.plugin.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptyNode
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
                is ObjJImplementationDeclaration -> ObjJImplementationDeclarationAnnotatorUtil.annotateImplementationDeclaration(element, annotationHolder)
                is ObjJProtocolDeclaration -> ObjJProtocolDeclarationAnnotatorUtil.annotateProtocolDeclaration(element, annotationHolder)
                is ObjJMethodHeader -> ObjJMethodDeclarationAnnotator.annotateMethodHeaderDeclarations(element, annotationHolder)
                is ObjJVariableDeclaration -> ObjJVariableDeclarationAnnotator.annotateVariableDeclarations(element, annotationHolder)
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

}
