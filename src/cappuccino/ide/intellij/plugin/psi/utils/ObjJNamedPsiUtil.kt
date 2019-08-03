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
        return variableName.stub?.variableName ?: variableName.text
    }

    fun setName(variableName: ObjJVariableName, newName: String): PsiElement {
        val newElement = ObjJElementFactory.createVariableName(variableName.project, newName)
        return variableName.replace(newElement)
    }

    fun setName(instanceVariableDeclaration: ObjJInstanceVariableDeclaration, newName: String): PsiElement {
        val newElement = ObjJElementFactory.createVariableName(instanceVariableDeclaration.project, newName)
        return instanceVariableDeclaration.variableName?.replace(newElement)!!
    }

    fun getName(className: ObjJClassName): String {
        return className.stub?.getClassName() ?: className.text
    }

    fun setName(className: ObjJClassName, newClassName: String?): PsiElement {
        if (newClassName == null || newClassName.isEmpty()) {
            return className
        }
        val newElement: ObjJClassName = ObjJElementFactory.createClassName(className.project, newClassName) ?: return className
        return className.replace(newElement)
    }
}
