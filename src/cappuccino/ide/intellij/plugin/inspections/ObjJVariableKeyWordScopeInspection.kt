package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.psi.ObjJInstanceVariableList
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.psi.ObjJVisitor
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.utils.isUniversalMethodCaller
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.annotations.NotNull

class ObjJVariableKeyWordScopeInspection : LocalInspectionTool() {

    override fun getGroupDisplayName(): String = ObjJInspectionProvider.GROUP_DISPLAY_NAME

    @NotNull
    override fun getDisplayName(): String {
        return "Keyword used outside of expected context"
    }

    override fun buildVisitor(problemsHolder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitVariableName(variableName: ObjJVariableName) {
                super.visitVariableName(variableName)
                val inInstanceVariablesList = variableName.getParentOfType(ObjJInstanceVariableList::class.java) != null
                val isUsedOutsideOfClass = isUniversalMethodCaller(variableName.containingClassName)
                val usedOutsideOfBlock = variableName.getParentOfType(ObjJBlock::class.java) == null
                when (variableName.text) {
                    "super", "this", "self" -> when {
                        isUsedOutsideOfClass && usedOutsideOfBlock -> problemsHolder.registerProblem(variableName, variableName.text + " used outside of class")
                        inInstanceVariablesList -> problemsHolder.registerProblem(variableName, "Using reserved variable name")
                        usedOutsideOfBlock -> problemsHolder.registerProblem(variableName, "Possible misuse of 'this' outside of block")
                    }
                }
            }
        }
    }

}
