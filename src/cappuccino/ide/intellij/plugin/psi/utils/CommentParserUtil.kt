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
    private val VARIABLE_TYPE_REGEX = Pattern.compile(".*?@var\\s+($IDENT_REGEX)\\s+($IDENT_REGEX).*")
    private val IGNORER_REGEX = Pattern.compile(".*?(?=@ignore)?@ignore\\s*(.*)")

    fun getVariableTypesInParent(element: ObjJNamedElement): String? {
        val varName = element.text
        element.getParentBlockChildrenOfType(PsiCommentImpl::class.java, true)
                .sortedByDescending { it.textRange.startOffset }
                .map { it.text }
                .forEach { comment ->
                    val matcher = VARIABLE_TYPE_REGEX.matcher(comment)
                    if (matcher.find()) {
                        if (matcher.group(2) != varName)
                            return@forEach
                        val type = matcher.group(1)
                        LOGGER.info("Found type in comment $element.text is $type")
                        return type
                    }

                }
        return null
    }

    /**
     * Find if given element has an @ignore comment preceeding it.
     * Recursive in cases where mutliple comments are used in sequence
     */
    fun isIgnored(elementIn:PsiElement?, flag:IgnoreFlags? = null, recursive:Boolean = true) : Boolean {
        var element: PsiElement? = elementIn ?: return false
        var didCheckContainingClass = false
        while (element != null) {
            var sibling = element.node.getPreviousNonEmptyNode(true)
            while (sibling != null && sibling.elementType in ObjJTokenSets.COMMENTS) {
                sibling.text.split("\\n".toRegex()).forEach { it ->
                    val match = IGNORER_REGEX.matcher(it)
                    if (match.find()) {
                        if (flag == null) {
                            return true
                        }
                        val flags = match.group(1).trim().split("\\s+".toRegex()).filter { flag -> flag.trim().isNotEmpty() }
                        if (flags.isEmpty()) {
                            return true
                        }
                        for (flagString in flags) {
                            LOGGER.info("Found Flag: $flagString")
                            if (flagString == flag.flag) {
                                return true
                            }
                        }
                    }
                }
                sibling = sibling.getPreviousNonEmptyNode(true);
            }

            val containingClass = (element as? ObjJHasContainingClass)?.containingClass
            if (recursive && !didCheckContainingClass && containingClass != null && !containingClass.isEquivalentTo(elementIn)) {
                didCheckContainingClass = true;
                isIgnored(containingClass, flag, recursive)
            }
            element = element.parent
        }
        return false
    }

}

enum class IgnoreFlags(val flag:String) {
    IGNORE_METHOD("#ignoreMethodDeclaration"),
    IGNORE_RETURN("#ignoreMethodReturn"),
    IGNORE_SIGNATURE("#ignoreMethodSignature"),
    IGNORE_UNDECLARED("#ignoreUndeclared")
}