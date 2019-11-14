package cappuccino.ide.intellij.plugin.comments.lexer;

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage;
import cappuccino.ide.intellij.plugin.psi.types.ObjJElementType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
public class ObjJDocCommentTokenType extends IElementType {

    public ObjJDocCommentTokenType(
            @NotNull
                    String debug) {
        super(debug, ObjJLanguage.getInstance());
    }
}
