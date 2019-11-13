package cappuccino.ide.intellij.plugin.comments.parser

import cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocLexer
import cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocCommentTokenType
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilderFactory
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

/**
 * Parses the contents of a Markdown link in KDoc. Uses the standard Kotlin lexer.
 */
class ObjJDocQualifiedNameParser : PsiParser {
    companion object {
        @JvmStatic fun parseQualifiedName(root: IElementType, chameleon: ASTNode): ASTNode {
            val parentElement = chameleon.treeParent.psi
            val project = parentElement.project
            val builder = PsiBuilderFactory.getInstance().createBuilder(project,
                    chameleon,
                    ObjJDocLexer(),
                    root.language,
                    chameleon.text)
            val parser = ObjJDocQualifiedNameParser()

            return parser.parse(root, builder).firstChildNode
        }
    }

    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        val hasTagDelim = builder.tokenType == ObjJDocCommentTokenType.TAG_VALUE_DELIMITER
        if (hasTagDelim) {
            builder.advanceLexer()
        }
        parseQualifiedName(builder)
        if (!builder.eof()) {
            builder.error("Expression expected")
            while (!builder.eof()) {
                builder.advanceLexer()
            }
        }
        rootMarker.done(root)
        return builder.treeBuilt
    }

    private fun parseQualifiedName(builder: PsiBuilder) {
        var marker = builder.mark()
        while (true) {
            // don't highlight a word in a link as an error if it happens to be a Kotlin keyword
            if (!isName(builder.tokenType)) {
                marker.drop()
                builder.error("Identifier expected")
                break
            }
            builder.advanceLexer()
            marker.done(ObjJDocElementTypes.ObjJDoc_NAME)
            if (builder.tokenType != ObjJTypes.ObjJ_DOT) {
                break
            }
            marker = marker.precede()
            builder.advanceLexer()
        }
    }

    private fun isName(tokenType: IElementType?) = tokenType == ObjJTypes.ObjJ_ID;
}