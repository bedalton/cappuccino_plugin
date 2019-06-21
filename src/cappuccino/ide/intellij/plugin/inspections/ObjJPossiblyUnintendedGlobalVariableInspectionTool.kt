package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.contributor.ObjJBuiltInJsProperties
import cappuccino.ide.intellij.plugin.contributor.ObjJGlobalJSVariablesNames
import cappuccino.ide.intellij.plugin.contributor.ObjJKeywordsList
import cappuccino.ide.intellij.plugin.contributor.globalJsClassNames
import cappuccino.ide.intellij.plugin.fixes.*
import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJIterationStatement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.ObjJIgnoreEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.references.ObjJVariableReference
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInspection.*
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class ObjJPossiblyUnintendedGlobalVariableInspectionTool : LocalInspectionTool() {

    override fun runForWholeFile() = true

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitVariableName(variableName: ObjJVariableName) {
                super.visitVariableName(variableName)
                registerProblemIfVariableIsNotDeclaredBeforeUse(variableName, holder)
            }
        }
    }

    companion object {

        private fun registerProblemIfVariableIsNotDeclaredBeforeUse(variableNameIn: ObjJVariableName, problemsHolder:ProblemsHolder) {

            // Check if is global variable assignment
            if (variableNameIn.parent is ObjJGlobalVariableDeclaration)
                return
            // Check that parent is Declaration
            val parentDeclaration = variableNameIn.parent.parent as? ObjJVariableDeclaration ?: return
            if (variableNameIn.reference.resolve(true) != null)
                return
            if ((parentDeclaration.parent.parent as? ObjJBodyVariableAssignment)?.varModifier != null)
                return
            // Is Global keyword, ignore even if it should not be assigned to
            if (variableNameIn.hasText("self") || variableNameIn.hasText("super") || variableNameIn.hasText("this")) {
                return
            }

            if (ObjJPluginSettings.isIgnoredVariableName(variableNameIn.text)) {
                problemsHolder.registerProblem(
                        variableNameIn,
                        ObjJBundle.message("objective-j.inspection.unintended-global-variable.ignoring.message"),
                        ProblemHighlightType.INFORMATION,
                        ObjJAlterIgnoredUndeclaredVariable(
                                keyword = variableNameIn.text,
                                addToIgnored = false,
                                message = ObjJBundle.message("objective-j.inspection.unintended-global-variable.unfix.message", variableNameIn.text)
                        )
                )
                return
            }

            problemsHolder.registerProblem(variableNameIn, ObjJBundle.message("objective-j.inspection.unintended-global-variable.message", variableNameIn.text),
                    ObjJAddSuppressInspectionForScope(variableNameIn, ObjJSuppressInspectionFlags.IGNORE_UNINTENDED_GLOBAL_VARIABLE, ObjJSuppressInspectionScope.METHOD),
                    ObjJAddSuppressInspectionForScope(variableNameIn, ObjJSuppressInspectionFlags.IGNORE_UNINTENDED_GLOBAL_VARIABLE, ObjJSuppressInspectionScope.FUNCTION),
                    ObjJAddSuppressInspectionForScope(variableNameIn, ObjJSuppressInspectionFlags.IGNORE_UNINTENDED_GLOBAL_VARIABLE, ObjJSuppressInspectionScope.CLASS),
                    ObjJAddSuppressInspectionForScope(variableNameIn, ObjJSuppressInspectionFlags.IGNORE_UNINTENDED_GLOBAL_VARIABLE, ObjJSuppressInspectionScope.FILE),
                    ObjJAlterIgnoredUndeclaredVariable(variableNameIn.text, addToIgnored = true, message = ObjJBundle.message("objective-j.inspection.unintended-global-variable.fix.message", variableNameIn.text)))

        }
    }

}
