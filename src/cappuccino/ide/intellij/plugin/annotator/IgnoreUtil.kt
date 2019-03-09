package cappuccino.ide.intellij.plugin.annotator

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import cappuccino.ide.intellij.plugin.psi.ObjJComment
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasIgnoreStatements
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import java.util.logging.Logger

object IgnoreUtil {
    private val LOGGER = Logger.getLogger(IgnoreUtil::class.java.name)
    private val IGNORE_KEYWORD = "ignore"
    private val IN_KEYWORD = "in"

    internal fun shouldIgnore(elementToPossiblyIgnore: ObjJCompositeElement, ignoreElementType: ElementType): Boolean {
        var commentElement = getPrecedingComment(elementToPossiblyIgnore)
        while (commentElement != null) {
            var commentText = commentElement.text.trim { it <= ' ' }.toLowerCase()
            if (commentText.startsWith("ignore")) {
                commentElement = getPrecedingComment(commentElement)
                continue
            }
            commentText = commentText.substring(IGNORE_KEYWORD.length)
            val parts = commentText.split(IN_KEYWORD.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (kind in parts[0].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                if (kind.trim { it <= ' ' } == ElementType.ALL.type || kind.trim { it <= ' ' } == ignoreElementType.type) {
                    return true
                }
            }
            return true
        }
        return false
    }

    private fun getPrecedingComment(rootElementIn: PsiElement): ObjJComment? {
        var rootElement = rootElementIn
        var prev = rootElement.getPreviousNonEmptySibling(true)
        if (prev is ObjJComment) {
            return rootElement.prevSibling as ObjJComment
        }

        while (rootElement.parent != null && rootElement.parent !is ObjJHasIgnoreStatements && rootElement.parent !is PsiFile) {
            rootElement = rootElement.parent
        }
        prev = rootElement.getPreviousNonEmptySibling(true)
        if (prev !is ObjJComment) {
            return null
        }
        //LOGGER.log(Level.INFO, "Prev sibling is of comment type.")
        return prev
    }

    enum class ElementType private constructor(val type: String) {
        VAR("undeclaredVar"),
        METHOD("missingMethod"),
        FUNCTION("missingFunction"),
        CALL_TARGET("callTarget"),
        METHOD_SCOPE("methodScope"),
        ALL("all")
    }

    enum class Scope private constructor(val scope: String) {
        FILE("file"),
        BLOCK("block"),
        EXPR("expr"),
        CLASS("class"),
        METHOD("method")
    }
}
