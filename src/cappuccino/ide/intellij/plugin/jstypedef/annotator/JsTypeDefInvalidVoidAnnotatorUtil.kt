package cappuccino.ide.intellij.plugin.jstypedef.annotator

import cappuccino.ide.intellij.plugin.jstypedef.fixes.RemoveElementInPipedListFix
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefBundle
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefGenericTypeTypes
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefNoVoid
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement

/**
 * Annotates invalid use of void keyword
 */
internal fun annotateInvalidVoidStatements (
            element: PsiElement,
            annotationHolder: AnnotationHolder) {
    if (!element.hasParentOfType(JsTypeDefNoVoid::class.java) || element.hasParentOfType(JsTypeDefGenericTypeTypes::class.java))
        return
    val annotation = annotationHolder.createAnnotation(HighlightSeverity.ERROR, element.textRange, JsTypeDefBundle.message("jstypedef.annotation.error.invalid-void.message"))
    annotation.registerFix(RemoveElementInPipedListFix(element, JsTypeDefBundle.message("jstypedef.annotation.error.invalid-void.quick-fix.remove-void.message")))
}