package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import com.intellij.psi.AbstractQualifiedReference

import java.util.ArrayList

object ObjJVariablePsiUtil {
    private val EMPTY_LIST = emptyList<ObjJVariableName>()

    fun toString(variableName: ObjJVariableName): String {
        return "ObjJ_VAR_NAME(" + variableName.text + ")"
    }

    fun getInstanceVarDeclarationFromDeclarations(instanceVariableDeclarations: List<ObjJInstanceVariableDeclaration>, variableName: String): ObjJVariableName? {
        if (!instanceVariableDeclarations.isEmpty()) {
            for (instanceVariableDeclaration in instanceVariableDeclarations) {
                val instanceVariableVariableName = instanceVariableDeclaration.variableName!!.text
                if (instanceVariableVariableName == variableName) {
                    return instanceVariableDeclaration.variableName
                }
            }
        }
        return null
    }

    /*
    fun ObjJInstanceVariableDeclaration.setName(newName: String): ObjJInstanceVariableDeclaration {
        val oldVariableName = variableName
        val newVariableName = ObjJElementFactory.createVariableName(project, newName)
        if (oldVariableName != null) {
            node.replaceChild(oldVariableName.node, newVariableName.node)
            //Old var name does not exist. Insert from scratch
        } else {
            //Get next psi elemet
            var after: PsiElement? = formalVariableType.nextSibling
            //If next element is not a space, add one
            if (after == null || after.node.elementType !== com.intellij.psi.TokenType.WHITE_SPACE) {
                after = ObjJElementFactory.createSpace(project)
                addAfter(formalVariableType, after)
            }
            //If there is an @accessor statement, add space before
            if (accessor?.atAccessors != null) {
                addBefore(accessor?.atAccessors!!, ObjJElementFactory.createSpace(project))
            }
            //Actaully add the variable name element
            addAfter(newVariableName, after)
        }
        return this
    }*/

    /**
     * Gets the last variableName element in a fully qualified name.
     * @param qualifiedReference qualified variable name
     * @return last var name element.
     */
    fun getLastVariableName(qualifiedReference: ObjJQualifiedReference): ObjJVariableName? {
        val variableNames = qualifiedReference.variableNameList
        val lastIndex = variableNames.size - 1
        return if (!variableNames.isEmpty()) variableNames[lastIndex] else null
    }

    fun PsiFile.getFileVariableNames(): List<String> {
        val out = ArrayList<String>()
        for (bodyVariableAssignment in getChildrenOfType(ObjJBodyVariableAssignment::class.java)) {
            for (declaration in bodyVariableAssignment.variableDeclarationList?.variableDeclarationList ?: listOf()) {
                for (qualifiedReference in declaration.qualifiedReferenceList) {
                    out.add(qualifiedReference.partsAsString)
                }
            }
        }
        return out
    }

    fun isNewVarDec(psiElement: PsiElement): Boolean {
        val reference = psiElement.getParentOfType(ObjJQualifiedReference::class.java) ?: return false
        if (reference.parent !is ObjJVariableDeclaration && reference.parent !is ObjJBodyVariableAssignment) {
            return false
        }
        val bodyVariableAssignment = reference.getParentOfType(ObjJBodyVariableAssignment::class.java)
        return bodyVariableAssignment != null && bodyVariableAssignment.varModifier != null
    }

    fun getFileName(variableDeclaration: ObjJGlobalVariableDeclaration): String? {
        val stub = variableDeclaration.stub
        if (stub?.fileName?.isEmpty() == true) {
            return stub.fileName
        }
        return ObjJFileUtil.getContainingFileName(variableDeclaration)
    }

    fun getVariableNameString(variableDeclaration: ObjJGlobalVariableDeclaration): String {
        val stub = variableDeclaration.stub
        if (stub?.variableName?.isEmpty() == true) {
            return stub.variableName
        }
        return variableDeclaration.variableName.text
    }

    fun getVariableType(variable:ObjJGlobalVariableDeclaration): String? {
        val stub = variable.stub
        if (stub?.variableType?.isEmpty() == true) {
            return stub.variableType
        }
        return null
    }
}