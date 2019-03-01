package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory
import cappuccino.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName

object ObjJNamedPsiUtil {

    fun hasText(variableNameElement: ObjJVariableName, variableNameString: String): Boolean {
        return variableNameElement.text == variableNameString
    }

    fun getName(variableName: ObjJVariableName): String {
        return if (variableName.stub != null) {
            variableName.stub.variableName
        } else variableName.text
    }

    fun setName(variableName: ObjJVariableName, newName: String): PsiElement {
        val newElement = ObjJElementFactory.createVariableName(variableName.project, newName)
        variableName.replace(newElement)
        return newElement
    }

    fun setName(variableName: ObjJInstanceVariableDeclaration, newName: String): PsiElement {
        val newElement = ObjJElementFactory.createVariableName(variableName.project, newName)
        variableName.variableName?.replace(newElement);
        return variableName
    }

    fun getName(className: ObjJClassName): String {
        return className?.stub?.getClassName() ?: className.text
    }

    fun setName(className: ObjJClassName, newName: String): PsiElement {
        val newElement:ObjJClassName? = ObjJElementFactory.createClassName(className.project, newName)
        if (newElement == null) {
            return className
        }
        className.replace(newElement)
        return newElement
    }
}
