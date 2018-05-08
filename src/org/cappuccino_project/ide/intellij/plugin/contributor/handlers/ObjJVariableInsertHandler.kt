package org.cappuccino_project.ide.intellij.plugin.contributor.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJFunctionsIndex
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJExpr
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJFunctionCall
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodCall
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes
import org.cappuccino_project.ide.intellij.plugin.utils.EditorUtil
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil

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
        val parentExpression = ObjJTreeUtil.getParentOfType(element, ObjJExpr::class.java) ?: return false
        val nextNonEmptyNode = ObjJTreeUtil.getNextNonEmptyNode(parentExpression, true)
        return parentExpression.parent is ObjJFunctionCall && (nextNonEmptyNode == null || nextNonEmptyNode.elementType !== ObjJTypes.ObjJ_COMMA)
    }

    fun shouldAppendClosingBracket(element: PsiElement?): Boolean {
        val parentExpression = ObjJTreeUtil.getParentOfType(element, ObjJExpr::class.java) ?: return false
        val methodCall = ObjJTreeUtil.getParentOfType(parentExpression, ObjJMethodCall::class.java)
        return methodCall != null && methodCall.closeBracket == null
    }

    fun isFunctionCompletion(element: PsiElement): Boolean {
        return !DumbService.isDumb(element.project) && ObjJVariableNameUtil.getPrecedingVariableAssignmentNameElements(element, 0).isEmpty() && !ObjJFunctionsIndex.instance.get(element.text, element.project).isEmpty()
    }

    companion object {

        val instance = ObjJVariableInsertHandler()
    }
}
