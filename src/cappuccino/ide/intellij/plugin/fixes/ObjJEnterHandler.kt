package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
//import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import java.util.logging.Level
import java.util.logging.Logger

@Suppress("unused")
class ObjJEnterHandler : EnterHandlerDelegateAdapter() {

    override fun preprocessEnter(file: PsiFile, editor: Editor, caretOffsetRef: Ref<Int>, caretAdvance: Ref<Int>, dataContext: DataContext, originalHandler: EditorActionHandler?): EnterHandlerDelegate.Result {
        if (file !is ObjJFile) {
            return EnterHandlerDelegate.Result.Continue
        }
        val caretOffset:Int = caretOffsetRef.get().toInt()
        val pointer = getPointer(file, caretOffset) ?: return EnterHandlerDelegate.Result.Continue
        var result = EnterHandlerDelegate.Result.Continue
        for (handler in handlers) {
            // Fetch element fresh from pointer each time, hoping that it stays current after modifications
            val element = pointer.element
            if (element == null) {
                return EnterHandlerDelegate.Result.Continue// bail out if element becomes stale
            }
            if (handler.doIf(editor, element)) {
                result = EnterHandlerDelegate.Result.Default
            }
        }
        //com.intellij.psi.codeStyle.CodeStyleManager.adjustLineIndent()
        return result
    }

    private fun getPointer(file:PsiFile, caretOffset:Int) : SmartPsiElementPointer<PsiElement>? {
        val psiElementIn:PsiElement = file.findElementAt(caretOffset) ?: return null
        val element = if (psiElementIn.text.trim().isNotEmpty()) psiElementIn else psiElementIn.getPreviousNonEmptySibling(true) ?: return {
            LOGGER.warning("Failed to find previous non-empty sibling")
            null
        }()
        return SmartPointerManager.createPointer(element)
    }

    companion object {
        internal val LOGGER:Logger by lazy {
            Logger.getLogger(ObjJEnterHandler::class.java.canonicalName)
        }

        private val handlers:List<OnEnterHandler> = listOf(
                MethodCallHandler,
                BlockEnterHandler,
                ClassEnterHandler
        )
    }
}


internal interface OnEnterHandler {
    fun doIf(editor: Editor, psiElementIn: PsiElement) : Boolean
}

object MethodCallHandler : OnEnterHandler {

    override fun doIf(editor: Editor, psiElementIn: PsiElement) : Boolean {
        val methodCall:ObjJMethodCall = psiElementIn.thisOrParentAs(ObjJMethodCall::class.java) ?: return false
        val selectors = methodCall.qualifiedMethodCallSelectorList
        var prevSelector:ObjJQualifiedMethodCallSelector? = null
        var didRun = false
        for (selector in selectors) {
            if (prevSelector == null) {
                prevSelector = selector
                continue
            }
            if (onIfQualifiedMethodCallSelector(editor, selector, prevSelector)) {
                didRun = true
            }

        }
        return didRun
    }

    private fun onIfQualifiedMethodCallSelector(editor: Editor, thisMethodCallSelector: ObjJQualifiedMethodCallSelector, prevSelector: ObjJQualifiedMethodCallSelector) : Boolean {
        if (!thisMethodCallSelector.node.isDirectlyPrecededByNewline()) {
            return false
        }
        val document = editor.document
        val elementLineNumber = document.getLineNumber(thisMethodCallSelector.textRange.startOffset)
        val startOfLine = document.getLineStartOffset(elementLineNumber)
        val siblingColonOffset = prevSelector.colon.distanceFromStartOfLine(document)
        val thisSelectorColonOffset = thisMethodCallSelector.colon.distanceFromStartOfLine(document)
        val neededOffset = siblingColonOffset - thisSelectorColonOffset // if needs shift left is negative
        val thisSelectorElementOffset = thisMethodCallSelector.distanceFromStartOfLine(document)
        val newStart = thisSelectorElementOffset + neededOffset
        if (newStart < startOfLine) {
            return false
        }
        if (neededOffset == 0) {
            return false
        }
        if (neededOffset > 0) {
            val neededText = " ".repeat(neededOffset)
            assert(neededText.length == neededOffset) {"Text was not of desired length" }
            editor.document.insertString(startOfLine, neededText)
        } else {
            val spaceOnlyRegex = "[^ ]".toRegex()
            val textRange = TextRange.create(startOfLine, startOfLine + neededOffset)
            if (document.getText(textRange).contains(spaceOnlyRegex)) {
                throw RuntimeException("Selector spacing should have bailed out if text before was not clear")
            }
            editor.document.deleteString(textRange.startOffset, textRange.endOffset)
        }

        return true
    }
}

object BlockEnterHandler : OnEnterHandler {
    override fun doIf(editor: Editor, psiElementIn: PsiElement): Boolean {
        val block = psiElementIn.thisOrParentAs(ObjJBlock::class.java) ?: return false
        if (block.openBrace != null && block.closeBrace == null) {
            val lastChild = block.lastChild ?: block.node.treeNext.psi ?: return false
            editor.document.insertString(lastChild.textRange.endOffset, "\n}")
            return true
        }
        return false
    }

}

object ClassEnterHandler : OnEnterHandler {
    override fun doIf(editor: Editor, psiElementIn: PsiElement): Boolean {
        val classDeclaration: ObjJClassDeclarationElement<*>? = psiElementIn.thisOrParentAs(ObjJClassDeclarationElement::class.java) ?: return false
        val hasEnd: Boolean = when (classDeclaration) {
            is ObjJImplementationDeclaration -> classDeclaration.atEnd != null
            is ObjJProtocolDeclaration -> classDeclaration.atEnd != null
            else -> {
                return false
            }
        }

        if (!hasEnd) {
            EditorUtil.insertText(editor, "\n", true)
            EditorUtil.insertText(editor, "@end\n\n", false)
        }
        return false
    }

}