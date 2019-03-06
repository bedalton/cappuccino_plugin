package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.fixes.ObjJAddIgnoreInspectionForScope
import cappuccino.ide.intellij.plugin.fixes.ObjJIgnoreScope
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.ObjJCommentParserUtil
import cappuccino.ide.intellij.plugin.psi.utils.IgnoreFlags
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import java.util.*

class ObjJReturnStatementDisagreementInspection : LocalInspectionTool() {

    override fun runForWholeFile(): Boolean = true

    override fun getShortName(): String {
        return "ReturnStatementDisagreement"
    }

    override fun buildVisitor(problemsHolder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitCompositeElement(element: ObjJCompositeElement) {
                super.visitCompositeElement(element)
                val block: ObjJBlock = element as? ObjJBlock ?: return
                validateBlockReturnStatements(block, problemsHolder)
            }
        }
    }

    companion object {

        private fun validateBlockReturnStatements(block: ObjJBlock, problemsHolder: ProblemsHolder) {
            if (ObjJCommentParserUtil.isIgnored(block, IgnoreFlags.IGNORE_RETURN_STATEMENT, true)) {
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
            val returnsWithoutExpression = ArrayList<ObjJReturnStatement>()
            for (returnStatement in returnStatementsList) {
                if (isFunction) {
                    if (returnStatement.getParentOfType(ObjJFunctionDeclarationElement::class.java) == null) {
                        continue
                    }
                } else if (isMethod) {
                    if (returnStatement.getParentOfType(ObjJMethodBlock::class.java) == null) {
                        continue
                    }
                }
                if (returnStatement.expr?.leftExpr?.methodCall != null && returnStatement.expr?.rightExprList?.isEmpty() == true) {
                    val methodCall: ObjJMethodCall? = returnStatement.expr?.leftExpr?.methodCall
                    if (returnMethodCallReturnsValue(methodCall)) {
                        returnsWithExpression.add(returnStatement)
                    }
                    if (returnMethodCallReturnsVoid(methodCall)) {
                        returnsWithoutExpression.add(returnStatement)
                    }
                } else if (returnStatement.expr != null) {
                    when (returnStatement.expr!!.text.toLowerCase()) {
                        "nil", "null", "undefined" -> {
                            returnsWithExpression.add(returnStatement)
                            returnsWithoutExpression.add(returnStatement)
                        }
                        else -> returnsWithExpression.add(returnStatement)
                    }
                } else {
                    returnsWithoutExpression.add(returnStatement)
                }
            }
            val methodDeclaration = block.getParentOfType(ObjJMethodDeclaration::class.java)
            if (isFunction) {
                annotateBlockReturnStatements(returnsWithExpression, returnsWithoutExpression, problemsHolder)
            } else if (methodDeclaration != null) {
                annotateBlockReturnStatements(methodDeclaration, returnsWithExpression, returnsWithoutExpression, problemsHolder)
            }
        }

        private fun annotateBlockReturnStatements(methodDeclaration: ObjJMethodDeclaration,
                                                  returnsWithExpression: List<ObjJReturnStatement>,
                                                  returnsWithoutExpression: List<ObjJReturnStatement>,
                                                  problemsHolder: ProblemsHolder) {
            val returnType = methodDeclaration.methodHeader.returnType
            val shouldHaveReturnExpression = returnType != ObjJClassType.VOID_CLASS_NAME
            val statementsToMark = if (shouldHaveReturnExpression) returnsWithoutExpression else returnsWithExpression
            val statementsNotToMark = if (!shouldHaveReturnExpression) returnsWithoutExpression else returnsWithExpression
            val errorAnnotation = if (shouldHaveReturnExpression) "Return statement is missing return element. Element should be of type: <$returnType>" else "Method with return type void should not return a value."
            for (returnStatement in statementsToMark) {
                if (returnStatement.expr?.leftExpr?.functionCall != null) {
                    continue
                }
                if (statementsNotToMark.contains(returnStatement)) {
                    continue
                }
                val element = returnStatement.expr ?: returnStatement.`return`
                problemsHolder.registerProblem(element, errorAnnotation, ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        ObjJAddIgnoreInspectionForScope(element, IgnoreFlags.IGNORE_RETURN_STATEMENT, ObjJIgnoreScope.METHOD),
                        ObjJAddIgnoreInspectionForScope(element, IgnoreFlags.IGNORE_RETURN_STATEMENT, ObjJIgnoreScope.CLASS),
                        ObjJAddIgnoreInspectionForScope(element, IgnoreFlags.IGNORE_RETURN_STATEMENT, ObjJIgnoreScope.FILE))
            }
        }

        private fun annotateBlockReturnStatements(returnsWithExpression: List<ObjJReturnStatement>,
                                                  returnsWithoutExpression: List<ObjJReturnStatement>,
                                                  problemsHolder: ProblemsHolder) {
            if (returnsWithExpression.isNotEmpty()) {
                forloop@ for (returnStatement in returnsWithoutExpression) {
                    when (returnStatement.expr?.text?.toLowerCase()) {
                        "nil", "null", "undefined" ->
                            continue@forloop
                        else -> {
                            val element = returnStatement.expr ?: returnStatement.`return`
                            //var annotationElement: PsiElement? = functionDeclarationElement.functionNameNode
                            problemsHolder.registerProblem(element, "Not all return statements return a value",
                                    ObjJAddIgnoreInspectionForScope(element, IgnoreFlags.IGNORE_RETURN_STATEMENT, ObjJIgnoreScope.METHOD),
                                    ObjJAddIgnoreInspectionForScope(element, IgnoreFlags.IGNORE_RETURN_STATEMENT, ObjJIgnoreScope.CLASS),
                                    ObjJAddIgnoreInspectionForScope(element, IgnoreFlags.IGNORE_RETURN_STATEMENT, ObjJIgnoreScope.FILE))
                        }
                    }
                }

            }
        }

        /*
        private fun validateReturnStatement(element: ObjJReturnStatement, problemsHolder: ProblemsHolder) {
            if (element.getParentOfType(ObjJBlock::class.java) == null) {
                problemsHolder.registerProblem(element, "return used outside of block")
            } else if (element.expr != null &&
                    element.getParentOfType(ObjJFunctionDeclarationElement::class.java) == null &&
                    element.getParentOfType(ObjJMethodDeclaration::class.java) == null) {
                problemsHolder.registerProblem(element, "Return value not captured")
            }
        }
*/
        private fun returnMethodCallReturnsValue(methodCall: ObjJMethodCall?): Boolean {
            if (methodCall == null) {
                return false
            }
            for (call in getAllMethodsForCall(methodCall)) {
                if (call.returnType != ObjJClassType.VOID_CLASS_NAME) {
                    return true
                }
            }
            return false
        }

        private fun returnMethodCallReturnsVoid(methodCall: ObjJMethodCall?): Boolean {
            if (methodCall == null) {
                return false
            }
            for (call in getAllMethodsForCall(methodCall)) {
                if (call.returnType == ObjJClassType.VOID_CLASS_NAME) {
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