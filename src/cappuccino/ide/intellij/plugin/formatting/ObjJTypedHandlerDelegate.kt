package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.ObjJObjectLiteral
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.orTrue
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.codeStyle.CodeStyleManager

/**
 * Class to handle key presses to allow formatting on certain keys
 */
class ObjJTypedHandlerDelegate : TypedHandlerDelegate() {

    private val keyPressHandlers: List<KeyPressHandler> = listOf(
            MethodCallKeyPressHandler,
            BlockCloseKeypressHandler,
            ClassKeypressHandler
    )

    override fun charTyped(thisChar: Char, project: Project, editor: Editor, file: PsiFile): Result {
        val caretPosition = editor.caretModel.offset
        var tempOriginalElement = try {
            file.findElementAt(caretPosition - 1)
        } catch (_: Exception) {
            null
        }
        if (tempOriginalElement?.tokenType() in ObjJTokenSets.WHITE_SPACE) {
            tempOriginalElement = tempOriginalElement?.getPreviousNonEmptySibling(true)
        }

        if (tempOriginalElement == null) {
            return Result.CONTINUE
        }
        val lastPrevChar = getLastPrevChar(tempOriginalElement, caretPosition)
        // Check if anything should handle keypress and return if none do
        // This ensures that there is not a lot of cpu wasted
        // finding a compatible element when no one will handle it
        val handlers = keyPressHandlers.filter {
            thisChar in it.keysToHandle &&
                    (lastPrevChar == null || it.prevCharRequired?.contains(lastPrevChar).orTrue())
        }
        if (handlers.isEmpty()) {
            return Result.CONTINUE
        }
        val tempElement: ObjJCompositeElement = (if (tempOriginalElement is ObjJCompositeElement)
            tempOriginalElement
        else if (tempOriginalElement.parent is ObjJCompositeElement)
            tempOriginalElement.parent as ObjJCompositeElement
        else {
            val prevSibling = tempOriginalElement.getPreviousNonEmptySibling(false)
            if (prevSibling is ObjJCompositeElement)
                prevSibling
            else
                prevSibling?.parent as? ObjJCompositeElement
        }) ?: return Result.CONTINUE
        val elementPointer: SmartPsiElementPointer<ObjJCompositeElement> = SmartPointerManager.createPointer(tempElement)
        val originalElementPointer = SmartPointerManager.createPointer(tempOriginalElement)
        for (handler in handlers) {
            val element = elementPointer.element ?: return Result.DEFAULT
            if (thisChar in handler.keysToHandle && handler.handle(element, originalElementPointer.element, project, editor, file)) {
                break
            }
        }
        return Result.CONTINUE
    }

    private fun getLastPrevChar(tempOriginalElement:PsiElement, caretPosition:Int) : Char? {
        var element:PsiElement? = tempOriginalElement
        while (element != null && element.textRange.startOffset > caretPosition) {
            element = element.previous
        }
        if (element == null)
            element = tempOriginalElement
        while (element != null && element.textRange.endOffset > caretPosition) {
            element = element.next
        }
        if (element == null)
            return null
        val offsetToCharInElement = caretPosition - element.textRange.startOffset
        return element.textToCharArray().getOrNull(offsetToCharInElement)
    }
}

private interface KeyPressHandler {
    val keysToHandle: List<Char>
    val prevCharRequired:List<Char>?
    fun handle(element: ObjJCompositeElement, originalElement:PsiElement?, project: Project, editor: Editor, file: PsiFile): Boolean
}

private object MethodCallKeyPressHandler : KeyPressHandler {
    override val keysToHandle: List<Char> = listOf(':', ']')
    override val prevCharRequired: List<Char>? = null

    override fun handle(element: ObjJCompositeElement, originalElement: PsiElement?, project: Project, editor: Editor, file: PsiFile): Boolean {
        val toReformat = element.getSelfOrParentOfType(ObjJMethodCall::class.java)
                ?: element.getSelfOrParentOfType(ObjJMethodHeaderDeclaration::class.java)
                ?: element.getSelfOrParentOfType(ObjJObjectLiteral::class.java)
                ?: return false
        CodeStyleManager.getInstance(file.project).reformatTextWithContext(file, listOf(toReformat.textRange))
        return true
    }

}

private object BlockCloseKeypressHandler : KeyPressHandler {
    override val keysToHandle: List<Char> = listOf('}')
    override val prevCharRequired: List<Char>? = null

    override fun handle(element: ObjJCompositeElement, originalElement: PsiElement?, project: Project, editor: Editor, file: PsiFile): Boolean {
        val block = element.getSelfOrParentOfType(ObjJBlock::class.java)
                ?: element.getSelfOrParentOfType(ObjJObjectLiteral::class.java)
                ?: return false
        CodeStyleManager.getInstance(file.project).reformatTextWithContext(file, listOf(block.textRange))
        return true
    }
}

private object ClassKeypressHandler : KeyPressHandler {
    override val keysToHandle: List<Char> = listOf ('d')
    override val prevCharRequired: List<Char> = listOf('n')

    override fun handle(element: ObjJCompositeElement, originalElement: PsiElement?, project: Project, editor: Editor, file: PsiFile): Boolean {
        if (originalElement?.elementType == ObjJTypes.ObjJ_AT_END) {
            val classElement = element.getSelfOrParentOfType(ObjJClassDeclarationElement::class.java) ?: return false
            CodeStyleManager.getInstance(file.project).reformatTextWithContext(file, listOf(classElement.textRange))
            return true
        }
        return false
    }

}