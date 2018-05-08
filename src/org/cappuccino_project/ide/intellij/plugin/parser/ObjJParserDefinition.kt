package org.cappuccino_project.ide.intellij.plugin.parser

import com.intellij.lang.*
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.tree.*
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile
import org.cappuccino_project.ide.intellij.plugin.lexer.ObjJLexer
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes

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
        return TokenSet.EMPTY
    }

    override fun createParser(project: Project): PsiParser {
        return ObjectiveJParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return ObjJStubTypes.FILE
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
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        val COMMENTS = TokenSet.create(ObjJTypes.ObjJ_SINGLE_LINE_COMMENT, ObjJTypes.ObjJ_BLOCK_COMMENT)
    }
}