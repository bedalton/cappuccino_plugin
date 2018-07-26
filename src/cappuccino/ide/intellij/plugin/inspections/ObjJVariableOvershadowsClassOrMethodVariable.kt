package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.psi.ObjJVisitor
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor

class ObjJVariableOvershadowsClassOrMethodVariable : LocalInspectionTool() {
    override fun runForWholeFile(): Boolean = true;
    override fun getDisplayName(): String = "Overshadows class or method variable";

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitVariableName(variableName: ObjJVariableName) {
                if (!ObjJVariableOvershadowInspection.isBodyVariableAssignment(variableName)) {
                    return;
                }
                annotateVariableIfOvershadowInstanceVariable(variableName, holder)
                registerProblemIfOvershadowsMethodVariable(variableName, holder)
            }
        }
    }

    companion object {

        private fun registerProblemIfOvershadowsMethodVariable(variableName: ObjJVariableName, problemsHolder: ProblemsHolder) {
            //Variable is defined in header itself
            if (variableName.getParentOfType(ObjJMethodHeader::class.java) != null) {
                return
            }
            //Check if method is actually in a method declaration
            val methodDeclaration = variableName.getParentOfType(ObjJMethodDeclaration::class.java) ?: return

            //Check if variable overshadows variable defined in method header
            if (ObjJMethodPsiUtils.getHeaderVariableNameMatching(methodDeclaration.methodHeader, variableName.text) != null) {
                problemsHolder.registerProblem(variableName, "Variable overshadows method parameter variable")
            }
        }
        /**
         * Annotates a body variable assignment if it overshadows an instance variable
         * @param variableName variable name element
         * @param problemsHolder
         */
        private fun annotateVariableIfOvershadowInstanceVariable(variableName: ObjJVariableName, problemsHolder: ProblemsHolder) {
            val classDeclarationElement = variableName.getParentOfType(ObjJClassDeclarationElement::class.java)
                    ?: return
            val variableContainingClass = classDeclarationElement.getClassNameString()
            val variableNameText = variableName.text
            for (instanceVariableName in ObjJVariableNameUtil.getAllContainingClassInstanceVariables(variableContainingClass, variableName.project)) {
                if (instanceVariableName.text == variableNameText) {
                    problemsHolder.registerProblem(variableName, "Variable overshadows class variable")
                    return
                }
            }
        }
    }
}