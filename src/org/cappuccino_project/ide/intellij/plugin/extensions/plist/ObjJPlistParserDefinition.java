package org.cappuccino_project.ide.intellij.plugin.extensions.plist;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.cappuccino_project.ide.intellij.plugin.extensions.plist.lexer.ObjJPlistLexer;
import org.cappuccino_project.ide.intellij.plugin.extensions.plist.parser.ObjJPlistParser;
import org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi.types.ObjJPlistTypes;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.jetbrains.annotations.NotNull;

public class ObjJPlistParserDefinition implements ParserDefinition {
    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet COMMENTS = TokenSet.create(ObjJPlistTypes.ObjJPlist_COMMENT);
    public static final IFileElementType FILE = new IFileElementType(ObjJPlistLanguage.INSTANCE);
    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new ObjJPlistLexer();
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
        return new ObjJPlistParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    public PsiFile createFile(FileViewProvider viewProvider) {
        return new ObjJPlistFile(viewProvider);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        return ObjJTypes.Factory.createElement(node);
    }
}