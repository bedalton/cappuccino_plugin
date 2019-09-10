package cappuccino.ide.intellij.plugin.jstypedef.parser

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.jstypedef.lexer.JsTypeDefLexer
import cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
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

class JsTypeDefParserDefinition : ParserDefinition {

    override fun createLexer(project: Project): Lexer {
        return JsTypeDefLexer()
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
        return JsTypeDefFile(viewProvider)
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