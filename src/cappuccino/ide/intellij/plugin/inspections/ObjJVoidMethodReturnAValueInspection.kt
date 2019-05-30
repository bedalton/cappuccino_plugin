package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.fixes.ObjJAddSuppressInspectionForScope
import cappuccino.ide.intellij.plugin.fixes.ObjJSuppressInspectionScope
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.references.ObjJIgnoreEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import java.util.*

class ObjJVoidMethodReturnAValueInspection : LocalInspectionTool() {

    override fun runForWholeFile(): Boolean = true

    override fun getShortName(): String {
        return "VoidMethodReturnsAValue"
    }

    override fun buildVisitor(problemsHolder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitMethodDeclaration(methodDeclaration: ObjJMethodDeclaration) {
                super.visitMethodDeclaration(methodDeclaration)
                if (methodDeclaration.methodHeader.explicitReturnType != "void")
                    return
                val block = methodDeclaration.block ?: return
                validateBlockReturnStatements(block, problemsHolder)
            }
        }
    }

    companion object {

        private val NIL_EQUIVALENTS = listOf("nil", "null", "undefined")

        private fun validateBlockReturnStatements(block: ObjJBlock, problemsHolder: ProblemsHolder) {
            if (ObjJIgnoreEvaluatorUtil.isIgnored(block, ObjJSuppressInspectionFlags.IGNORE_RETURN_STATEMENT, true)) {
                return
            }
            val returnStatementsList = block.getBlockChildrenOfType(ObjJReturnStatement::class.java, true)
            if (returnStatementsList.isEmpty()) {
                return
            }
            val isFunction: Boolean = block.getParentOfType(ObjJFunctionDeclarationElement::class.java) != null
            val isMethod = block is ObjJMethodBlock
            if (!isFunction && !isMethod) {
                return
            }
            val returnsWithExpression = ArrayList<ObjJReturnStatement>()
            for (returnStatement in returnStatementsList) {
                val methodCall = getMethodCallIfIsOnlyPartOfExpression(returnStatement)
                // If action is a method call, check if it returns a value
                if (methodCall != null) {
                    // Provide both checks, as they evaluate to true if only one method with same selector is true
                    if (!returnMethodCallReturnsVoid(methodCall)) {
                        returnsWithExpression.add(returnStatement)
                    }
                    continue
                }
                val functionCall = getFunctionCallIfIsOnlyPartOfExpression(returnStatement)
                if (functionCall != null) {
                    continue
                }
                if (returnStatement.expr != null) {
                    // Check if expression as text is in nil equivalents such as 'null' or 'undefined'
                    when (returnStatement.expr!!.text.toLowerCase()) {
                        !in NIL_EQUIVALENTS -> returnsWithExpression.add(returnStatement)
                    }
                }
            }
            annotateBlockReturnStatements(returnsWithExpression, problemsHolder)
        }

        private fun getMethodCallIfIsOnlyPartOfExpression(returnStatement:ObjJReturnStatement) : ObjJMethodCall? {
            val expression = returnStatement.expr ?: return null
            val methodCall = expression.leftExpr?.methodCall ?: return null
            return if (returnStatement.expr?.rightExprList?.isEmpty() == true)
                methodCall
            else
                null
        }

        private fun getFunctionCallIfIsOnlyPartOfExpression(returnStatement:ObjJReturnStatement) : ObjJFunctionCall? {
            val expression = returnStatement.expr ?: return null
            val functionCall = expression.leftExpr?.functionCall ?: return null
            return if (returnStatement.expr?.rightExprList?.isEmpty() == true)
                functionCall
            else
                null
        }

        private fun annotateBlockReturnStatements(returnsWithExpression: List<ObjJReturnStatement>,
                                                  problemsHolder: ProblemsHolder) {
            for (returnStatement in returnsWithExpression) {
                val element = returnStatement.expr ?: returnStatement.`return`
                //var annotationElement: PsiElement? = functionDeclarationElement.functionNameNode
                problemsHolder.registerProblem(element, ObjJBundle.message("objective-j.inspections.return-statement-disagreement.no-value-expected.message"),
                        ObjJAddSuppressInspectionForScope(element, ObjJSuppressInspectionFlags.IGNORE_RETURN_STATEMENT, ObjJSuppressInspectionScope.METHOD),
                        ObjJAddSuppressInspectionForScope(element, ObjJSuppressInspectionFlags.IGNORE_RETURN_STATEMENT, ObjJSuppressInspectionScope.CLASS),
                        ObjJAddSuppressInspectionForScope(element, ObjJSuppressInspectionFlags.IGNORE_RETURN_STATEMENT, ObjJSuppressInspectionScope.FILE))
            }
        }

        /**
         * Simple check to see if any method with selector has a return type of void
         */
        private fun returnMethodCallReturnsVoid(methodCall: ObjJMethodCall?): Boolean {
            if (methodCall == null) {
                return false
            }
            for (call in getAllMethodsForCall(methodCall)) {
                if (ObjJClassType.VOID_CLASS_NAME in call.returnTypes) {
                    return true
                }
            }
            return false
        }

        private fun getAllMethodsForCall(methodCall: ObjJMethodCall?): List<ObjJMethodHeaderDeclaration<*>> {
            if (methodCall == null) {
                return Collections.emptyList()
            }
            val fullSelector = methodCall.selectorString
            val project = methodCall.project
            val out: ArrayList<ObjJMethodHeaderDeclaration<*>> = ArrayList()
            out.addAll(ObjJUnifiedMethodIndex.instance[fullSelector, project])
            return out
        }
    }
}