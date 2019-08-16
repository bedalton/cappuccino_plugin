package cappuccino.ide.intellij.plugin.contributor.handlers

import cappuccino.ide.intellij.plugin.contributor.ObjJInsertionTracker
import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.ObjJInheritedProtocolList
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclarationSelector
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getNextNonEmptySibling
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.tokenType
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiElement

/**
 * Handles the insertion of class names
 * Mostly used for method selector declarations
 */
object ObjJClassNameInsertHandler : InsertHandler<LookupElement> {

    /**
     * Handles the aftermath of inserting a class name
     */
    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        ObjJInsertionTracker.hit(lookupElement.lookupString)
        val thisElement: PsiElement = lookupElement.psiElement ?: return
        when {
            thisElement.hasParentOfType(ObjJMethodDeclarationSelector::class.java) ->
                insertInMethodDeclarationSelector(thisElement, insertionContext)
            thisElement.hasParentOfType(ObjJInheritedProtocolList::class.java) ->
                insertInInheritedProtocolList(thisElement, insertionContext)
        }
    }

    /**
     * Handles insertions while inside method declaration selector
     */
    private fun insertInMethodDeclarationSelector(thisElement:PsiElement, insertionContext: InsertionContext) {
        val nextElement: PsiElement = thisElement.getNextNonEmptySibling(true) ?: return
        when {
            nextElement.text != ")" -> {
                EditorUtil.insertText(insertionContext, ")", true)
                if (EditorUtil.isTextAtOffset(insertionContext.document, insertionContext.selectionEndOffset + 1, " ")) {
                    EditorUtil.insertText(insertionContext, " ", true)
                }
            }
            else -> {
                if (nextElement.text == ")") {
                    insertionContext.editor.caretModel.moveToOffset(nextElement.textRange.endOffset)
                }
            }
        }
    }

    /**
     * Handles insertion while in inherited protocol list
     */
    private fun insertInInheritedProtocolList(thisElement:PsiElement, insertionContext: InsertionContext) {
        val nextElement = thisElement.getNextNonEmptySibling(true)
        when {
            nextElement == null -> EditorUtil.insertText(insertionContext, ">", false)
            nextElement.text == "," -> EditorUtil.offsetCaret(insertionContext, 1)
            nextElement is ObjJClassName -> EditorUtil.insertText(insertionContext, ",", true)
            nextElement.tokenType() != ObjJTypes.ObjJ_GREATER_THAN -> EditorUtil.insertText(insertionContext, ">", false)
        }
    }
}
