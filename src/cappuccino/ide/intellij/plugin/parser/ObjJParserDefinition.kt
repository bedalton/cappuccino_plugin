package cappuccino.ide.intellij.plugin.parser

import com.intellij.lang.*
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.tree.*
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.lexer.ObjJLexer
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets.COMMENTS
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.FILE
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
        return ObjJTypes.Factory.createElement(node)
    }

    companion object {
        val WHITE_SPACES = create(TokenType.WHITE_SPACE)
    }
}