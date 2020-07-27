package cappuccino.ide.intellij.plugin.jstypedef.annotator

import cappuccino.ide.intellij.plugin.annotator.AnnotationHolderWrapper
import cappuccino.ide.intellij.plugin.jstypedef.fixes.RemoveElementInPipedListFix
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefBundle
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefGenericTypeTypes
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefNoVoid
import cappuccino.ide.intellij.plugin.psi.utils.hasAnyParentOfType
import com.intellij.psi.PsiElement

/**
 * Annotates invalid use of void keyword
 */
internal fun annotateInvalidVoidStatements(
        element: PsiElement,
        annotationHolder: AnnotationHolderWrapper) {
    val hasNonType = element.hasAnyParentOfType(
            JsTypeDefNoVoid::class.java,
            JsTypeDefGenericTypeTypes::class.java
    )
    if (!hasNonType)
        return
    annotationHolder
            .newErrorAnnotation(JsTypeDefBundle.message("jstypedef.annotation.error.invalid-void.message"))
            .range(element.textRange)
            .withFix(RemoveElementInPipedListFix(
                    element,
                    JsTypeDefBundle.message("jstypedef.annotation.error.invalid-void.quick-fix.remove-void.message"))
            )
            .create()
}