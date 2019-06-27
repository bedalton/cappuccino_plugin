package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.fixes.ObjJAddSuppressInspectionForScope
import cappuccino.ide.intellij.plugin.fixes.ObjJAlterIgnoredFunctionNames
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags.*
import cappuccino.ide.intellij.plugin.fixes.ObjJSuppressInspectionScope.*
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor

class ObjJInvalidFunctionNameInspection : LocalInspectionTool() {

    override fun getShortName(): String {
        return "InvalidFunctionName"
    }

    override fun getDisplayName(): String {
        return ObjJBundle.message("objective-j.inspections.function-name-inspection.missing-function.display-name.text")
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitFunctionName(functionName: ObjJFunctionName) {
                super.visitFunctionName(functionName)
                val functionCall = (functionName.parent as? ObjJFunctionCall) ?: return
                annotateFunctionCall(functionCall, holder)
            }
        }
    }
    fun annotateFunctionCall(functionCall:ObjJFunctionCall, problemsHolder: ProblemsHolder) {
        if (!shouldAnnotate(functionCall)) {
            annotateIgnoredFunctionNameIfNecessary(functionCall, problemsHolder)
            return
        }
        if (!functionWithNameExists(functionCall)) {
            annotateInvalidFunctionName(functionCall, problemsHolder)
        }
    }

    private fun shouldAnnotate(functionCall: ObjJFunctionCall) : Boolean {
        if (isIgnored(functionCall)) {
            return false
        }
        if (functionCall.parent !is ObjJLeftExpr || functionCall.hasParentOfType(ObjJPreprocessorIfCondition::class.java) || isInNewDec(functionCall)) {
            return false
        }
        return true
    }

    private fun isInNewDec(functionCall: ObjJFunctionCall) : Boolean {
        val expression = functionCall.getParentOfType(ObjJExpr::class.java) ?: return false
        return expression.parent is ObjJNewExpression
    }

    private fun isIgnored(functionCall: ObjJFunctionCall) : Boolean {
        return ObjJPluginSettings.isIgnoredFunctionName(functionCall.functionName?.text ?: "#_#_#") ||
                ObjJCommentEvaluatorUtil.isIgnored(functionCall, IGNORE_UNDECLARED_FUNCTION) ||
                ObjJCommentEvaluatorUtil.isIgnored(functionCall, IGNORE_UNDECLARED_VAR) ||
                ObjJPluginSettings.isIgnoredVariableName(functionCall.functionName?.text ?: "#_#_#")

    }

    private fun isIgnoredInSettings(functionCall: ObjJFunctionCall) : Boolean {
        val functionName = functionCall.functionName?.text ?: return true
        return ObjJPluginSettings.isIgnoredFunctionName(functionName)
    }

    private fun functionWithNameExists(functionCall:ObjJFunctionCall) : Boolean {
        val functionName = functionCall.functionName ?: return true // Ignore function calls without a name
        return functionName.reference.resolve() != null || JsTypeDefFunctionsByNameIndex.instance.containsKey(functionName.text, functionCall.project) || JsTypeDefClassesByNameIndex.instance.containsKey(functionName.text, functionCall.project)
    }

    private fun annotateInvalidFunctionName(functionCall: ObjJFunctionCall, problemsHolder: ProblemsHolder) {
        val functionName = functionCall.functionName ?: return
        val message = ObjJBundle.message("objective-j.inspections.function-name-inspection.missing-function.message", functionName.text)
        problemsHolder.registerProblem(functionName, message, ObjJAlterIgnoredFunctionNames(functionName.text, true),
                ObjJAddSuppressInspectionForScope(functionName, IGNORE_UNDECLARED_FUNCTION, STATEMENT),
                ObjJAddSuppressInspectionForScope(functionName, IGNORE_UNDECLARED_FUNCTION, METHOD),
                ObjJAddSuppressInspectionForScope(functionName, IGNORE_UNDECLARED_FUNCTION, FUNCTION),
                ObjJAddSuppressInspectionForScope(functionName, IGNORE_UNDECLARED_FUNCTION, FILE),
                ObjJAddSuppressInspectionForScope(functionName, IGNORE_UNDECLARED_FUNCTION, CLASS)
                )
    }

    private fun annotateIgnoredFunctionNameIfNecessary(functionCall:ObjJFunctionCall, problemsHolder: ProblemsHolder) {
        if (functionWithNameExists(functionCall)) {
            return
        }
        if (!isIgnoredInSettings(functionCall)) {
            return
        }
        val functionNameElement = functionCall.functionName ?: return
        val functionName = functionNameElement.text ?: return
        val message = ObjJBundle.message("objective-j.inspections.function-name-inspection.function-is-ignored.message", functionNameElement.text)
        problemsHolder.registerProblem(functionNameElement, message, ProblemHighlightType.INFORMATION, ObjJAlterIgnoredFunctionNames(functionName, false))
    }

}