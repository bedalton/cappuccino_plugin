package cappuccino.ide.intellij.plugin.jstypedef.annotator

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefBundle
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefKeyOfType
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefNoKeyOfTypes
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJChildrenRequireSemiColons
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNeedsSemiColon
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity

/**
 * Annotates KeyOf elements that are used outside of context
 */
internal fun annotateInvalidKeyOfUsage(
        element: JsTypeDefKeyOfType,
        annotationHolder: AnnotationHolder) {
    if (!element.hasParentOfType(JsTypeDefNoKeyOfTypes::class.java))
        return
    annotationHolder.createAnnotation(HighlightSeverity.ERROR, element.textRange, JsTypeDefBundle.message("jstypedef.invalid-keyof-usage.message"))
}