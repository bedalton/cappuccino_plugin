package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import cappuccino.ide.intellij.plugin.fixes.ObjJAddSemiColonIntention
import cappuccino.ide.intellij.plugin.fixes.ObjJRemoveSemiColonIntention
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJIterationStatement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNeedsSemiColon
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptyNode
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

/**
 * Base annotator class. Used to filter and redirect element annotation requests
 */
class ObjJAnnotator : Annotator {

    /**
     * Takes and redirects elements to the appropriate annotator
     */
    override fun annotate(
            element: PsiElement,
            annotationHolder: AnnotationHolder) {

        if (element.containingFile !is ObjJFile)
            return
        // Ensure index is not dumb
        if (DumbService.getInstance(element.project).isDumb) {
            return
        }

        // Actually annotate items
        try {
            // Redirects elements to appropriate annotators
            when (element) {
                is ObjJMethodCall -> ObjJMethodCallAnnotatorUtil.annotateMethodCall(element, annotationHolder)
                is ObjJImplementationDeclaration -> ObjJImplementationDeclarationAnnotatorUtil.annotateImplementationDeclaration(element, annotationHolder)
                is ObjJProtocolDeclaration -> ObjJProtocolDeclarationAnnotatorUtil.annotateProtocolDeclaration(element, annotationHolder)
                is ObjJMethodHeader -> ObjJMethodDeclarationAnnotator.annotateMethodHeaderDeclarations(element, annotationHolder)
                is ObjJVariableDeclaration -> ObjJVariableDeclarationAnnotator.annotateVariableDeclarations(element, annotationHolder)
                is ObjJFragment -> annotationHolder.newAnnotation(HighlightSeverity.ERROR, "Invalid directive").range(element).create()
                else -> validateMiscElement(element, annotationHolder)
            }
            // Additional pass to annotate elements needing semi-colons
            // Cannot be combines to earlier calls, as this annotation may need to run in parallel
            if (element is ObjJNeedsSemiColon) {
                ObjJSemiColonAnnotatorUtil.annotateMissingSemiColons(element, annotationHolder)
            }
        } catch (ignored: IndexNotReadyRuntimeException) {
            // Index was not ready, and threw exceptions due to the heavy uses of indexes in validation and annotation
        }

    }

    /**
     * Validates miscellaneous keyword and token elements for given cases.
     */
    private fun validateMiscElement(element: PsiElement?, annotationHolder: AnnotationHolder) {
        if (element == null) {
            return
        }
        when (element.node?.elementType) {
            ObjJTypes.ObjJ_EXPR -> return validateAndAnnotateExprIfPreviousExpressionIsNotClosed(element as? ObjJExpr, annotationHolder)
            ObjJTypes.ObjJ_CONTINUE -> return validateAndAnnotateInvalidContinueStatement(element, annotationHolder)
            ObjJTypes.ObjJ_BREAK -> return validateAndAnnotateInvalidBreakStatement(element, annotationHolder)
            ObjJTypes.ObjJ_CASE -> return validateAndAnnotateInvalidCaseStatement(element, annotationHolder)
            ObjJTypes.ObjJ_SEMI_COLON -> return validateAndAnnotateRedundantSemiColon(element, annotationHolder)
        }
    }

    private fun validateAndAnnotateExprIfPreviousExpressionIsNotClosed(element: ObjJExpr?, annotationHolder: AnnotationHolder) {
        val previousElement = element
                ?.getPreviousNonEmptySibling(true) as? ObjJNeedsSemiColon
                ?: return
        val messageKey = "objective-j.inspections.expr-use.previous-expression-is-not-closed"
        val message = ObjJBundle.message(messageKey)
        val annotation = annotationHolder
                .newAnnotation(HighlightSeverity.ERROR, message)
                .range(TextRange.create(
                        previousElement.textRange.endOffset - 1,
                        previousElement.textRange.endOffset
                ))
                .withFix(ObjJAddSemiColonIntention(previousElement))
                .create()
    }

    /**
     * Validates 'continue' statements to ensure they are used in context of a loop
     */
    private fun validateAndAnnotateInvalidContinueStatement(element: PsiElement, annotationHolder: AnnotationHolder) {
        if (hasIterationStatementParent(element)) {
            return
        }
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, "Continue is used outside of loop.")
                .range(element)
                .create()
    }

    /**
     * Validates break statements to ensure they are used appropriately
     */
    private fun validateAndAnnotateInvalidBreakStatement(element: PsiElement, annotationHolder: AnnotationHolder) {
        if (hasIterationStatementParent(element) || element.getParentOfType(ObjJCaseClause::class.java) != null) {
            return
        }
        val message = "Break used outside of loop or switch statement";
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, message)
                .range(element)
                .create()
    }

    /**
     * Validates 'case' statements and ensures they are used correctly
     */
    private fun validateAndAnnotateInvalidCaseStatement(element: PsiElement, annotationHolder: AnnotationHolder) {
        if (element.getParentOfType(ObjJSwitchStatement::class.java) != null)
            return
        annotationHolder.newAnnotation(HighlightSeverity.ERROR,
                "Case statement used outside of switch statement")
                .range(element)
                .create()
    }

    /**
     * Validates semi-colons to ensure they are not redundant
     */
    private fun validateAndAnnotateRedundantSemiColon(element: PsiElement, annotationHolder: AnnotationHolder) {
        val previousNode = element.getPreviousNonEmptyNode(true) ?: return
        if (previousNode.elementType != ObjJTypes.ObjJ_SEMI_COLON)
            return
        annotationHolder.newAnnotation(HighlightSeverity.WARNING, "extraneous colon")
                .range(element)
                .withFix(ObjJRemoveSemiColonIntention(element))
                .create()
    }

    /**
     * Helper method to determine whether a given element is contained in an iteration statement
     */
    private fun hasIterationStatementParent(element: PsiElement): Boolean {
        return PsiTreeUtil.findFirstParent(element) {
            it is ObjJIterationStatement ||
                    it is ObjJForStatement ||
                    it is ObjJWhileStatement ||
                    it is ObjJDoWhileStatement ||
                    it is ObjJDebuggerStatement
        } != null
    }

}
