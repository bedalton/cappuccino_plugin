package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.indices.ObjJClassMethodIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService

/**
 * Annotator/Validator for method declarations
 */
object ObjJMethodDeclarationAnnotator {

    /**
     * Annotates a method header declaration
     */
    fun annotateMethodHeaderDeclarations(methodHeader: ObjJMethodHeader, annotationHolder: AnnotationHolderWrapper) {
        annotateDuplicateSelectors(methodHeader, annotationHolder)
    }

    /**
     * Annotates duplicate method headers
     */
    private fun annotateDuplicateSelectors(methodHeader: ObjJMethodHeader, annotationHolder: AnnotationHolderWrapper) {
        val project = methodHeader.project
        val thisSelector = methodHeader.selectorString
        val containingClass = methodHeader.containingClass ?: return
        val containingClassName = containingClass.classNameString
        Logger.getInstance(ObjJMethodDeclarationAnnotator::class.java).assertTrue(!DumbService.isDumb(project))
        for (classMethodHeader in ObjJClassMethodIndex.instance[containingClassName, project]) {
            // If containing class elements are not equivalent, they should not be tested
            // As they are indeed different definitions
            if (!containingClass.isEquivalentTo(classMethodHeader.containingClass)) {
                continue
            }
            // If a given method is the same element in psi file, ignore
            if (classMethodHeader.isEquivalentTo(methodHeader)) {
                continue
            }
            // Check if method selectors can be considered duplicates or not.
            if (classMethodHeader.selectorString == thisSelector && ObjJMethodPsiUtils.hasSimilarDisposition(methodHeader, classMethodHeader as? ObjJMethodHeader)) {
                annotationHolder.newErrorAnnotation(ObjJBundle.message("objective-j.annotator-messages.method-declaration-annotator.duplicate-selector.message"))
                        .range(methodHeader)
                        .create()
                return
            }
        }
    }

}
