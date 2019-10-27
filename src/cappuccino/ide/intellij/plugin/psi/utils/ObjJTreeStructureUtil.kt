package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasTreeStructureElement
import cappuccino.ide.intellij.plugin.structure.ObjJStructureViewElement
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.progress.ProgressIndicatorProvider
import icons.ObjJIcons

object ObjJTreeStructureUtil {

    fun createTreeStructureElement(declaration: ObjJImplementationDeclaration): ObjJStructureViewElement {
        val fileName = ObjJPsiFileUtil.getContainingFileName(declaration)
        val presentation: ItemPresentation = when {
            declaration.isCategory -> PresentationData("${declaration.getClassName()} (${declaration.categoryNameString})", fileName, ObjJIcons.CATEGORY_ICON, null)
            declaration.superClassName != null && declaration.superClassName?.isNotEmpty() == true -> PresentationData("${declaration.classNameString} : ${declaration.superClassName}", fileName, ObjJIcons.CLASS_ICON, null)
            else -> PresentationData("${declaration.classNameString}", fileName, ObjJIcons.CLASS_ICON, null)
        }
        return ObjJStructureViewElement(declaration, presentation, "__"+declaration.classNameString)
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
        val label = "${instanceVariable.formalVariableType.text} ${instanceVariable.variableName?.text
                ?: "{UNDEF}"}${if (instanceVariable.accessor != null) " @accessors" else ""}"
        val presentation = PresentationData(label, ObjJPsiFileUtil.getContainingFileName(instanceVariable), ObjJIcons.VARIABLE_ICON, null)
        return ObjJStructureViewElement(instanceVariable, presentation, "_" + (instanceVariable.variableName?.text
                ?: "UNDEF"))
    }

    fun createTreeStructureElement(declaration: ObjJProtocolDeclaration): ObjJStructureViewElement {
        val fileName = ObjJPsiFileUtil.getContainingFileName(declaration)
        val presentation: ItemPresentation = PresentationData(declaration.classNameString, fileName, ObjJIcons.PROTOCOL_ICON, null)
        return ObjJStructureViewElement(declaration, presentation, "__"+declaration.classNameString)
    }

    fun createTreeStructureElement(header: ObjJProtocolScopedMethodBlock): ObjJStructureViewElement {
        val fileName = ObjJPsiFileUtil.getContainingFileName(header)
        val text = if (header.atOptional != null) "@optional" else "@required"
        return ObjJStructureViewElement(header, PresentationData(text, fileName, null, null), "")
    }

    fun createTreeStructureElement(declaration: ObjJMethodDeclaration): ObjJStructureViewElement {
        val fileName = ObjJPsiFileUtil.getContainingFileName(declaration)
        val presentation: ItemPresentation = PresentationData(declaration.methodHeader.text.replace("[\n\r]*", ""), fileName, ObjJIcons.METHOD_ICON, null)
        return object:ObjJStructureViewElement(declaration, presentation, declaration.containingClassName) {
            override fun getChildren(): Array<out TreeElement> {
                return emptyArray()
            }
        }
    }

    fun createTreeStructureElement(header: ObjJMethodHeader): ObjJStructureViewElement {
        val fileName = ObjJPsiFileUtil.getContainingFileName(header)
        val presentation: ItemPresentation = PresentationData(header.text.replace("[\n\r]*", ""), fileName, ObjJIcons.METHOD_ICON, null)
        return ObjJStructureViewElement(header, presentation, header.containingClassName)
    }

    fun createTreeStructureElement(variable:ObjJGlobalVariableDeclaration): ObjJStructureViewElement {
        val fileName = ObjJPsiFileUtil.getContainingFileName(variable)
        val presentation: ItemPresentation = PresentationData(variable.variableNameString, fileName, ObjJIcons.GLOBAL_VARIABLE_ICON, null)
        return ObjJStructureViewElement(variable, presentation, "zzzz_${variable.variableNameString}")
    }

    fun createTreeStructureElement(variable:ObjJVariableName): ObjJStructureViewElement {
        if (variable.parent?.parent is ObjJVariableDeclaration) {
            val dec:ObjJVariableDeclaration? = variable.parent!!.parent as? ObjJVariableDeclaration
            val functionLiteral = dec?.assignedValue?.leftExpr?.functionLiteral
            if (functionLiteral != null)
                return functionLiteral.createTreeStructureElement()
        }
        val fileName = ObjJPsiFileUtil.getContainingFileName(variable)
        val presentation: ItemPresentation = PresentationData(variable.text, fileName, ObjJIcons.VARIABLE_ICON, null)
        return ObjJStructureViewElement(variable, presentation, "zzzzz_${variable.text}")
    }

    fun createTreeStructureElement(function:ObjJFunctionDeclarationElement<*>) : ObjJStructureViewElement {
        val fileName = ObjJPsiFileUtil.getContainingFileName(function)
        val functionNameString = function.functionNameString.ifEmpty {"function"} +  " (" + function.paramNames.joinToString(", ") +")"
        val presentation: ItemPresentation = PresentationData(functionNameString, fileName, ObjJIcons.FUNCTION_ICON, null)
        return ObjJStructureViewElement(function, presentation, "z_${functionNameString}")
    }

}