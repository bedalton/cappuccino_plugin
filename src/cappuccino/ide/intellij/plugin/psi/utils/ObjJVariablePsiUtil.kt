package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.inference.inferQualifiedReferenceType
import cappuccino.ide.intellij.plugin.inference.toClassList
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.previousSiblings

object ObjJVariablePsiUtil {

    fun toString(variableName: ObjJVariableName): String {
        return "ObjJ_VAR_NAME(" + variableName.text + ")"
    }

    fun isNewVarDec(psiElement: PsiElement): Boolean {
        val reference = psiElement.getParentOfType(ObjJQualifiedReference::class.java) ?: return false
        if (reference.parent !is ObjJVariableDeclaration && reference.parent !is ObjJBodyVariableAssignment) {
            return false
        }
        val bodyVariableAssignment = reference.getParentOfType(ObjJBodyVariableAssignment::class.java)
        return bodyVariableAssignment != null && bodyVariableAssignment.varModifier != null
    }

    fun getVariableType(variable: ObjJGlobalVariableDeclaration): String? {
        val stub = variable.stub
        if (stub?.variableType?.isEmpty() == true) {
            return stub.variableType
        }
        return null
    }
}