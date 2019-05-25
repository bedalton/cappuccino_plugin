package cappuccino.ide.intellij.plugin.jstypedef.annotator

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefBundle
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunctionReturnType
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefValueOfKeyType
import com.intellij.lang.annotation.AnnotationHolder
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import com.intellij.lang.annotation.HighlightSeverity

/**
 * Annotates invalid use of map return type
 */
internal fun annotateInvalidMapReturnType(
        element: JsTypeDefValueOfKeyType,
        annotationHolder: AnnotationHolder) {
    if (element.hasParentOfType(JsTypeDefFunctionReturnType::class.java))
        return
    annotationHolder.createAnnotation(HighlightSeverity.ERROR, element.textRange, JsTypeDefBundle.message("jstypedef.invalid-map-return-type-usage.message"))
}