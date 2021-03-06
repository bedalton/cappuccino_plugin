package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.fixes.ObjJAddSuppressInspectionForScope
import cappuccino.ide.intellij.plugin.fixes.ObjJRemoveMethodReturnTypeFix
import cappuccino.ide.intellij.plugin.fixes.ObjJSuppressInspectionScope
import cappuccino.ide.intellij.plugin.fixes.ObjJSuppressInspectionScope.*
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJReturnStatement
import cappuccino.ide.intellij.plugin.psi.ObjJVisitor
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class ObjJMethodReturnsAValueInspection : LocalInspectionTool() {

    override fun getDisplayName(): String = "Method returns a value"

    override fun getShortName(): String {
        return "MethodReturnsAValue"
    }

    override fun runForWholeFile(): Boolean = true

    override fun buildVisitor(problemsHolder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitMethodDeclaration(methodDeclaration: ObjJMethodDeclaration) {
                super.visitMethodDeclaration(methodDeclaration)
                validateMethodReturn(methodDeclaration, problemsHolder)
            }
        }
    }

    companion object {

        private fun validateMethodReturn(methodDeclaration: ObjJMethodDeclaration, problemsHolder: ProblemsHolder) {
            if (ObjJCommentEvaluatorUtil.isIgnored(methodDeclaration, ObjJSuppressInspectionFlags.IGNORE_RETURN_STATEMENT, true)) {
                return
            }
            val returnType = methodDeclaration.methodHeader.explicitReturnType
            if (returnType == ObjJClassType.VOID_CLASS_NAME || returnType == "@action" || returnType == "IBAction") {
                return
            }
            val returns = methodDeclaration.methodBlock.getBlockChildrenOfType(ObjJReturnStatement::class.java, true) {
                it.getParentOfType(ObjJFunctionDeclarationElement::class.java) == null
            }
            val returnsValue = returns.isNotEmpty()
            for(returnStatement in returns) {
                val expr = returnStatement.expr
                if (expr == null || expr.text.isEmpty()) {
                    problemsHolder.registerProblem(returnStatement.`return`, ObjJBundle.message("objective-j.inspections.method-return-value.must-have-type.message", returnType),
                            suppressFix(returnStatement, METHOD),
                            suppressFix(returnStatement, CLASS),
                            suppressFix(returnStatement, FILE))
                }
            }
            if (!returnsValue) {
                val element = methodDeclaration.methodBlock?.closeBrace ?: methodDeclaration.methodHeader.methodHeaderReturnTypeElement ?: methodDeclaration.methodBlock?.lastChild ?: methodDeclaration.lastChild
                problemsHolder.registerProblem(element, ObjJBundle.message("objective-j.inspections.method-return-value.method-expects-return-statement.message"), ObjJRemoveMethodReturnTypeFix(element),
                        suppressFix(element, METHOD),
                        suppressFix(element, CLASS),
                        suppressFix(element, FILE))
            }
        }

        private fun suppressFix(element:PsiElement, scope:ObjJSuppressInspectionScope) : ObjJAddSuppressInspectionForScope {
                    return ObjJAddSuppressInspectionForScope(element, ObjJSuppressInspectionFlags.IGNORE_RETURN_STATEMENT, scope)
        }
    }
}