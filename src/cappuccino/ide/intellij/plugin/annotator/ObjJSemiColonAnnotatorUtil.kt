package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.fixes.ObjJAddSemiColonIntention
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJChildrenRequireSemiColons
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNeedsSemiColon
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.psi.utils.getNextNonEmptyNodeType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

/**
 * Annotator for missing semi-colons.
 * Annotator marks ObjJNeedsSemiColon elements, if missing semi-colons
 * ObjJNeedsSemiColon must be contained in a ObjJRequiresChildSemiColons element
 * @see ObjJNeedsSemiColon
 *
 * @see ObjJChildrenRequireSemiColons
 */
internal object ObjJSemiColonAnnotatorUtil {

    private val NO_SEMI_COLON_BEFORE = arrayOf(ObjJTypes.ObjJ_CLOSE_PAREN, ObjJTypes.ObjJ_OPEN_PAREN, ObjJTypes.ObjJ_OPEN_BRACE, ObjJTypes.ObjJ_CLOSE_BRACE, ObjJTypes.ObjJ_COMMA, ObjJTypes.ObjJ_CLOSE_BRACKET, ObjJTypes.ObjJ_OPEN_BRACKET, ObjJTypes.ObjJ_COLON, ObjJTypes.ObjJ_SEMI_COLON)


    /**
     * Actual annotation method for ObjJNeedsSemiColonElements
     * @param element element to possibly annotate
     * @param annotationHolder annotation holder
     */
    fun annotateMissingSemiColons(
            element: ObjJNeedsSemiColon,
            annotationHolder: AnnotationHolderWrapper) {
        //Checks whether this element actually requires a semi colon and whether it already has one
        if (!requiresSemiColon(element) || isNextElementSemiColonBlocking(element) || ObjJPsiImplUtil.eos(element)) {
            return
        }

        //Annotate element as it is missing semi-colon
        if (!didAnnotateWithErrorElement(element)) {
            doAnnotateWithAnnotationHolder(element, annotationHolder)
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
    private fun doAnnotateWithAnnotationHolder(element: ObjJNeedsSemiColon, annotationHolder: AnnotationHolderWrapper) {
        val errorRange = TextRange.create(element.textRange.endOffset - 1, element.textRange.endOffset)
        annotationHolder.newErrorAnnotation(ObjJBundle.message("objective-j.annotator-messages.semi-colon-annotator.missing-semi-colon.message"))
                .range(errorRange)
                .withFix(ObjJAddSemiColonIntention(element))
                .create()
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
