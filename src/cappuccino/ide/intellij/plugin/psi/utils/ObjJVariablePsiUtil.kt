package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil

import java.util.ArrayList

object ObjJVariablePsiUtil {
    private val EMPTY_LIST = emptyList<ObjJVariableName>()


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

    fun isNewVariableDeclaration(psiElement: PsiElement): Boolean {
        val variableName:ObjJVariableName = psiElement as? ObjJVariableName ?: psiElement.getParentOfType(ObjJVariableName::class.java) ?: return false
        val qualifiedReference = variableName.getParentOfType(ObjJQualifiedReference::class.java)
        if (qualifiedReference != null) {
            if (qualifiedReference.parent !is ObjJVariableDeclaration && qualifiedReference.parent !is ObjJBodyVariableAssignment) {
                return false
            }
        }
        val bodyVariableAssignment = variableName.getParentOfType(ObjJBodyVariableAssignment::class.java)
        return bodyVariableAssignment != null && bodyVariableAssignment.varModifier != null
    }

    fun toString(variableName: ObjJVariableName): String {
        return "ObjJ_VAR_NAME(" + variableName.text + ")"
    }

    fun setName(instanceVariable:ObjJInstanceVariableDeclaration, newName: String): ObjJVariableName {
        val project = instanceVariable.project
        val oldVariableName = instanceVariable.variableName
        val newVariableName = ObjJElementFactory.createVariableName(project, newName)
        if (oldVariableName != null) {
            instanceVariable.node.replaceChild(oldVariableName.node, newVariableName.node)
            //Old var name does not exist. Insert from scratch
        } else {
            //Get next psi elemet
            var after: PsiElement? = instanceVariable.formalVariableType.nextSibling
            //If next element is not a space, add one
            if (after == null || after.node.elementType !== com.intellij.psi.TokenType.WHITE_SPACE) {
                after = ObjJElementFactory.createSpace(project)
                instanceVariable.addAfter(instanceVariable.formalVariableType, after)
            }
            //If there is an @accessor statement, add space before
            if (instanceVariable.atAccessors != null) {
                instanceVariable.addBefore(instanceVariable.atAccessors!!, ObjJElementFactory.createSpace(project))
            }
            //Actaully add the variable name element
            instanceVariable.addAfter(newVariableName, after)
        }
        return newVariableName
    }

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

    fun getFileName(globalVariable:ObjJGlobalVariableDeclaration): String? {
        val stub = globalVariable.stub
        if (stub?.fileName?.isEmpty() == true) {
            return stub.fileName
        }
        return ObjJFileUtil.getContainingFileName(globalVariable)
    }

    fun getVariableNameString(globalVariable:ObjJGlobalVariableDeclaration): String {
        val stub = globalVariable.stub
        if (stub?.variableName?.isEmpty() == true) {
            return stub.variableName
        }
        return globalVariable.variableName.text
    }

    fun getVariableType(globalVariable:ObjJGlobalVariableDeclaration): String? {
        val stub = globalVariable.stub
        if (stub?.variableType?.isEmpty() == true) {
            return stub.variableType
        }
        return null
    }


    fun PsiFile.getFileVariableNames(): List<String> {
        val out = ArrayList<String>()
        for (bodyVariableAssignment in getChildrenOfType(ObjJBodyVariableAssignment::class.java)) {
            for (declaration in bodyVariableAssignment.variableDeclarationList) {
                for (qualifiedReference in declaration.qualifiedReferenceList) {
                    out.add(qualifiedReference.partsAsString)
                }
            }
        }
        return out
    }


}
