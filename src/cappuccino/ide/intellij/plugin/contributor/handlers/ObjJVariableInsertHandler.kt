package cappuccino.ide.intellij.plugin.contributor.handlers

import cappuccino.ide.intellij.plugin.contributor.ObjJInsertionTracker
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.psi.ObjJExpr
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionCall
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameAggregatorUtil
import cappuccino.ide.intellij.plugin.psi.utils.getNextNonEmptyNode
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType

/**
 * Handles variable name completion events
 */
object ObjJVariableInsertHandler : InsertHandler<LookupElement> {

    /**
     * Entry point for insertion
     */
    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        ObjJInsertionTracker.hit(lookupElement.lookupString)
        handleInsert(lookupElement.psiElement, insertionContext.editor)

    }

    /**
     * Performs appropriate inserts
     */
    private fun handleInsert(
            element: PsiElement?, editor: Editor) {
        if (element == null) {
            return
        }
        if (isFunctionCompletion(element)) {
            EditorUtil.insertText(editor, "()", false)
            EditorUtil.offsetCaret(editor, 1)
        }
        if (shouldAppendFunctionParamComma(element)) {
            EditorUtil.insertText(editor, ", ", true)
        }
        if (shouldAppendClosingBracket(element)) {
            EditorUtil.insertText(editor, "]", false)
        }
    }

    /**
     * Appends comma if is a function parameter
     */
    fun shouldAppendFunctionParamComma(element: PsiElement): Boolean {
        val parentExpression = element.getParentOfType( ObjJExpr::class.java) ?: return false
        val nextNonEmptyNode = parentExpression.getNextNonEmptyNode(true)
        return parentExpression.parent is ObjJFunctionCall && (nextNonEmptyNode == null || nextNonEmptyNode.elementType !== ObjJTypes.ObjJ_COMMA)
    }

    /**
     * Appends closing bracket if inside a method call
     */
    fun shouldAppendClosingBracket(element: PsiElement?): Boolean {
        val parentExpression = element.getParentOfType( ObjJExpr::class.java) ?: return false
        val methodCall = parentExpression.getParentOfType( ObjJMethodCall::class.java)
        return methodCall != null && methodCall.closeBracket == null
    }

    /**
     * Checks whether this variable name is in fact a method
     */
    fun isFunctionCompletion(element: PsiElement): Boolean {
        return !DumbService.isDumb(element.project) && ObjJVariableNameAggregatorUtil.getPrecedingVariableAssignmentNameElements(element, 0).isEmpty() && !ObjJFunctionsIndex.instance[element.text, element.project].isEmpty()
    }
}
