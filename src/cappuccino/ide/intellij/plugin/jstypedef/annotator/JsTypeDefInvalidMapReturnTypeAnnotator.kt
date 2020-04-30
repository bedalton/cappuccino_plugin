package cappuccino.ide.intellij.plugin.jstypedef.annotator

import cappuccino.ide.intellij.plugin.annotator.newAnnotationBuilder
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefBundle
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefAnonymousFunction
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunctionReturnType
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefValueOfKeyType
import cappuccino.ide.intellij.plugin.psi.utils.hasAnyParentOfType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity

/**
 * Annotates invalid use of map return type
 */
internal fun annotateInvalidMapReturnType(
        element: JsTypeDefValueOfKeyType,
        annotationHolder: AnnotationHolder) {
    val hasFunctionParent = element.hasAnyParentOfType(
            JsTypeDefFunctionReturnType::class.java,
            JsTypeDefAnonymousFunction::class.java
    )
    if (hasFunctionParent) {
        return
    }
    val messageKey = "jstypedef.annotation.error.invalid-map-return-type-usage.message"
    val message = JsTypeDefBundle.message(messageKey)
    annotationHolder.newAnnotationBuilder(HighlightSeverity.ERROR, message)
            .range(element.textRange)
            .create()
}