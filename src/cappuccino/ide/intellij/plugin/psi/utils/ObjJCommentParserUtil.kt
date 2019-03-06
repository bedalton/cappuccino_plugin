package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.ObjJComment
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNamedElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import java.util.logging.Logger
import java.util.regex.Pattern

object ObjJCommentParserUtil {

    private val LOGGER: Logger = Logger.getLogger(ObjJCommentParserUtil::class.java.canonicalName)
    private const val IDENT_REGEX = "[_\$a-zA-Z][_\$a-zA-Z0-9]*"
    private const val IGNORE_FLAG = "@ignore"
    private const val NO_INDEX_FLAG = "@noIndex"
    private val VARIABLE_TYPE_REGEX = Pattern.compile(".*?@var\\s+($IDENT_REGEX)\\s+($IDENT_REGEX).*")
    private val SPACE_REGEX = "\\s+".toRegex()

    /**
     * Gets the variable type if declared in an @var comment
     */
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
     * Gets whether or not to index a given element,
     * @todo have element stubs stash this information
     */
    fun noIndex(elementIn: PsiElement, noIndex: NoIndex): Boolean {
        return checkInInheritedComments(elementIn, true) {
            searchCommentForFlags(it.text, NO_INDEX_FLAG, noIndex.flag)
        }
    }

    /**
     * Find if given element has an @ignore comment preceding it.
     * Recursive in cases where multiple comments are used in sequence
     */
    fun isIgnored(elementIn: PsiElement?, flag: ObjJSuppressInspectionFlags? = null, recursive: Boolean = true): Boolean {
        return ObjJCommentParserUtil.isIgnored(elementIn, flag, null, recursive)
    }

    /**
     * Find if given element has an @ignore comment preceding it.
     * Recursive in cases where multiple comments are used in sequence
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun isIgnored(elementIn: PsiElement?, flag: ObjJSuppressInspectionFlags? = null, requiredMatchingParam: String? = null, recursive: Boolean = true): Boolean {
        return checkInInheritedComments(elementIn, recursive) {
            return@checkInInheritedComments ObjJCommentParserUtil.isIgnored(it.text, flag, requiredMatchingParam)
        }
    }

    private fun isIgnored(text: String, flag: ObjJSuppressInspectionFlags? = null, requiredMatchingParam: String? = null): Boolean {
        return searchCommentForFlags(text, IGNORE_FLAG, flag?.flag, requiredMatchingParam)
    }

    private fun searchCommentForFlags(text: String, prefix: String, flag: String?, param: String? = null): Boolean {
        text.split("\\n".toRegex()).forEach lineForEach@{ line ->
            if (text.contains(prefix)) {
                // Take all text in line after @ignore and tokenize it
                val flags = line.substringAfter(prefix)
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                // if flag is null or there are no actual flags set
                // return default of true
                if (flag == null || flags.isEmpty()) {
                    return true
                }

                // For each flag set in line after @ignore, check for match between flag and param(if any)
                flags.forEach flagForEach@{ tag ->
                    val parts = tag.split(SPACE_REGEX)
                    // Flag doesn't match, keep looking
                    if (parts[0] != flag) {
                        return@flagForEach
                    }
                    // Flag matches and either doesn't need a matching param, flag doesn't have one, or param matches
                    if (param == null || parts.size < 2 || param == parts[1]) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun checkInInheritedComments(elementIn: PsiElement?, recursive: Boolean = true, check: (ASTNode) -> Boolean): Boolean {
        var element: PsiElement? = elementIn ?: return false
        var didCheckContainingClass = false
        while (element != null) {
            var sibling = element.node.getPreviousNonEmptyNode(true)
            while (sibling != null && sibling.elementType in ObjJTokenSets.COMMENTS) {
                if (check(sibling)) {
                    return true
                }
                sibling = sibling.getPreviousNonEmptyNode(true)
            }
            if (recursive && !didCheckContainingClass) {
                val containingClass = (element as? ObjJHasContainingClass)?.containingClass
                if (containingClass != null && !containingClass.isEquivalentTo(elementIn)) {
                    didCheckContainingClass = true
                    if (checkInInheritedComments(containingClass, recursive, check)) {
                        return true
                    }
                }
            }
            if (recursive) {
                element = element.parent
            }
        }
        elementIn.containingFile.getChildrenOfType(PsiCommentImpl::class.java).forEach {
            if (check(it.node)) {
                return true
            }
        }
        elementIn.containingFile.getChildrenOfType(ObjJComment::class.java).forEach {
            if (check(it.node)) {
                return true
            }
        }
        return false
    }

}

enum class ObjJSuppressInspectionFlags(val title:String, val flag:String) {
    IGNORE_METHOD("method declaration", "methodDeclaration"),
    IGNORE_INCOMPATIBLE_METHOD_OVERRIDE("incompatible method override", "incompatibleOverride"),
    IGNORE_RETURN_STATEMENT("return statement", "incompatibleMethodReturn"),
    IGNORE_INVALID_SELECTOR("invalid or undefined method selector", "invalidSelector"),
    IGNORE_UNDECLARED_FUNCTION("possibly undeclared function", "undeclaredFunction"),
    IGNORE_UNDECLARED_VAR("possibly undeclared variable", "undeclaredVar"),
    IGNORE_CLASS("class in completions", "ignoreClass");
}

enum class NoIndex(val title:String, val flag:String) {
    ANY("of any kind for %s", "any"),
    METHOD("this method", "method"),
    FUNCTION("this function", "function"),
    GLOBAL("this global variable", "global"),
    CLASS("this class", "class");

}