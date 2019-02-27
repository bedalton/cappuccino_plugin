package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.psi.ObjJBodyVariableAssignment
import cappuccino.ide.intellij.plugin.psi.ObjJVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType

class ObjJRemoveVarKeywordQuickFix() : LocalQuickFix {

    override fun getName(): String {
        return "Remove 'var' modifier"
    }

    override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
        remove(problemDescriptor.psiElement as? ObjJVariableName ?: return)
    }

    private fun remove(variableName:ObjJVariableName) {
        val bodyVariableAssignment:ObjJBodyVariableAssignment =
                variableName.getParentOfType(ObjJBodyVariableAssignment::class.java) ?: return
        // @todo implement var removal while separating out the lone variable
        val variableDeclaration:ObjJVariableDeclaration = variableName.getParentOfType(ObjJVariableDeclaration::class.java) ?: return
        bodyVariableAssignment.varModifier
    }

    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME;
    }

}
