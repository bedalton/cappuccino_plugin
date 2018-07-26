package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.fixes.ObjJIgnoreOvershadowedVariablesInProject
import cappuccino.ide.intellij.plugin.fixes.ObjJRemoveVarKeywordQuickFix
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil
import cappuccino.ide.intellij.plugin.psi.utils.getParentBlockChildrenOfType
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElementVisitor
import java.util.logging.Level
import java.util.logging.Logger

class ObjJVariableOvershadowInspection : LocalInspectionTool() {

    override fun getDisplayName(): String = "Overshadowing Variables"

    override fun getShortName(): String {
        return "VariableOvershadowsParentVariable"
    }

    override fun getGroupDisplayName(): String = ObjJInspectionProvider.GROUP_DISPLAY_NAME

    override fun runForWholeFile(): Boolean = true

    override fun buildVisitor(problemsHolder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitVariableName(variableName: ObjJVariableName) {
                super.visitVariableName(variableName)
                annotateOvershadow(variableName, problemsHolder)
            }
        }
    }

    companion object {
        private const val OVERSHADOWS_VARIABLE_STRING_FORMAT = "Variable overshadows existing variable in %s"
        private const val OVERSHADOWS_FUNCTION_NAME_STRING_FORMAT = "Variable overshadows function with name %s"
        private const val OVERSHADOWS_CLASS_VARIABLE = "Variable overshadows class variable"
        private const val OVERSHADOWS_METHOD_HEADER_VARIABLE = "Variable overshadows method variable"

        /**
         * Checks whether this variable is a body variable assignment declaration
         * @param variableName variable name element
         * @return `true` if variable name element is part of a variable declaration
         */
        fun isBodyVariableAssignment(variableName: ObjJVariableName): Boolean {
            if (variableName.hasParentOfType(ObjJExpr::class.java)) {
                return false;
            }
            val bodyVariableAssignment = variableName.getParentOfType(ObjJBodyVariableAssignment::class.java)
            return bodyVariableAssignment != null && bodyVariableAssignment.varModifier != null
        }

        private fun annotateOvershadow(variableName: ObjJVariableName, problemsHolder: ProblemsHolder) {
            if (ObjJPluginSettings.ignoreOvershadowedVariables()) {
                //return
            }
            if (!isBodyVariableAssignment(variableName)) {
                return
            }
            annotateIfOvershadowsBlocks(variableName, problemsHolder)
            annotateVariableIfOvershadowsFileVars(variableName, problemsHolder)
            annotateIfOvershadowsMethodVariable(variableName, problemsHolder)
        }

        private fun annotateIfOvershadowsMethodVariable(variableName: ObjJVariableName, problemsHolder: ProblemsHolder) {
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

        private fun annotateIfOvershadowsBlocks(variableName: ObjJVariableName, problemsHolder: ProblemsHolder) {
            val bodyVariableAssignments = variableName.getParentBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true)
            if (bodyVariableAssignments.isEmpty()) {
                return
            }
            val offset = variableName.textRange.startOffset
            val variableNameString = variableName.text
            for (bodyVariableAssignment in bodyVariableAssignments) {
                if (isDeclaredInBodyVariableAssignment(bodyVariableAssignment, variableNameString, offset)) {
                    problemsHolder.registerProblem(variableName, "Variable overshadows variable in enclosing block",ObjJIgnoreOvershadowedVariablesInProject(), ObjJRemoveVarKeywordQuickFix())
                    return
                }
            }
        }

        private fun isDeclaredInBodyVariableAssignment(variableAssignment: ObjJBodyVariableAssignment, variableNameString: String, offset: Int): Boolean {
            if (variableAssignment.varModifier == null) {
                return false
            }

            val qualifiedReferences = mutableListOf<ObjJQualifiedReference>()
            val varNames = variableAssignment.variableNameList
            for (declaration in variableAssignment.variableDeclarationList) {
                qualifiedReferences.addAll(declaration.qualifiedReferenceList)
            }
            for (qualifiedReference in qualifiedReferences) {
                varNames.add(qualifiedReference.primaryVar!!)
            }
            return ObjJVariableNameUtil.getFirstMatchOrNull(varNames) { it.text == variableNameString && offset > it.textRange.startOffset } != null
        }

        /**
         * Annotes variable if it overshadows any file scoped variables or function names
         * @param variableName variable name
         * @param problemsHolder
         */
        private fun annotateVariableIfOvershadowsFileVars(variableName: ObjJVariableName, problemsHolder: ProblemsHolder) {
            val file = variableName.containingFile
            val reference = ObjJVariableNameUtil.getFirstMatchOrNull(ObjJVariableNameUtil.getAllFileScopedVariables(file, 0)) { variableToCheck -> variableName.text == variableToCheck.text }
            if (reference != null && reference != variableName) {
                problemsHolder.registerProblem(variableName, String.format(OVERSHADOWS_VARIABLE_STRING_FORMAT, "file scope"), ObjJIgnoreOvershadowedVariablesInProject(),ObjJRemoveVarKeywordQuickFix())
                return
            }
            for (declarationElement in ObjJFunctionsIndex.instance[variableName.text, variableName.project]) {
                ProgressIndicatorProvider.checkCanceled()
                if (declarationElement.containingFile.isEquivalentTo(file) && declarationElement.functionNameNode != null && variableName.textRange.startOffset > declarationElement.functionNameNode!!.textRange.startOffset) {
                    problemsHolder.registerProblem(variableName, String.format(OVERSHADOWS_FUNCTION_NAME_STRING_FORMAT, variableName.text), ObjJIgnoreOvershadowedVariablesInProject(), ObjJRemoveVarKeywordQuickFix())
                }

            }
        }

    }

}
