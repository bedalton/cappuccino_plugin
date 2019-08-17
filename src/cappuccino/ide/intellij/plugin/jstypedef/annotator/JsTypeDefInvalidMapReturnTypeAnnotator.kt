package cappuccino.ide.intellij.plugin.jstypedef.annotator

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefBundle
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefAnonymousFunction
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunctionReturnType
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefValueOfKeyType
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity

/**
 * Annotates invalid use of map return type
 */
internal fun annotateInvalidMapReturnType(
        element: JsTypeDefValueOfKeyType,
        annotationHolder: AnnotationHolder) {
    if (element.hasParentOfType(JsTypeDefFunctionReturnType::class.java) || element.hasParentOfType(JsTypeDefAnonymousFunction::class.java))
        return
    annotationHolder.createAnnotation(HighlightSeverity.ERROR, element.textRange, JsTypeDefBundle.message("jstypedef.annotation.error.invalid-map-return-type-usage.message"))
}