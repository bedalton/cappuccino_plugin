package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.comments.parser.ObjJDocCommentKnownTag
import cappuccino.ide.intellij.plugin.comments.psi.impl.ObjJDocCommentParsableBlock
import cappuccino.ide.intellij.plugin.comments.psi.stubs.ObjJDocCommentTagLineStruct
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.combine
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.orTrue
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement

fun PsiElement.getContainingComments(): List<PsiComment> {
    val out: MutableList<PsiComment> = mutableListOf()
    var parentNode: ASTNode? = this.node
    //LOGGER.info("Get Containing Comments")
    // Loop through parent nodes checking if previous node is a comment node
    while (parentNode != null) {
        // Get previous node
        val prevNode = parentNode.getPreviousNonEmptyNode(true)
        // Check if prev node is comment
        if (prevNode != null && prevNode.elementType in ObjJTokenSets.COMMENTS) {
            var nextNode: ASTNode? = prevNode
            while (nextNode != null && nextNode.psi is PsiComment) {
                out.add(0, nextNode.psi as PsiComment)
                nextNode = prevNode.treePrev
            }
            return out
        }
        // Get parent element for checking prev node
        parentNode = parentNode.treeParent
    }
    return emptyList()
}

val PsiElement.docComment: CommentWrapper?
    get() {
        val containingComments = getContainingComments()
                .filterIsInstance(ObjJDocCommentParsableBlock::class.java)
        val tagLines = containingComments.flatMap {
            it.tagLinesAsStructs
        }
        val commentText = containingComments.flatMap {
            it.textLines
        }.joinToString("\n")
        val returnType = containingComments.mapNotNull {
            it.returnType
        }.combine()
        if (commentText.isBlank() && tagLines.isEmpty())
            return null
        return CommentWrapper(commentText, tagLines, returnType)
    }

/**
 * Wrapper class for organizing and parsing doc comments
 */
@Suppress("MemberVisibilityCanBePrivate")
data class CommentWrapper(
        val commentText: String,
        val tagLines:List<ObjJDocCommentTagLineStruct>,
        val returnType:InferenceResult?) {

    val parameterComments: List<ObjJDocCommentTagLineStruct> by lazy {
        val parameterLines = tagLines.filter {
            it.tag == ObjJDocCommentKnownTag.VAR || it.tag == ObjJDocCommentKnownTag.PARAM
        }
        parameterLines
    }

    fun getReturnTypes(): InferenceResult? {
        return if(returnType?.types?.isEmpty().orTrue())
            return null
        else
            returnType
    }

    val deprecated: Boolean by lazy {
        deprecationWarning.isNotNullOrBlank()
    }

    val deprecationWarning: String? by lazy {
        tagLines.firstOrNull {
            it.tag == ObjJDocCommentKnownTag.DEPRECATED
        }?.text
    }

    /**
     * Gets parameter comment based on parameter name
     */
    fun getParameterComment(name: String): ObjJDocCommentTagLineStruct? {
        return parameterComments.firstOrNull {
            it.name == name
        }
    }

    /**
     * Gets parameter comment based on index
     * Note:: May not be accurate if doc comment does not
     *        maintain order or does not define all parameters
     */
    fun getParameterComment(index: Int): ObjJDocCommentTagLineStruct? {
        return parameterComments.getOrNull(index)
    }
}