package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory
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
}
