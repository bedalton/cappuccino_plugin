package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.fixes.ObjJRemoveMethodReturnTypeFix
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJReturnStatement
import cappuccino.ide.intellij.plugin.psi.ObjJVisitor
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.ObjJCommentParserUtil
import cappuccino.ide.intellij.plugin.psi.utils.IgnoreFlags
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
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
            if (ObjJCommentParserUtil.isIgnored(methodDeclaration, IgnoreFlags.IGNORE_RETURN, true)) {
                return
            }
            val returnType = methodDeclaration.methodHeader.returnType
            if (returnType == ObjJClassType.VOID_CLASS_NAME) {
                return
            }
            val returns = methodDeclaration.methodBlock.getBlockChildrenOfType(ObjJReturnStatement::class.java, true) {
                it.getParentOfType(ObjJFunctionDeclarationElement::class.java) == null
            }
            val returnsValue = returns.isNotEmpty()
            for(returnStatement in returns) {
                val expr = returnStatement.expr
                if (expr == null || expr.text.isEmpty()) {
                    problemsHolder.registerProblem(returnStatement.`return`, "Method must return a value of type: '$returnType'")
                }
            }
            if (!returnsValue) {
                val element = methodDeclaration.methodBlock?.closeBrace ?: methodDeclaration.methodHeader.methodHeaderReturnTypeElement ?: methodDeclaration.methodBlock?.lastChild ?: methodDeclaration.lastChild
                problemsHolder.registerProblem(element, "Method expects return statement", ObjJRemoveMethodReturnTypeFix(element))
            }
        }
    }
}