package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

fun PsiElement.getContainingComment() : PsiElement? {
    var parentNode:ASTNode? = this.node
    // Loop through parent nodes checking if previous node is a comment node
    while(parentNode != null) {
        // Get previous node
        val prevNode = parentNode.getPreviousNonEmptyNode(true)
        // Check if prev node is comment
        if (prevNode != null && prevNode.elementType in ObjJTokenSets.COMMENTS) {
            return prevNode.psi
        }
        // Get parent element for checking prev node
        parentNode = parentNode.treeParent
    }
    return null
}

val PsiElement.docComment:CommentWrapper? get() {
    val commentText = getContainingComment()?.text?.trim()
            ?.removePrefix("/*!")
            ?.removePrefix("/*")
            ?.removeSuffix("*/")
            ?.trim()
    if (commentText == null) {
        return null
    }
    return CommentWrapper(commentText)
}

private val newLineRegex = "\n".toRegex()


/**
 * Wrapper class for organizing and parsing doc comments
 */
data class CommentWrapper(val commentText:String) {
    private val lines:List<String> by lazy {
        commentText.split(newLineRegex)
                .map{
                    it.trim()
                }
                .filter {
                    it.isNotBlank()
                }
    }
    val parameterComments:List<CommentParam> by lazy {
        val paramLines = lines
                .filter {
                    it.startsWith("@param")
                }
                .map {
                    val tokens:List<String> = it.split("\\s+".toRegex(), 3)
                    CommentParam(tokens.getOrElse(1) { "_" }, tokens.getOrNull(2))
                }
        paramLines
    }

    val returnParameterComment:String? by lazy {
        val line = lines.filter { it.startsWith("@return") }.firstOrNull()
        line?.trim()?.split("\\s+".toRegex(), 2)?.getOrNull(1)
    }

    val deprecated:Boolean by lazy {
        deprecationWarning.isNotNullOrBlank()
    }

    val deprecationWarning:String? by lazy {
        lines.first {
            it.trim().startsWith("@deprecated")
        }
    }

    /**
     * Gets parameter comment based on parameter name
     */
    fun getParameterComment(name:String) : CommentParam? {
        return parameterComments.firstOrNull {
            it.paramName == name
        }
    }

    /**
     * Gets parameter comment based on index
     * Note:: May not be accurate if doc comment does not
     *        maintain order or does not define all parameters
     */
    fun getParameterComment(index:Int) : CommentParam? {
        return parameterComments.getOrNull(index)
    }
}

data class CommentParam(val paramName:String, val paramComment:String?)