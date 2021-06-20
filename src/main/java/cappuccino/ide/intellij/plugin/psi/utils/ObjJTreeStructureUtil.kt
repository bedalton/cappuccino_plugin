package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasTreeStructureElement
import cappuccino.ide.intellij.plugin.structure.ObjJStructureViewElement
import cappuccino.ide.intellij.plugin.utils.ifEmptyNull
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.progress.ProgressIndicatorProvider
import icons.ObjJIcons

@Suppress("DuplicatedCode")
object ObjJTreeStructureUtil {

    fun createTreeStructureElement(declaration: ObjJImplementationDeclaration): ObjJStructureViewElement {
        val presentation: ItemPresentation = when {
            declaration.isCategory -> PresentationData("${declaration.className} (${declaration.categoryNameString})", null, ObjJIcons.CATEGORY_ICON, null)
            declaration.superClassName != null && declaration.superClassName?.isNotEmpty() == true -> PresentationData("${declaration.classNameString} : ${declaration.superClassName}", null, ObjJIcons.CLASS_ICON, null)
            else -> PresentationData(declaration.classNameString, null, ObjJIcons.CLASS_ICON, null)
        }
        return ObjJStructureViewElement(declaration, presentation, "__"+declaration.classNameString).withWeight(10)
    }

    fun getTreeStructureChildElements(declaration: ObjJImplementationDeclaration): Array<ObjJStructureViewElement> {
        val out: MutableList<ObjJStructureViewElement> = mutableListOf()
        out.add(getHeaderStructureViewElement(declaration))
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

    fun getHeaderStructureViewElement(declaration: ObjJImplementationDeclaration) : ObjJStructureViewElement {
        var label = "implementation ${declaration.classNameString}"
        if (declaration.isCategory) {
            label += "(${declaration.categoryNameString})"
        }
        label += declaration.superClassName?.ifEmptyNull()?.let {
            " : $it"
        } ?: ""
        val protocolList = declaration.getInheritedProtocols().joinToString(", ").ifEmptyNull()
        if (protocolList != null) {
            label += " <$protocolList>"
        }
        val presentation = PresentationData(label, null, ObjJIcons.AT_ICON, null)
        return ObjJStructureViewElement(declaration.className, presentation, "___").withWeight(0)
    }

    fun createTreeStructureElement(instanceVariable: ObjJInstanceVariableDeclaration): ObjJStructureViewElement {
        val label = "${instanceVariable.formalVariableType.text} ${instanceVariable.variableName?.text
                ?: "{UNDEF}"}${if (instanceVariable.accessor != null) " @accessors" else ""}"
        val presentation = PresentationData(label, null, ObjJIcons.INSTANCE_VARIABLE_ICON, null)
        return ObjJStructureViewElement(instanceVariable, presentation, "_" + (instanceVariable.variableName?.text
                ?: "UNDEF")).withWeight(20)
    }

    fun createTreeStructureElement(declaration: ObjJProtocolDeclaration): ObjJStructureViewElement {
        val presentation: ItemPresentation = PresentationData(declaration.classNameString, null, ObjJIcons.PROTOCOL_ICON, null)
        return ObjJStructureViewElement(declaration, presentation, "__"+declaration.classNameString).withWeight(10)
    }

    fun getTreeStructureChildElements(declaration: ObjJProtocolDeclaration): Array<ObjJStructureViewElement> {
        val out: MutableList<ObjJStructureViewElement> = mutableListOf()
        var label = "@protocol ${declaration.classNameString}"
        val protocolList = declaration.getInheritedProtocols().joinToString(", ").ifEmptyNull()
        if (protocolList != null) {
            label += " <$protocolList>"
        }
        val presentation = PresentationData(label, null, null, null)
        out.add(ObjJStructureViewElement(declaration.className, presentation, "___"))
        declaration.getChildrenOfType(ObjJHasTreeStructureElement::class.java).forEach {
            ProgressIndicatorProvider.checkCanceled()
            out.add(it.createTreeStructureElement())
        }
        return out.toTypedArray()
    }

    fun getHeaderStructureViewElement(declaration: ObjJProtocolDeclaration) : ObjJStructureViewElement {
        var label = "protocol ${declaration.classNameString}"
        val protocolList = declaration.getInheritedProtocols().joinToString(", ").ifEmptyNull()
        if (protocolList != null) {
            label += " <$protocolList>"
        }
        val presentation = PresentationData(label, null, ObjJIcons.AT_ICON, null)
        return ObjJStructureViewElement(declaration.className, presentation, "___").withWeight(0)
    }

    fun createTreeStructureElement(header: ObjJProtocolScopedMethodBlock): ObjJStructureViewElement {
        val text = if (header.atOptional != null) "@optional" else "@required"
        return ObjJStructureViewElement(header, PresentationData(text, null, null, null), "")
    }

    fun createTreeStructureElement(declaration: ObjJMethodDeclaration): ObjJStructureViewElement {
        val presentation: ItemPresentation = PresentationData(declaration.methodHeader.text.replace("[\n\r]*", ""), null, ObjJIcons.METHOD_ICON, null)
        return object:ObjJStructureViewElement(declaration, presentation, declaration.containingClassName) {
            override fun getChildren(): Array<out TreeElement> {
                return emptyArray()
            }
        }
    }

    fun createTreeStructureElement(header: ObjJMethodHeader): ObjJStructureViewElement {
        val presentation: ItemPresentation = PresentationData(header.text.replace("[\n\r]*", ""), null, ObjJIcons.METHOD_ICON, null)
        return ObjJStructureViewElement(header, presentation, header.containingClassName)
    }

    fun createTreeStructureElement(variable:ObjJGlobalVariableDeclaration): ObjJStructureViewElement {
        val presentation: ItemPresentation = PresentationData(variable.variableNameString, null, ObjJIcons.GLOBAL_VARIABLE_ICON, null)
        return ObjJStructureViewElement(variable, presentation, "zzzz_${variable.variableNameString}")
    }

    fun createTreeStructureElement(variable:ObjJVariableName): ObjJStructureViewElement {
        if (variable.parent?.parent is ObjJVariableDeclaration) {
            val dec:ObjJVariableDeclaration? = variable.parent!!.parent as? ObjJVariableDeclaration
            val functionLiteral = dec?.assignedValue?.leftExpr?.functionLiteral
            if (functionLiteral != null)
                return functionLiteral.createTreeStructureElement()
        }
        val presentation: ItemPresentation = PresentationData(variable.text, null, ObjJIcons.VARIABLE_ICON, null)
        return ObjJStructureViewElement(variable, presentation, "zzzzz_${variable.text}")
    }

    fun createTreeStructureElement(function:ObjJFunctionDeclarationElement<*>) : ObjJStructureViewElement {
        val functionNameString = function.functionNameString.ifEmpty {"function"} +  " (" + function.parameterNames.joinToString(", ") +")"
        val presentation: ItemPresentation = PresentationData(functionNameString, null, ObjJIcons.FUNCTION_ICON, null)
        return ObjJStructureViewElement(function, presentation, "z_${functionNameString}")
    }

}