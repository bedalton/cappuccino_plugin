package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.psi.ObjJFrameworkDescriptor
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.ObjJObjectLiteral
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import cappuccino.ide.intellij.plugin.utils.orTrue
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
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

    private val beforeKeyPressedHandler: List<BeforeKeyPressHandler> = listOf(
            SlashPressedHandler,
            DoubleButtonPressHandler
    )

    override fun beforeCharTyped(thisChar: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
        val caretPosition = editor.caretModel.offset
        val tempOriginalElement = try {
            file.findElementAt(caretPosition - 1)
        } catch (_: Exception) {
            null
        }

        val lastChar = try {
            editor.document.text.substring(caretPosition-1, caretPosition).firstOrNull()
        } catch (e:Exception) {
            null
        }
        val nextChar = try {
            editor.document.text.substring(caretPosition, caretPosition+1).firstOrNull()
        } catch (e:Exception) {
            null
        }

        // Check if anything should handle keypress and return if none do
        // This ensures that there is not a lot of cpu wasted
        // finding a compatible element when no one will handle it
        val handlers = beforeKeyPressedHandler.filter {
            thisChar in it.keysToHandle &&
                    (lastChar == null || it.prevCharRequired?.contains(lastChar).orTrue()) &&
                    (nextChar == null || it.nextCharRequired?.contains(nextChar).orTrue())
        }
        if (handlers.isEmpty()) {
            return Result.CONTINUE
        }
        val tempElement: ObjJCompositeElement = (when {
            tempOriginalElement is ObjJCompositeElement -> tempOriginalElement
            tempOriginalElement?.parent is ObjJCompositeElement -> tempOriginalElement.parent as ObjJCompositeElement
            else -> null
        }) ?: return Result.CONTINUE
        val elementPointer: SmartPsiElementPointer<ObjJCompositeElement> = SmartPointerManager.createPointer(tempElement)
        for (handler in handlers) {
            val element = elementPointer.element ?: return Result.DEFAULT
            if (thisChar in handler.keysToHandle && handler.handle(element, thisChar, lastChar, nextChar, project, editor, file) == Result.STOP) {
                return Result.STOP
            }
        }
        return Result.CONTINUE
    }

    override fun charTyped(thisChar: Char, project: Project, editor: Editor, file: PsiFile): Result {
        val caretPosition = editor.caretModel.offset

        val lastPrevChar = editor.document.text.substring(caretPosition-1, caretPosition).firstOrNull()
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


private interface BeforeKeyPressHandler {
    val keysToHandle: List<Char>
    val prevCharRequired:List<Char>?
    val nextCharRequired:List<Char>?
    fun handle(element: ObjJCompositeElement, char:Char, prevChar:Char?, nextChar:Char?, project: Project, editor: Editor, file: PsiFile): Result
}

private object SlashPressedHandler : BeforeKeyPressHandler {
    override val keysToHandle: List<Char> = listOf('/')
    override val prevCharRequired: List<Char>? = null
    override val nextCharRequired:List<Char>? = null

    override fun handle(element: ObjJCompositeElement, char:Char, prevChar:Char?, nextChar:Char?, project: Project, editor: Editor, file: PsiFile): Result {
        if (!element.hasParentOfType(ObjJFrameworkDescriptor::class.java))
            return Result.DEFAULT
        EditorUtil.offsetCaret(editor, 1)
        return Result.STOP
    }
}


private object DoubleButtonPressHandler : BeforeKeyPressHandler {
    override val keysToHandle: List<Char> = "/()<>.".toList()
    override val prevCharRequired: List<Char>? = null
    override val nextCharRequired:List<Char>? = "/()<>.".toList()

    override fun handle(element: ObjJCompositeElement, char:Char, prevChar:Char?, nextChar:Char?, project: Project, editor: Editor, file: PsiFile): Result {
        if (char == nextChar) {
            editor.caretModel.currentCaret.moveToOffset(editor.caretModel.currentCaret.offset + 1)
            return Result.STOP
        }
        return Result.CONTINUE
    }
}