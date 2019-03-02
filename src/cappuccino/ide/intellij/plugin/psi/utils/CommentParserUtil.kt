package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.ObjJComment
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNamedElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.psi.util.PsiTreeUtil
import java.util.logging.Logger
import java.util.regex.Pattern

object CommentParserUtil {

    private val LOGGER: Logger = Logger.getLogger(CommentParserUtil::class.java.canonicalName)
    private val IDENT_REGEX = "[_\$a-zA-Z][_\$a-zA-Z0-9]*";
    private val VARIABLE_TYPE_REGEX = Pattern.compile("[^@]*@var\\s+($IDENT_REGEX)\\s+($IDENT_REGEX).*")
    private val IGNORER_REGEX = Pattern.compile("[^@]*@ignore\\s*([^\n]+)*")

    fun getVariableTypesInParent(element: ObjJNamedElement): String? {
        val varName = element.text
        element.getParentBlockChildrenOfType(PsiCommentImpl::class.java, true)
                .sortedByDescending { it.textRange.startOffset }
                .map { it.text }
                .forEach { comment ->
                    val matcher = VARIABLE_TYPE_REGEX.matcher(comment)
                    if (matcher.find()) {
                        if (!matcher.group(2).equals(varName))
                            return@forEach
                        val type = matcher.group(1)
                        return type;
                    }

                }
        return null
    }

    /**
     * Find if given element has an @ignore comment preceeding it.
     * Recursive in cases where mutliple comments are used in sequence
     */
    fun isIgnored(element:PsiElement) : Boolean {
        var sibling = element.node.getPreviousNonEmptyNode(true);
        while (sibling != null && sibling.elementType in ObjJTokenSets.COMMENTS) {
            val match = IGNORER_REGEX.matcher(sibling.text)
            if (match.find()) {
                return true
            }
            sibling = sibling.getPreviousNonEmptyNode(true);
        }
        if (element is ObjJHasContainingClass) {
            val containingClass = element.containingClass ?: return false
            //return containingClass != element && isIgnored(containingClass)
        }
        return false
    }

}