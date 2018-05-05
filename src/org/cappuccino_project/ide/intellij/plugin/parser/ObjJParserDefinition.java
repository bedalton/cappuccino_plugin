package org.cappuccino_project.ide.intellij.plugin.parser;

import com.intellij.lang.*;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.*;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.cappuccino_project.ide.intellij.plugin.lexer.ObjJLexer;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.jetbrains.annotations.NotNull;

import static org.cappuccino_project.ide.intellij.plugin.psi.types.TokenSets.*;

public class ObjJParserDefinition  implements ParserDefinition {

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new ObjJLexer();
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return WHITE_SPACES;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @NotNull
    public PsiParser createParser(final Project project) {
        return new ObjectiveJParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return ObjJStubTypes.FILE;
    }

    public PsiFile createFile(FileViewProvider viewProvider) {
        return new ObjJFile(viewProvider);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        return ObjJTypes.Factory.createElement(node);
    }
}