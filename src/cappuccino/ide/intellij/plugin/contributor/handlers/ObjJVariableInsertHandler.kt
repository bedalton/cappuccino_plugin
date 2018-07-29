package cappuccino.ide.intellij.plugin.contributor.handlers

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
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil
import cappuccino.ide.intellij.plugin.psi.utils.getNextNonEmptyNode
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType

class ObjJVariableInsertHandler private constructor() : InsertHandler<LookupElement> {

    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        handleInsert(lookupElement.psiElement, insertionContext.editor)

    }

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

    fun shouldAppendFunctionParamComma(element: PsiElement): Boolean {
        val parentExpression = element.getParentOfType( ObjJExpr::class.java) ?: return false
        val nextNonEmptyNode = parentExpression.getNextNonEmptyNode(true)
        return parentExpression.parent is ObjJFunctionCall && (nextNonEmptyNode == null || nextNonEmptyNode.elementType !== ObjJTypes.ObjJ_COMMA)
    }

    fun shouldAppendClosingBracket(element: PsiElement?): Boolean {
        val parentExpression = element.getParentOfType( ObjJExpr::class.java) ?: return false
        val methodCall = parentExpression.getParentOfType( ObjJMethodCall::class.java)
        return methodCall != null && methodCall.closeBracket == null
    }

    fun isFunctionCompletion(element: PsiElement): Boolean {
        return !DumbService.isDumb(element.project) && ObjJVariableNameUtil.getPrecedingVariableAssignmentNameElements(element, 0).isEmpty() && !ObjJFunctionsIndex.instance[element.text, element.project].isEmpty()
    }

    companion object {

        val instance = ObjJVariableInsertHandler()
    }
}
