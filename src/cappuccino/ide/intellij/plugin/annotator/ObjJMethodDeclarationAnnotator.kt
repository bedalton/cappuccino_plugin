package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.indices.ObjJClassMethodIndex
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.lang.annotation.AnnotationHolder
import org.jetbrains.uast.getContainingClass

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
                continue;
            }
            if (classMethodHeader.selectorString == thisSelector) {
                annotationHolder.createErrorAnnotation(methodHeader, "Duplicate method selector in class")
                return
            }
        }
    }
}
