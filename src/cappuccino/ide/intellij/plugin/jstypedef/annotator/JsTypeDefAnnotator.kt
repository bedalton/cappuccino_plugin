package cappuccino.ide.intellij.plugin.jstypedef.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes
import cappuccino.ide.intellij.plugin.psi.utils.elementType

/**
 * Base annotator class. Used to filter and redirect element annotation requests
 */
class JsTypeDefAnnotator : Annotator {

    /**
     * Takes and redirects elements to the appropriate annotator
     */
    override fun annotate(
            element: PsiElement,
            annotationHolder: AnnotationHolder) {

        if (element.containingFile !is JsTypeDefFile)
            return
        // Ensure index is not dumb
        if (DumbService.getInstance(element.project).isDumb) {
            return
        }

        // Actually annotate items
        try {
            // Redirects elements to appropriate annotators
            when (element) {

            }
            // Additional pass to annotate elements needing semi-colons
            // Cannot be combines to earlier calls, as this annotation may need to run in parallel
            if (element.elementType == JsTypeDefTypes.JS_VOID) {
                JsInvalidVoidAnnotatorUtil.annotateInvalidVoidStatements(element, annotationHolder)
            }
        } catch (ignored: IndexNotReadyRuntimeException) {
            // Index was not ready, and threw exceptions due to the heavy uses of indexes in validation and annotation
        }

    }
}
