package cappuccino.ide.intellij.plugin.jstypedef.parser

import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.lang.*
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.tree.*
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.lexer.ObjJLexer
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import com.intellij.psi.tree.TokenSet.EMPTY

class JsTypeDefParserDefinition : ParserDefinition {

    override fun createLexer(project: Project): Lexer {
        return ObjJLexer()
    }

    override fun getWhitespaceTokens(): TokenSet {
        return WHITE_SPACES
    }

    override fun getCommentTokens(): TokenSet {
        return ObjJTokenSets.COMMENTS
    }

    override fun getStringLiteralElements(): TokenSet {
        return EMPTY
    }

    override fun createParser(project: Project): PsiParser {
        return JsTypeDefParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return JsTypeDefStubTypes.JS_FILE
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return ObjJFile(viewProvider)
    }

    override fun spaceExistanceTypeBetweenTokens(left: ASTNode, right: ASTNode): ParserDefinition.SpaceRequirements {
        return ParserDefinition.SpaceRequirements.MAY
    }

    override fun createElement(node: ASTNode): PsiElement {
        return JsTypeDefTypes.Factory.createElement(node)
    }

    companion object {
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
    }
}