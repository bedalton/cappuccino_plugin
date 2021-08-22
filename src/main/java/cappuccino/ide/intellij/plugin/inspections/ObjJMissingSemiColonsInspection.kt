package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.annotator.AnnotationHolderWrapper
import cappuccino.ide.intellij.plugin.fixes.ObjJAddSemiColonIntention
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJExpr
import cappuccino.ide.intellij.plugin.psi.ObjJVisitor
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJChildrenRequireSemiColons
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNeedsSemiColon
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.psi.utils.getNextNonEmptyNodeType
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import cappuccino.ide.intellij.plugin.psi.utils.lineNumber
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

/**
 * Annotator for missing semi-colons.
 * Annotator marks ObjJNeedsSemiColon elements, if missing semi-colons
 * ObjJNeedsSemiColon must be contained in a ObjJRequiresChildSemiColons element
 * @see ObjJNeedsSemiColon
 *
 * @see ObjJChildrenRequireSemiColons
 */
class ObjJMissingSemiColonsInspection: LocalInspectionTool() {

    override fun getShortName(): String {
        return "MissingSemiColons"
    }

    override fun getDisplayName(): String {
        return ObjJBundle.message("objective-j.inspections.missing-semi-colon-inspection.display-name.text")
    }

    private val NO_SEMI_COLON_BEFORE = arrayOf(ObjJTypes.ObjJ_CLOSE_PAREN, ObjJTypes.ObjJ_OPEN_PAREN, ObjJTypes.ObjJ_OPEN_BRACE, ObjJTypes.ObjJ_CLOSE_BRACE, ObjJTypes.ObjJ_COMMA, ObjJTypes.ObjJ_CLOSE_BRACKET, ObjJTypes.ObjJ_OPEN_BRACKET, ObjJTypes.ObjJ_COLON, ObjJTypes.ObjJ_SEMI_COLON)

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitExpr(o: ObjJExpr) {
                super.visitExpr(o)
                validateAndAnnotateExprIfPreviousExpressionIsNotClosed(o, holder)
            }
            override fun visitNeedsSemiColon(element: ObjJNeedsSemiColon) {
                super.visitNeedsSemiColon(element)
                annotateMissingSemiColons(element, holder)
            }
        }
    }

    /**
     * Actual annotation method for ObjJNeedsSemiColonElements
     * @param element element to possibly annotate
     * @param annotationHolder annotation holder
     */
    fun annotateMissingSemiColons(
            element: ObjJNeedsSemiColon,
            holder: ProblemsHolder
    ) {
        //Checks whether this element actually requires a semi colon and whether it already has one
        if (!requiresSemiColon(element) || isNextElementSemiColonBlocking(element) || ObjJPsiImplUtil.eos(element)) {
            return
        }

        //Annotate element as it is missing semi-colon
        if (!didAnnotateWithErrorElement(element)) {
            doAnnotateWithAnnotationHolder(element, holder)
        }

    }

    /**
     * Try to annotate missing semi-colon error with an error element
     *
     * {NOTE} Element could not be added as PSI tree cannot be changed during annotation
     * @param element element to annotate
     * @return `true` if error element was added, `false` otherwise
     */
    private fun didAnnotateWithErrorElement(
            @Suppress("UNUSED_PARAMETER") element: ObjJNeedsSemiColon): Boolean {
        return false

        /*final PsiErrorElement errorElement = ObjJElementFactory.createSemiColonErrorElement(element.getProject());
        if (errorElement == null) {
            return false;
        }
        new WriteCommandAction.Simple(element.getProject(), element.getContainingFile()) {
            public void run() {
                element.getParent().addAfter(errorElement, element);
            }
        }.execute();
        return errorElement.getParent() == element.getParent();
        */
    }

    /**
     * Annotates missing semi-colon with the annotation holder.
     * @param element element to annotate
     * @param annotationHolder annotation holder
     */
    private fun doAnnotateWithAnnotationHolder(element: ObjJNeedsSemiColon, holder: ProblemsHolder) {
        val errorRange = TextRange.create(element.textRange.endOffset - 1, element.textRange.endOffset)
        holder.registerProblem(
            element,
            errorRange,
            ObjJBundle.message("objective-j.annotator-messages.semi-colon-annotator.missing-semi-colon.message"),
            ObjJAddSemiColonIntention(element)
        )
    }

    /**
     * Determines whether this element requires a semi-colon
     * Element needs semi-colon if has class ObjJNeedsSemiColon &&
     * direct parent is ObjJChildrenRequireSemiColons
     * @param psiElement element to check
     * @return `true` if element requires trailing semi-colon, `false` otherwise
     */
    private fun requiresSemiColon(psiElement: PsiElement?): Boolean {
        // If this element a requires semi-colon element find out if parent requires one
        return if (psiElement is ObjJNeedsSemiColon)
            psiElement.parent is ObjJChildrenRequireSemiColons
        else // Element does not need semi-colon, return false
            false
    }


    private fun validateAndAnnotateExprIfPreviousExpressionIsNotClosed(element: ObjJExpr?, holder: ProblemsHolder) {
        val previousElement = element?.getPreviousNonEmptySibling(true) as? ObjJNeedsSemiColon ?: return
        if (previousElement.lineNumber == element.lineNumber)
            return
        holder.registerProblem(
            element.containingFile,
            TextRange.create(previousElement.textRange.endOffset - 1, previousElement.textRange.endOffset),
            ObjJBundle.message("objective-j.inspections.expr-use.previous-expression-is-not-closed"),
            ObjJAddSemiColonIntention(previousElement)
        )
    }

    /**
     * Determines whether an element is within a statement that negates the need for a semi-colon. ie ')'
     * @param psiElement element to check next siblings of
     * @return `true` if next element blocks semi-colon, `false` otherwise
     */
    private fun isNextElementSemiColonBlocking(psiElement: PsiElement): Boolean {
        val nextNodeType = psiElement.getNextNonEmptyNodeType(true) ?: return false
        for (elementType in NO_SEMI_COLON_BEFORE) {
            if (nextNodeType === elementType) {
                return true
            }
        }
        return false
    }
}
