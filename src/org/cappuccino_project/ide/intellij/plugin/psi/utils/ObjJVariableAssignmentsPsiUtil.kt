package org.cappuccino_project.ide.intellij.plugin.psi.utils

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJVariableAssignment
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils

import java.util.ArrayList

object ObjJVariableAssignmentsPsiUtil {

    fun getAllVariableAssignmentsMatchingName(element: PsiElement, fqName: String): List<ObjJVariableAssignment> {
        val bodyVariableAssignments = getAllVariableAssignements(element)
        return ArrayUtils.filter(bodyVariableAssignments) filter@ { assignment ->
            for (qualifiedReference in assignment.qualifiedReferenceList) {
                if (qualifiedReference.variableNameList.isEmpty()) {
                    continue
                }
                val currentVariableFqName = ObjJVariableNameUtil.getQualifiedNameAsString(qualifiedReference, null)
                if (fqName == currentVariableFqName) {
                    return@filter true
                }
            }
            false
        }
    }

    fun getAllVariableAssignements(block: PsiElement): List<ObjJVariableAssignment> {
        val compositeElements = block.getParentBlockChildrenOfType(ObjJCompositeElement::class.java, true)
        val declarations = ArrayList<ObjJVariableAssignment>()
        for (compositeElement in compositeElements) {
            if (compositeElement is ObjJBodyVariableAssignment) {
                addVariableDeclarationFromBodyVariableAssignment(declarations, compositeElement)
            } else if (compositeElement is ObjJExpr) {
                addVariableDeclarationFromExpression(declarations, compositeElement)
            }
        }
        addVariableDeclarationsInFile(declarations, block.containingFile)
        return declarations
    }

    private fun addVariableDeclarationsInFile(declarations: MutableList<ObjJVariableAssignment>, file: PsiFile) {
        for (variableAssignment in file.getChildrenOfType( ObjJBodyVariableAssignment::class.java)) {
            addVariableDeclarationFromBodyVariableAssignment(declarations, variableAssignment)
        }
    }

    private fun addVariableDeclarationFromBodyVariableAssignment(declarations: MutableList<ObjJVariableAssignment>, bodyVariableAssignment: ObjJBodyVariableAssignment) {
        declarations.addAll(bodyVariableAssignment.getChildrenOfType( ObjJVariableAssignment::class.java))
    }

    private fun addVariableDeclarationFromExpression(declarations: MutableList<ObjJVariableAssignment>, expression: ObjJExpr) {
        if (expression.leftExpr == null) {
            return
        }
        if (expression.leftExpr == null) {
            return
        }
        declarations.addAll(expression.leftExpr!!.variableDeclaration!!.getChildrenOfType(ObjJVariableAssignment::class.java))
    }

    fun getAssignedValue(assignmentLogical: ObjJVariableAssignmentLogical): ObjJExpr {
        return assignmentLogical.assignmentExprPrime.expr
    }

}
