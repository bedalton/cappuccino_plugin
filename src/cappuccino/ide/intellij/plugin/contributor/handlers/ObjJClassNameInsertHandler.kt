package cappuccino.ide.intellij.plugin.contributor.handlers

import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.ObjJInheritedProtocolList
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclarationSelector
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getNextNonEmptyNode
import cappuccino.ide.intellij.plugin.psi.utils.getNextNonEmptySibling
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.tokenType
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.ex.util.EditorUIUtil
import com.intellij.psi.PsiElement

class ObjJClassNameInsertHandler : InsertHandler<LookupElement> {

    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        val thisElement: PsiElement = lookupElement.psiElement ?: return
        when {
            thisElement.hasParentOfType(ObjJMethodDeclarationSelector::class.java) -> {
                val nextElement: PsiElement = thisElement.getNextNonEmptySibling(true) ?: return
                when {
                    !nextElement.text.equals(")") -> {
                        EditorUtil.insertText(insertionContext, ")", true)
                        if (EditorUtil.isTextAtOffset(insertionContext.document, insertionContext.selectionEndOffset + 1, " ")) {
                            EditorUtil.insertText(insertionContext, " ", true)
                        }
                    }
                    else -> {
                        if (nextElement.text.equals(")")) {
                            insertionContext.editor.caretModel.moveToOffset(nextElement.textRange.endOffset)
                        }
                    }
                }
                return
            }

            thisElement.hasParentOfType(ObjJInheritedProtocolList::class.java) -> {
                val nextElement = thisElement.getNextNonEmptySibling(true)
                if (nextElement == null) {
                    EditorUtil.insertText(insertionContext, ">", false)
                } else if (nextElement.text == ",") {
                    EditorUtil.offsetCaret(insertionContext, 1)
                } else if (nextElement is ObjJClassName) {
                    EditorUtil.insertText(insertionContext, ",", true)
                } else if (nextElement.tokenType() != ObjJTypes.ObjJ_GREATER_THAN) {
                    EditorUtil.insertText(insertionContext, ">", false)
                }
            }
        }
    }

    companion object {
        val instance = ObjJClassNameInsertHandler()
    }
}
