package cappuccino.ide.intellij.plugin.jstypedef.annotator

import cappuccino.ide.intellij.plugin.annotator.AnnotationHolderWrapper
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefInterfaceElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefProperty
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import cappuccino.ide.intellij.plugin.utils.document
import com.intellij.codeInspection.IntentionAndQuickFixAction
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager


internal fun annotateProperty(property: JsTypeDefProperty, annotationHolder: AnnotationHolderWrapper) {
    val parentVariableDeclaration = property.parent as? JsTypeDefVariableDeclaration
    if (parentVariableDeclaration != null) {
        annotateVariableDec(property, parentVariableDeclaration, annotationHolder)
    }
}

@Suppress("UNUSED_PARAMETER")
private fun annotateVariableDec(property: JsTypeDefProperty, parentVariableDeclaration: JsTypeDefVariableDeclaration, annotationHolder: AnnotationHolderWrapper) {
    val className = property.propertyNameString
    val body = property.interfaceBodyProperty ?: return
    val constructors = body.interfaceConstructorList
    if (constructors.isEmpty())
        return
    val properties = body.propertyList
    if (properties.isEmpty())
        return
    if (properties.none { it.propertyNameString == "prototype" })
        return
    val interfaceElements = JsTypeDefClassesByNameIndex.instance[className, property.project].mapNotNull {
        it as? JsTypeDefInterfaceElement
    }
    if (interfaceElements.isEmpty())
        return
    if (interfaceElements.size > 1) {
        LOGGER.warning("Too many implementations exist")
    }

    val interfaceElement = interfaceElements.firstOrNull() ?: return
    val textRange = property.propertyName?.textRange ?: return

    annotationHolder.newWeakWarningAnnotation("Variable declaration can be mapped to class")
            .range(textRange)
            .withFix(JsTypeDefVariableToClassFix(interfaceElement = interfaceElement, property = property))
            .create()
}

class JsTypeDefVariableToClassFix(interfaceElement: JsTypeDefInterfaceElement, property: JsTypeDefProperty) : IntentionAndQuickFixAction() {
    val property = SmartPointerManager.createPointer(property)
    private val interfaceElement = SmartPointerManager.createPointer(interfaceElement)
    override fun getName(): String {
        return "Map var declaration to class type"
    }

    override fun getFamilyName(): String {
        return "JsTypeDef Inspections"
    }

    override fun applyFix(project: Project, file: PsiFile?, editor: Editor?) {
        if (applyFixActual(project))
            return
    }

    private fun applyFixActual(project: Project): Boolean {

        return runWriteAction {
            val interfaceElement = interfaceElement.element ?: return@runWriteAction false

            val documentManager = PsiDocumentManager.getInstance(project)
            var cachedDocument = documentManager.getCachedDocument(interfaceElement.containingFile)
            if (cachedDocument != null) {
                documentManager.commitDocument(cachedDocument)
            }
            val property = property.element ?: return@runWriteAction false
            val propertyBody = property.interfaceBodyProperty ?: return@runWriteAction false

            // Text and Element starts
            val textRange = interfaceElement.textRange
            val textStart = interfaceElement.typeName?.startOffsetInParent ?: return@runWriteAction false
            val interfaceText = interfaceElement.text
            val bodyStartOffset = interfaceElement.openBrace?.startOffsetInParent?.plus(1)
                    ?: return@runWriteAction false
            if (bodyStartOffset < 1)
                return@runWriteAction false
            val out = StringBuilder("class ")
            out.append(interfaceText.substring(textStart, bodyStartOffset))
            propertyBody.interfaceConstructorList.forEach {
                out.append("\n\t").append(it.text)
            }
            propertyBody.propertyList.filter { it.propertyNameString != "prototype" }.forEach {
                out.append("\n\tstatic ").append(it.text)
            }
            propertyBody.functionList.forEach {
                out.append("\n\tstatic ").append(it.text)
            }
            propertyBody.namelessFunctionList.forEach {
                out.append("\n\tstatic self").append(it.text)
            }
            out.append(interfaceText.substring(bodyStartOffset))
            val document = interfaceElement.document ?: return@runWriteAction false
            EditorUtil.deleteText(document, textRange)
            EditorUtil.insertText(document, out.toString(), textRange.startOffset)
            cachedDocument = documentManager.getCachedDocument(interfaceElement.containingFile)
            if (cachedDocument != null) {
                documentManager.commitDocument(cachedDocument)
            }
            val variableDeclaration = property.parent as? JsTypeDefVariableDeclaration ?: return@runWriteAction false
            variableDeclaration.parent.node?.removeChild(variableDeclaration.node)
            return@runWriteAction true
        }
    }

}