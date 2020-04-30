package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefInterfaceElement
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import cappuccino.ide.intellij.plugin.utils.document
import com.intellij.codeInspection.IntentionAndQuickFixAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager

internal fun annotateInterfaceElement(element: JsTypeDefInterfaceElement, annotationHolder: AnnotationHolderWrapper) {
    annotateIfShouldBeTypeMap(element, annotationHolder)
}

private fun annotateIfShouldBeTypeMap(element: JsTypeDefInterfaceElement, annotationHolder: AnnotationHolderWrapper) {
    if (element.functionList.isNotEmpty() || element.namelessFunctionList.isNotEmpty())
        return
    if (element.propertyList.isEmpty() || element.propertyList.any { it.stringLiteral == null })
        return
    val typeName = element.typeName ?: return
    annotationHolder.newWarningAnnotation("definition should be typemap not interface")
            .range(typeName.textRange)
            .withFix(InterfaceShouldBeTypeMapFix(interfaceElement = element))
            .create()
}


internal class InterfaceShouldBeTypeMapFix(interfaceElement: JsTypeDefInterfaceElement) : IntentionAndQuickFixAction() {
    private val interfaceElement = SmartPointerManager.createPointer(interfaceElement)
    override fun getName(): String {
        return "Interface element should be typemap"
    }

    override fun getFamilyName(): String {
        return "JsTypeDef Inspections"
    }

    override fun applyFix(project: Project, file: PsiFile?, editor: Editor?) {
        if (applyFixActual(project))
            return
        LOGGER.severe("Failed to transform interface to typemap")
    }

    private fun applyFixActual(project: Project): Boolean {

        return runWriteAction {
            val interfaceElement = interfaceElement.element ?: return@runWriteAction false

            val documentManager = PsiDocumentManager.getInstance(project)
            var cachedDocument = documentManager.getCachedDocument(interfaceElement.containingFile)
            if (cachedDocument != null) {
                documentManager.commitDocument(cachedDocument)
            }

            // Text and Element starts
            val textRange = interfaceElement.`interface`.textRange
            val document = interfaceElement.document ?: return@runWriteAction false
            EditorUtil.deleteText(document, textRange)
            EditorUtil.insertText(document, "typemap", textRange.startOffset)
            cachedDocument = documentManager.getCachedDocument(interfaceElement.containingFile)
            if (cachedDocument != null) {
                documentManager.commitDocument(cachedDocument)
            }
            return@runWriteAction true
        }
    }

}
