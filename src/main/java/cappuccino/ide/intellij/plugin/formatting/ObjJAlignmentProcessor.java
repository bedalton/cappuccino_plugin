package cappuccino.ide.intellij.plugin.formatting;

import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets;
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes;
import cappuccino.ide.intellij.plugin.settings.ObjJCodeStyleSettings;
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
    private final ObjJCodeStyleSettings objjSettings;

    public ObjJAlignmentProcessor(ASTNode node, CommonCodeStyleSettings settings, ObjJCodeStyleSettings objJCodeStyleSettings) {
        myNode = node;
        mySettings = settings;
        myBaseAlignment = Alignment.createAlignment();
        objjSettings = objJCodeStyleSettings;
    }

    @Nullable
    public Alignment createChildAlignment(ASTNode child) {
        IElementType elementType = myNode.getElementType();
        IElementType childType = child.getElementType();

        if (elementType == ObjJ_TERNARY_EXPR_PRIME) {
            if (mySettings.ALIGN_MULTILINE_PARAMETERS) {
                return myBaseAlignment;
            }
        }
        if (objjSettings.ALIGN_SELECTORS_IN_METHOD_DECLARATION || objjSettings.ALIGN_SELECTORS_IN_METHOD_CALL) {
            if (ObjJTokenSets.INSTANCE.getMETHOD_HEADER_DECLARATION_SELECTOR().contains(childType) ||
            childType == ObjJTypes.ObjJ_QUALIFIED_METHOD_CALL_SELECTOR) {
                return Alignment.createAlignment(true);
            }
        }
        return null;
    }
}
