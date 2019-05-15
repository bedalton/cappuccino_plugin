package cappuccino.ide.intellij.plugin.jstypedef.annotator

import cappuccino.ide.intellij.plugin.jstypedef.fixes.RemoveElementFix
import cappuccino.ide.intellij.plugin.jstypedef.fixes.RemoveElementInPipedListFix
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefBundle
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefHasVoid
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefNoNull
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefNoVoid
import com.intellij.lang.annotation.AnnotationHolder
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNeedsSemiColon
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJChildrenRequireSemiColons
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

/**
 * Annotator for missing semi-colons.
 * Annotator marks ObjJNeedsSemiColon elements, if missing semi-colons
 * ObjJNeedsSemiColon must be contained in a ObjJRequiresChildSemiColons element
 * @see ObjJNeedsSemiColon
 *
 * @see ObjJChildrenRequireSemiColons
 */
internal object JsInvalidVoidAnnotatorUtil {
    /**
     * Actual annotation method for ObjJNeedsSemiColonElements
     * @param element element to possibly annotate
     * @param annotationHolder annotation holder
     */
    fun annotateInvalidVoidStatements (
            element: PsiElement,
            annotationHolder: AnnotationHolder) {
        if (!element.hasParentOfType(JsTypeDefNoVoid::class.java))
            return
        val annotation = annotationHolder.createAnnotation(HighlightSeverity.ERROR, element.textRange, JsTypeDefBundle.message("jstypedef.invalid-void.message"))
        annotation.registerFix(RemoveElementInPipedListFix(element, JsTypeDefBundle.message("jstypedef.invalid-void.quick-fix.remove-void.message")))
    }

}
