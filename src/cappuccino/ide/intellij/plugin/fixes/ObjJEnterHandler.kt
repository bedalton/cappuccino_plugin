package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.codeStyle.CodeStyleManager
import java.util.logging.Logger
import kotlin.math.min

/**
 * Enter handler delegate to simplify autocompleting elements
 */
@Suppress("unused")
class ObjJEnterHandler : EnterHandlerDelegateAdapter() {

    override fun postProcessEnter(file: PsiFile, editor: Editor, dataContext: DataContext): EnterHandlerDelegate.Result {
        if (file !is ObjJFile) {
            return EnterHandlerDelegate.Result.Continue
        }
        val caretOffset:Int = dataContext.getData(DataKeys.CARET)?.offset ?: return EnterHandlerDelegate.Result.Continue
        val pointer = getPointer(file, caretOffset) ?: return EnterHandlerDelegate.Result.Continue
        if (pointer.element?.text?.trim().isNullOrBlank())
            return EnterHandlerDelegate.Result.Continue
        var totalRange:TextRange? = null
        for (handler in handlers) {
            // Fetch element fresh from pointer each time, hoping that it stays current after modifications
            val element = pointer.element
                    ?: return EnterHandlerDelegate.Result.Continue// bail out if element becomes stale
            val thisRange = handler.doIf(editor, element)
            if (thisRange != null) {
                totalRange = totalRange.max(thisRange)
            }
        }
        if (totalRange != null) {
            CodeStyleManager.getInstance(file.project).reformatTextWithContext(file, listOf(totalRange))
        }
        return EnterHandlerDelegate.Result.Continue
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
                //MethodCallHandler,
                ClassEnterHandler,
                MethodCallEnterHandler
        )
    }
}


/**
 * Interface to implement for enter handlers
 */
internal interface OnEnterHandler {
    fun doIf(editor: Editor, psiElementIn: PsiElement) : TextRange?
}

private object MethodCallEnterHandler : OnEnterHandler {
    override fun doIf(editor: Editor, psiElementIn: PsiElement): TextRange? {
        if (psiElementIn.prevSibling is ObjJMethodCall)
            return psiElementIn.prevSibling.textRange
        if (!psiElementIn.text.contains("([\\]:])".toRegex()))
            return null
        val methodCallParent = psiElementIn.getSelfOrParentOfType(ObjJMethodCall::class.java) ?: return null
        return methodCallParent.textRange
    }

}

/**
 * Should autocomplete implementation and protocol class statements
 * @todo actually make it work in all instances.
 */
object ClassEnterHandler : OnEnterHandler {
    override fun doIf(editor: Editor, psiElementIn: PsiElement): TextRange? {
        val classDeclaration: ObjJClassDeclarationElement<*> = psiElementIn as? ObjJClassDeclarationElement<*>
                ?: return null
        val hasEnd: Boolean = when (classDeclaration) {
            is ObjJImplementationDeclaration -> classDeclaration.atEnd != null
            is ObjJProtocolDeclaration -> classDeclaration.atEnd != null
            else -> false
        }

        if (hasEnd) {
            return null
        }
        EditorUtil.insertText(editor, "\n", true)
        EditorUtil.insertText(editor, "@end\n\n", false)
        return classDeclaration.textRange
    }

}

private fun TextRange?.max(otherRange:TextRange?) : TextRange? {
    if (this == null)
        return otherRange
    if (otherRange == null)
        return this
    val start = min(this.startOffset, otherRange.startOffset)
    val end = kotlin.math.max(this.endOffset, otherRange.endOffset)
    return TextRange(start, end)
}