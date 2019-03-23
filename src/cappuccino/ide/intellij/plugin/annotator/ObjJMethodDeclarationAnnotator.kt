package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.indices.ObjJClassMethodIndex
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.lang.annotation.AnnotationHolder

/**
 * Annotator/Validator for method declarations
 */
object ObjJMethodDeclarationAnnotator {

    /**
     * Annotates a method header declaration
     */
    fun annotateMethodHeaderDeclarations(methodHeader: ObjJMethodHeader, annotationHolder: AnnotationHolder) {
        annotateDuplicateSelectors(methodHeader, annotationHolder)
    }

    /**
     * Annotates duplicate method headers
     */
    private fun annotateDuplicateSelectors(methodHeader: ObjJMethodHeader, annotationHolder: AnnotationHolder) {
        val project = methodHeader.project
        val thisSelector = methodHeader.selectorString
        val containingClass = methodHeader.containingClass ?: return
        val containingClassName = containingClass.getClassNameString()
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
            if (classMethodHeader.selectorString == thisSelector && !isDifferentWhileSimilar(methodHeader, classMethodHeader)) {
                annotationHolder.createErrorAnnotation(methodHeader, "Duplicate method selector in class")
                return
            }
        }
    }

    /**
     * Determines whether two methods in the same class are truly different.
     * This is due to overlaps of static and instnace method selectors
     * And also with single selector methods where one has a parameter and the other does not
     */
    private fun isDifferentWhileSimilar(thisHeader: ObjJMethodHeader, otherHeader:ObjJMethodHeader) : Boolean
    {
        // If one method is static, while another is an instance method, ignore
        if (thisHeader.methodScope != otherHeader.methodScope) {
            return true
        }
        // If Selector lengths are greater than one, then they are indeed overriding duplicated
        // Only single selector method headers can be different with same selectors
        // If one has a parameter and the other does not
        if (thisHeader.selectorList.size > 1) {
            return false
        }
        val thisSelector = thisHeader.methodDeclarationSelectorList.getOrNull(0) ?: return true
        val otherSelector = otherHeader.methodDeclarationSelectorList.getOrNull(0) ?: return true

        // Return different if one selector has a parameter, and the other does not
        return (thisSelector.methodHeaderSelectorFormalVariableType == null && otherSelector.methodHeaderSelectorFormalVariableType != null) ||
                (thisSelector.methodHeaderSelectorFormalVariableType != null && otherSelector.methodHeaderSelectorFormalVariableType == null)
    }
}
