package org.cappuccino_project.ide.intellij.plugin.annotator

import com.intellij.lang.annotation.AnnotationHolder
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJClassName
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil

import java.util.ArrayList


internal object ObjJProtocolDeclarationAnnotatorUtil {

    fun annotateProtocolDeclaration(protocolDeclaration: ObjJProtocolDeclaration, annotationHolder: AnnotationHolder) {
        annotateIfDuplicateProtocol(protocolDeclaration, annotationHolder)
    }


    private fun annotateIfDuplicateProtocol(thisProtocolDeclaration: ObjJProtocolDeclaration, annotationHolder: AnnotationHolder) {
        val classNameElement = thisProtocolDeclaration.className ?: return
        val className = classNameElement.text
        val duplicates = ArrayList<ObjJProtocolDeclaration>()
        for (protocolDeclaration in ObjJProtocolDeclarationsIndex.instance.get(className, classNameElement.project)) {
            if (protocolDeclaration.isEquivalentTo(thisProtocolDeclaration)) {
                continue
            }
            duplicates.add(protocolDeclaration)
        }
        if (duplicates.isEmpty()) {
            return
        }
        val errorMessage = StringBuilder("Duplicate protocol <$className> found in ")
        for (protocolDeclaration in duplicates) {
            errorMessage.append(ObjJFileUtil.getContainingFileName(protocolDeclaration)).append(", ")
        }
        annotationHolder.createErrorAnnotation(classNameElement, errorMessage.substring(0, errorMessage.length - 2))

    }

}
