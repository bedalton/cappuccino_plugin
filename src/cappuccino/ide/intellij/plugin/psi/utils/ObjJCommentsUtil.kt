package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.inference.SPLIT_JS_CLASS_TYPES_LIST_REGEX
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

fun PsiElement.getContainingComments() : List<String> {
    val out:MutableList<String> = mutableListOf()
    var parentNode:ASTNode? = this.node
    //LOGGER.info("Get Containing Comments")
    // Loop through parent nodes checking if previous node is a comment node
    while(parentNode != null) {
        // Get previous node
        val prevNode = parentNode.getPreviousNonEmptyNode(true)
        // Check if prev node is comment
        if (prevNode != null && prevNode.elementType in ObjJTokenSets.COMMENTS) {
            var nextNode:ASTNode? = prevNode
            while (nextNode != null && nextNode.elementType in ObjJTokenSets.COMMENTS) {
                out.add(0, nextNode.text)
                nextNode = prevNode.treePrev
            }
            return out
        }
        // Get parent element for checking prev node
        parentNode = parentNode.treeParent
    }
    return emptyList()
}

val PsiElement.docComment:CommentWrapper? get() {
    val commentText = getContainingComments().joinToString("\n").trim()
            .removePrefix("/*!")
            .removePrefix("/*")
            .removeSuffix("*/")
            .removePrefix("//")
            .removePrefix(" ")
            .trim()
    if (commentText.isBlank())
        return null
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
                    CommentParam(tokens.getOrElse(0) { "_" }, tokens.getOrElse(1){ "?" }, tokens.getOrNull(2))
                }
        paramLines
    }

    val returnParameterComment:String? by lazy {
        val line = lines.filter { it.startsWith("@return") }.firstOrNull()
        line?.trim()?.split("\\s+".toRegex(), 2)?.getOrNull(1)
    }

    val returnTypes:Set<String>? by lazy {
        returnParameterComment?.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX)?.toSet()
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

data class CommentParam(val paramName:String, val type:String?, val paramComment:String?) {
    val types:Set<String>? get() {
        return type?.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX)?.toSet()
    }
}