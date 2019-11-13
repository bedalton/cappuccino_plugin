package cappuccino.ide.intellij.plugin.comments.lexer;

import cappuccino.ide.intellij.plugin.comments.parser.ObjJDocQualifiedNameParser;
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage;
import cappuccino.ide.intellij.plugin.psi.types.ObjJElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.ILazyParseableElementType;
import org.jetbrains.annotations.NotNull;
public class ObjJDocCommentTokenType extends ObjJElementType {


    public ObjJDocCommentTokenType(
            @NotNull
                    String debug) {
        super(debug);
    }
}
