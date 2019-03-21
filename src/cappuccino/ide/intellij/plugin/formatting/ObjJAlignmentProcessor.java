package cappuccino.ide.intellij.plugin.formatting;

import com.intellij.formatting.Alignment;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

import static cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.ObjJ_TERNARY_EXPR_PRIME;


/**
 */
public class ObjJAlignmentProcessor {
    private final ASTNode myNode;
    private final Alignment myBaseAlignment;
    private final CommonCodeStyleSettings mySettings;

    public ObjJAlignmentProcessor(ASTNode node, CommonCodeStyleSettings settings) {
        myNode = node;
        mySettings = settings;
        myBaseAlignment = Alignment.createAlignment();
    }

    @Nullable
    public Alignment createChildAlignment() {
        IElementType elementType = myNode.getElementType();

        if (elementType == ObjJ_TERNARY_EXPR_PRIME) {
            if (mySettings.ALIGN_MULTILINE_PARAMETERS) {
                return myBaseAlignment;
            }
        }
        return null;
    }
}
