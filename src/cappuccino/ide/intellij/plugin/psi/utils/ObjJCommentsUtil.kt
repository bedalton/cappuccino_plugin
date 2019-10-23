package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.inference.withoutAnyType
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

fun PsiElement.getContainingComments(): List<String> {
    val out: MutableList<String> = mutableListOf()
    var parentNode: ASTNode? = this.node
    //LOGGER.info("Get Containing Comments")
    // Loop through parent nodes checking if previous node is a comment node
    while (parentNode != null) {
        // Get previous node
        val prevNode = parentNode.getPreviousNonEmptyNode(true)
        // Check if prev node is comment
        if (prevNode != null && prevNode.elementType in ObjJTokenSets.COMMENTS) {
            var nextNode: ASTNode? = prevNode
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

val PsiElement.docComment: CommentWrapper?
    get() {
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
@Suppress("MemberVisibilityCanBePrivate")
data class CommentWrapper(val commentText: String) {
    private val lines: List<String> by lazy {
        commentText.split(newLineRegex)
                .map {
                    it.trim()
                }
                .filter {
                    it.isNotBlank()
                }
    }
    val parameterComments: List<CommentParam> by lazy {
        val paramLines = lines
                .filter {
                    it.startsWith("@param")
                }
                .map {
                    val tokens: List<String> = it.split("\\s+".toRegex(), 3)

                    CommentParam(tokens.getOrElse(0) { "_" }, it)
                }
        paramLines
    }

    val returnParameterComment: CommentParam? by lazy {
        val line = lines.firstOrNull { it.startsWith("@return") }
        if (line.isNotNullOrBlank()) {
            CommentParam("@return", line)
        } else {
            null
        }
    }

    fun getReturnTypes(project: Project): Set<String>? {
        return returnParameterComment?.getTypes(project)
    }

    val deprecated: Boolean by lazy {
        deprecationWarning.isNotNullOrBlank()
    }

    val deprecationWarning: String? by lazy {
        lines.first {
            it.trim().startsWith("@deprecated")
        }
    }

    /**
     * Gets parameter comment based on parameter name
     */
    fun getParameterComment(name: String): CommentParam? {
        return parameterComments.firstOrNull {
            it.paramName == name
        }
    }

    /**
     * Gets parameter comment based on index
     * Note:: May not be accurate if doc comment does not
     *        maintain order or does not define all parameters
     */
    fun getParameterComment(index: Int): CommentParam? {
        return parameterComments.getOrNull(index)
    }
}

data class CommentParam(val paramName: String, private val paramCommentIn: String?) {

    val commentStringTrimmed by lazy {
        paramCommentIn?.trim()?.replace("^@?param\\s*|@?return[s]?\\s*(the\\s*)?".toRegex(), "")?.trim()
    }

    val paramCommentClean: String? by lazy {
        paramCommentIn?.replace("""\s*\\c\s*""".toRegex(), " ")
    }

    val paramCommentFormatted: String? by lazy {
        val pattern = """\s*\\c\s+([^ $]+)""".toRegex()
        paramCommentIn?.replace(pattern) {
            "<strong>$it</strong>"
        }
    }

    val possibleClassStrings: Set<String> by lazy {
        val commentStringTrimmed = this.commentStringTrimmed
                ?: return@lazy emptySet<String>()

        val out = mutableSetOf<String>()
        listOf(CLASS_NAME_REGEX).forEach { pattern ->
            val matcher = pattern.matcher(commentStringTrimmed)
            while (matcher.find()) {
                if (matcher.groupCount() < 2) {
                    continue
                }
                out.add(matcher.group(1).trim())
            }
        }
        out
    }

    /**
     * Gets class types if matching, null otherwise
     */
    fun getTypes(project: Project, matchTypeFilter: ClassMatchType? = null): Set<String>? {

        if (paramCommentIn == null)
            return null
        val classes = if (matchTypeFilter == null || matchTypeFilter == ClassMatchType.OBJJ)
            ObjJClassDeclarationsIndex.instance.getAllKeys(project).withoutAnyType().toSet()
        else
            emptySet()

        val jsClasses = if (matchTypeFilter == null || matchTypeFilter == ClassMatchType.JS)
            JsTypeDefClassesByNamespaceIndex.instance.getAllKeys(project).toSet()
        else
            emptySet()


        val firstIn = commentStringTrimmed.orEmpty().split("\\s+".toRegex(), 2).first()
        if (matchType(firstIn, classes, jsClasses) != null)
            return setOf(firstIn)

        val jsMatches = mutableSetOf<String>()
        val objJMatches = mutableSetOf<String>()
        possibleClassStrings.forEach {
            val matchType = matchType(it, classes, jsClasses) ?: return@forEach
            if (matchTypeFilter != null && matchTypeFilter != matchType)
                return@forEach
            else if (matchType == ClassMatchType.OBJJ)
                objJMatches.add(it)
            if (matchType == ClassMatchType.JS)
                jsMatches.add(it)
        }

        // Return correct set of matches
        val out = when (matchTypeFilter) {
            ClassMatchType.OBJJ -> objJMatches
            ClassMatchType.JS -> jsMatches
            else -> objJMatches + jsMatches
        }

        // Return matches if not empty, null otherwise
        return if (out.isNotEmpty())
            out
        else
            null
    }
}

fun jsTypesMinusCPPrefix(jsClassNames: Set<String>): Set<String> = jsClassNames.map { it.removePrefix("CP").removePrefix("CF") }.toSet()


private val CLASS_NAME_REGEX = "([a-zA-Z_$][a-zA-Z0-9_]*)".toPattern()

private fun matchType(className: String, objjClassNames: Set<String>, jsClassNames: Set<String>): ClassMatchType? {
    return when (className) {
        in objjClassNames -> ClassMatchType.OBJJ
        in jsClassNames -> ClassMatchType.JS
        else -> if (jsClassNames.isNotEmpty() && className in jsTypesMinusCPPrefix(jsClassNames)) ClassMatchType.JS else null
    }

}

enum class ClassMatchType {
    OBJJ,
    JS
}
