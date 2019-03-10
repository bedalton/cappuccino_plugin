package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.indices.ObjJClassMethodIndex
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.lang.annotation.AnnotationHolder

object ObjJMethodDeclarationAnnotator {

    fun annotateMethodHeaderDeclarations(methodHeader: ObjJMethodHeader, annotationHolder: AnnotationHolder) {
        val project = methodHeader.project
        val thisSelector = methodHeader.selectorString
        val containingClass = methodHeader.containingClass ?: return
        val containingClassName = containingClass.getClassNameString()
        Logger.getInstance(ObjJMethodDeclarationAnnotator::class.java).assertTrue(!DumbService.isDumb(project))
        for (classMethodHeader in ObjJClassMethodIndex.instance[containingClassName, project]) {
            if (!containingClass.isEquivalentTo(classMethodHeader.containingClass)) {
                continue
            }
            if (classMethodHeader.isEquivalentTo(methodHeader)) {
                continue
            }
            if (classMethodHeader.selectorString == thisSelector && !isDifferentWhileSimilar(methodHeader, classMethodHeader)) {
                annotationHolder.createErrorAnnotation(methodHeader, "Duplicate method selector in class")
                return
            }
        }
    }

    private fun isDifferentWhileSimilar(thisHeader: ObjJMethodHeader, otherHeader:ObjJMethodHeader) : Boolean
    {
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
        return (thisSelector.methodHeaderSelectorFormalVariableType == null && otherSelector.methodHeaderSelectorFormalVariableType != null) ||
                (thisSelector.methodHeaderSelectorFormalVariableType != null && otherSelector.methodHeaderSelectorFormalVariableType == null)
    }
}
