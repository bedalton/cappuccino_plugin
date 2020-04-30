package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiFileUtil
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import java.util.*

/**
 * Annotates protocol declarations
 */
internal object ObjJProtocolDeclarationAnnotatorUtil {

    /**
     * Entry method to annotations
     */
    fun annotateProtocolDeclaration(protocolDeclaration: ObjJProtocolDeclaration, annotationHolder: AnnotationHolder) {
        annotateIfDuplicateProtocol(protocolDeclaration, annotationHolder)
        ObjJImplementationDeclarationAnnotatorUtil.annotateInvalidProtocolNames(protocolDeclaration.inheritedProtocolList, annotationHolder)
    }


    /**
     * Annotates protocol if another protocol exists with same name
     * @todo do not include protocols in different frameworks
     */
    private fun annotateIfDuplicateProtocol(thisProtocolDeclaration: ObjJProtocolDeclaration, annotationHolder: AnnotationHolder) {
        val classNameElement = thisProtocolDeclaration.className ?: return
        val className = classNameElement.text
        val duplicates = ArrayList<ObjJProtocolDeclaration>()
        // Find all protocols with same name, and filter out THIS protocol
        for (protocolDeclaration in ObjJProtocolDeclarationsIndex.instance[className, classNameElement.project]) {
            if (protocolDeclaration.isEquivalentTo(thisProtocolDeclaration)) {
                continue
            }
            duplicates.add(protocolDeclaration)
        }
        if (duplicates.isEmpty()) {
            return
        }
        val duplicatedInFileNameList = duplicates.joinToString(", ") { protocolDeclaration -> ObjJPsiFileUtil.getContainingFileName(protocolDeclaration) }
        val errorMessage = ObjJBundle.message("objective-j.annotator-messages.protocol-declaration.duplicate-declaration.message", className, duplicatedInFileNameList)
        annotationHolder.newAnnotation(HighlightSeverity.ERROR, errorMessage)
                .range(classNameElement)
                .create()

    }

}
