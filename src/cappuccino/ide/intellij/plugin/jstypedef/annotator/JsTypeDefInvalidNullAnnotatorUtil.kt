package cappuccino.ide.intellij.plugin.jstypedef.annotator

import cappuccino.ide.intellij.plugin.jstypedef.fixes.RemoveElementInPipedListFix
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefBundle
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefNoVoid
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

/**
 * Annotator for invalid use of null keyword
 */
internal fun annotateInvalidNullStatement (
            element: PsiElement,
            annotationHolder: AnnotationHolder) {
    if (!element.hasParentOfType(JsTypeDefNoVoid::class.java))
        return
    val message = JsTypeDefBundle.message("jstypedef.annotation.error.invalid-null.message")
    val annotation = annotationHolder
            .newAnnotation(HighlightSeverity.ERROR, message)
            .range(element.textRange)
            .withFix(RemoveElementInPipedListFix(element, JsTypeDefBundle.message("jstypedef.annotation.error.invalid-null.quick-fix.remove-void.message")))
            .create()
}
