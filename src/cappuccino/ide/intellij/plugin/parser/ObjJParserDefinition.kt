package cappuccino.ide.intellij.plugin.parser

import cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocCommentParsableBlockToken
import cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocCommentParsableBlockToken.OBJJ_DOC_COMMENT_PARSABLE_BLOCK
import cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocCommentTypes
import cappuccino.ide.intellij.plugin.comments.psi.ObjJDocCommentElementTypeFactory
import cappuccino.ide.intellij.plugin.comments.psi.api.ObjJDocCommentElement
import cappuccino.ide.intellij.plugin.comments.psi.impl.ObjJDocCommentCommentImpl
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.lexer.ObjJLexer
import cappuccino.ide.intellij.plugin.psi.impl.ObjJBlockCommentImpl
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets.COMMENTS
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.FILE
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.tree.TokenSet.EMPTY
import com.intellij.psi.tree.TokenSet.create

class ObjJParserDefinition : ParserDefinition {

    override fun createLexer(project: Project): Lexer {
        return ObjJLexer()
    }

    override fun getWhitespaceTokens(): TokenSet {
        return WHITE_SPACES
    }

    override fun getCommentTokens(): TokenSet {
        return COMMENTS
    }

    override fun getStringLiteralElements(): TokenSet {
        return EMPTY
    }

    override fun createParser(project: Project): PsiParser {
        return ObjectiveJParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return ObjJFile(viewProvider)
    }

    override fun spaceExistanceTypeBetweenTokens(left: ASTNode, right: ASTNode): ParserDefinition.SpaceRequirements {
        return ParserDefinition.SpaceRequirements.MAY
    }

    override fun createElement(node: ASTNode): PsiElement {
        return when {
            node.elementType == OBJJ_DOC_COMMENT_PARSABLE_BLOCK -> ObjJDocCommentCommentImpl(node)
            node.elementType.toString().contains("ObjJDocComment_") -> ObjJDocCommentTypes.Factory.createElement(node)
            else -> ObjJTypes.Factory.createElement(node)
        }
    }

    companion object {
        val WHITE_SPACES = create(TokenType.WHITE_SPACE)
    }
}