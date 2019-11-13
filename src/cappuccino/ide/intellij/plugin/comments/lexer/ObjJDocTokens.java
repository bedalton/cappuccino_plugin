package cappuccino.ide.intellij.plugin.comments.lexer;


import cappuccino.ide.intellij.plugin.comments.ObjJCommentToken;
import cappuccino.ide.intellij.plugin.comments.parser.ObjJDocClassParser;
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.ILazyParseableElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ObjJDocTokens {


    ObjJCommentToken START = new ObjJCommentToken("ObjJDoc_START");
    ObjJCommentToken END = new ObjJCommentToken("ObjJDoc_END");
    ObjJCommentToken LEADING_ASTERISK = new ObjJCommentToken("ObjJDoc+LEADING_ASTERISK");
    ObjJCommentToken tagValueDelim = new ObjJCommentToken("ObjJDoc_NAME_DELIM");

    ObjJCommentToken TEXT = new ObjJCommentToken("ObjJDoc_TEXT");
    ObjJCommentToken CODE_BLOCK_TEXT = new ObjJCommentToken("ObjJDoc_CODE_BLOCK_TEXT");

    ObjJCommentToken TAG_NAME = new ObjJCommentToken("ObjJDoc_TAG_NAME");

    ILazyParseableElementType QUALIFIED_NAME = new ILazyParseableElementType("ObjJDoc_MARKDOWN_LINK", ObjJLanguage.getInstance()) {
        @NotNull
        @Override
        public ASTNode parseContents(
                @NotNull ASTNode chameleon
        ) {
            return ObjJDocClassParser.parseMarkdownLink(this, chameleon);
        }
    };
}
