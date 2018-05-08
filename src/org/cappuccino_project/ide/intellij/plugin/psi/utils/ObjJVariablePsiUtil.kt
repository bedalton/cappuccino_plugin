package org.cappuccino_project.ide.intellij.plugin.psi.utils

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJGlobalVariableDeclarationStub
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil

import java.util.ArrayList
import java.util.Collections

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

    fun setName(instanceVariableDeclaration: ObjJInstanceVariableDeclaration, newName: String): ObjJInstanceVariableDeclaration {
        val oldVariableName = instanceVariableDeclaration.variableName
        val newVariableName = ObjJElementFactory.createVariableName(instanceVariableDeclaration.project, newName)
        Logger.getInstance(ObjJVariablePsiUtil::class.java).assertTrue(newVariableName != null)
        if (oldVariableName != null) {
            instanceVariableDeclaration.node.replaceChild(oldVariableName.node, newVariableName!!.node)
            //Old var name does not exist. Insert from scratch
        } else {
            //Get next psi elemet
            var after: PsiElement? = instanceVariableDeclaration.formalVariableType.nextSibling
            //If next element is not a space, add one
            if (after == null || after.node.elementType !== com.intellij.psi.TokenType.WHITE_SPACE) {
                after = ObjJElementFactory.createSpace(instanceVariableDeclaration.project)
                instanceVariableDeclaration.addAfter(instanceVariableDeclaration.formalVariableType, after)
            }
            //If there is an @accessor statement, add space before
            if (instanceVariableDeclaration.atAccessors != null) {
                instanceVariableDeclaration.addBefore(instanceVariableDeclaration.atAccessors!!, ObjJElementFactory.createSpace(instanceVariableDeclaration.project))
            }
            //Actaully add the variable name element
            instanceVariableDeclaration.addAfter(newVariableName!!, after)
        }
        return instanceVariableDeclaration
    }

    /**
     * Gets the last variableName element in a fully qualified name.
     * @param qualifiedReference qualified variable name
     * @return last var name element.
     */
    fun getLastVar(qualifiedReference: ObjJQualifiedReference): ObjJVariableName? {
        val variableNames = qualifiedReference.variableNameList
        val lastIndex = variableNames.size - 1
        return if (!variableNames.isEmpty()) variableNames[lastIndex] else null
    }

    fun getFileVariableNames(file: PsiFile): List<String> {
        val out = ArrayList<String>()
        for (bodyVariableAssignment in ObjJTreeUtil.getChildrenOfTypeAsList(file, ObjJBodyVariableAssignment::class.java)) {
            for (declaration in bodyVariableAssignment.variableDeclarationList) {
                for (qualifiedReference in declaration.qualifiedReferenceList) {
                    out.add(qualifiedReference.partsAsString)
                }
            }
        }
        return out
    }

    fun isNewVarDec(psiElement: PsiElement): Boolean {
        val reference = ObjJTreeUtil.getParentOfType(psiElement, ObjJQualifiedReference::class.java) ?: return false
        if (reference.parent !is ObjJVariableDeclaration && reference.parent !is ObjJBodyVariableAssignment) {
            return false
        }
        val bodyVariableAssignment = reference.getParentOfType(ObjJBodyVariableAssignment::class.java)
        return bodyVariableAssignment != null && bodyVariableAssignment.varModifier != null
    }

    fun getFileName(declaration: ObjJGlobalVariableDeclaration): String? {
        if (declaration.stub != null) {
            val stub = declaration.stub
            if (stub.fileName != null && !stub.fileName!!.isEmpty()) {
                return stub.fileName
            }
        }
        return ObjJFileUtil.getContainingFileName(declaration)
    }

    fun getVariableNameString(declaration: ObjJGlobalVariableDeclaration): String {
        if (declaration.stub != null) {
            val stub = declaration.stub
            if (!stub.variableName.isEmpty()) {
                return stub.variableName
            }
        }
        return declaration.variableName.text
    }

    fun getVariableType(declaration: ObjJGlobalVariableDeclaration): String? {
        if (declaration.stub != null) {
            val stub = declaration.stub
            if (stub.variableType != null && !stub.variableType!!.isEmpty()) {
                return stub.variableType
            }
        }
        return null
    }


}
