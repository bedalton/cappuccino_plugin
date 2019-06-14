package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasTreeStructureElement
import cappuccino.ide.intellij.plugin.structure.ObjJStructureViewElement
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import com.intellij.ide.projectView.PresentationData
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.progress.ProgressIndicatorProvider
import icons.ObjJIcons

object ObjJTreeStructureUtil {

    fun createTreeStructureElement(declaration: ObjJImplementationDeclaration): ObjJStructureViewElement {
        val fileName = ObjJFileUtil.getContainingFileName(declaration)
        val presentation: ItemPresentation = when {
            declaration.isCategory -> PresentationData("@category ${declaration.getClassName()} (${declaration.categoryNameString})", fileName, ObjJIcons.CATEGORY_ICON, null)
            declaration.superClassName != null && declaration.superClassName?.isNotEmpty() == true -> PresentationData("@implementation ${declaration.classNameString} : ${declaration.superClassName}", fileName, ObjJIcons.CLASS_ICON, null)
            else -> PresentationData("@implementation ${declaration.classNameString}", fileName, ObjJIcons.CLASS_ICON, null)
        }
        return ObjJStructureViewElement(declaration, presentation, declaration.classNameString)
    }

    fun getTreeStructureChildElements(declaration: ObjJImplementationDeclaration): Array<ObjJStructureViewElement> {
        val out: MutableList<ObjJStructureViewElement> = mutableListOf()
        declaration.instanceVariableList?.instanceVariableDeclarationList?.forEach {
            ProgressIndicatorProvider.checkCanceled()
            out.add(it.createTreeStructureElement())
        }
        declaration.getChildrenOfType(ObjJHasTreeStructureElement::class.java).forEach {
            ProgressIndicatorProvider.checkCanceled()
            out.add(it.createTreeStructureElement())
        }
        return out.toTypedArray()
    }

    fun createTreeStructureElement(instanceVariable: ObjJInstanceVariableDeclaration): ObjJStructureViewElement {
        val label = "ivar: ${instanceVariable.formalVariableType.text} ${instanceVariable.variableName?.text
                ?: "{UNDEF}"}${if (instanceVariable.accessor != null) " @accessors" else ""}"
        val presentation = PresentationData(label, ObjJFileUtil.getContainingFileName(instanceVariable), ObjJIcons.VARIABLE_ICON, null)
        return ObjJStructureViewElement(instanceVariable, presentation, "_" + (instanceVariable.variableName?.text
                ?: "UNDEF"))
    }

    fun createTreeStructureElement(declaration: ObjJProtocolDeclaration): ObjJStructureViewElement {
        val fileName = ObjJFileUtil.getContainingFileName(declaration)
        val presentation: ItemPresentation = PresentationData("@protocol ${declaration.classNameString}", fileName, ObjJIcons.PROTOCOL_ICON, null)
        return ObjJStructureViewElement(declaration, presentation, declaration.classNameString)
    }

    fun createTreeStructureElement(header: ObjJProtocolScopedMethodBlock): ObjJStructureViewElement {
        val fileName = ObjJFileUtil.getContainingFileName(header)
        val text = if (header.atOptional != null) "@optional" else "@required"
        return ObjJStructureViewElement(header, PresentationData(text, fileName, null, null), "")
    }

    fun createTreeStructureElement(header: ObjJMethodDeclaration): ObjJStructureViewElement {
        return createTreeStructureElement(header.methodHeader)
    }

    fun createTreeStructureElement(header: ObjJMethodHeader): ObjJStructureViewElement {
        val fileName = ObjJFileUtil.getContainingFileName(header)
        val presentation: ItemPresentation = PresentationData(header.text.replace("[\n\r]*", ""), fileName, ObjJIcons.METHOD_ICON, null)
        return ObjJStructureViewElement(header, presentation, header.containingClassName)
    }

}