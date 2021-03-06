package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.fixes.ObjJAddSuppressInspectionForScope
import cappuccino.ide.intellij.plugin.fixes.ObjJAlterIgnoredUndeclaredVariable
import cappuccino.ide.intellij.plugin.fixes.ObjJSuppressInspectionScope
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefPropertiesByNamespaceIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.elementType
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
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
            if (variableNameIn.reference.multiResolve(true).isNotEmpty())
                return
            if ((parentDeclaration.parent.parent as? ObjJBodyVariableAssignment)?.varModifier != null ||
                    parentDeclaration.parent.getPreviousNonEmptySibling(true).elementType == ObjJTypes.ObjJ_VAR_MODIFIER)
                return

            // Is Global keyword, ignore even if it should not be assigned to
            if (variableNameIn.hasText("self") || variableNameIn.hasText("super") || variableNameIn.hasText("this")) {
                return
            }

            if (variableNameIn.indexInQualifiedReference != 0) {
                return
            }

            val project = variableNameIn.project
            val text = variableNameIn.text
            if (JsTypeDefPropertiesByNamespaceIndex.instance.containsKey(text, project) || JsTypeDefFunctionsByNamespaceIndex.instance.containsKey(text, project) || JsTypeDefClassesByNameIndex.instance.containsKey(text, project))
                return
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
