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
 * Annotator for invalid use of null.
 */
internal object JsInvalidNullAnnotatorUtil {
    /**
     * Actual annotation method for for invalid null items
     */
    fun annotateInvalidNullStatements (
            element: PsiElement,
            annotationHolder: AnnotationHolder) {
        if (!element.hasParentOfType(JsTypeDefNoVoid::class.java))
            return
        val annotation = annotationHolder.createAnnotation(HighlightSeverity.ERROR, element.textRange, JsTypeDefBundle.message("jstypedef.invalid-null.message"))
        annotation.registerFix(RemoveElementInPipedListFix(element, JsTypeDefBundle.message("jstypedef.invalid-null.quick-fix.remove-void.message")))
    }

}
