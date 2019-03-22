package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

object ObjJSharedParserFunctions {
    fun eos(compositeElement: PsiElement?): Boolean {
        if (compositeElement == null) {
            return false
        }
        var ahead = compositeElement.getNextNode()
        if (ahead == null && compositeElement.parent != null) {
            return eos(compositeElement.parent)
        }
        var hadLineTerminator = false
        while (ahead != null && (ahead.elementType === com.intellij.psi.TokenType.WHITE_SPACE || ahead.elementType === ObjJTypes.ObjJ_LINE_TERMINATOR)) {
            if (ahead === ObjJTypes.ObjJ_LINE_TERMINATOR) {
                hadLineTerminator = true
            }
            while (ahead!!.treeNext == null && ahead.treeParent != null) {
                ahead = ahead.treeParent
            }
            ahead = ahead.treeNext
        }
        return ahead != null && eosToken(ahead.elementType, hadLineTerminator)
    }

    fun eosToken(ahead: IElementType?, hadLineTerminator: Boolean): Boolean {

        if (ahead == null) {
            //LOGGER.log(Level.INFO, "EOS assumed as ahead == null")
            return true
        }

        // Check if the token is, or contains a line terminator.
        //LOGGER.log(Level.INFO, String.format("LineTerminatorAheadToken: <%s>; CurrentToken <%s> Is Line Terminator?:  <%b>", ahead, builder_.getTokenText(), isLineTerminator));
        var isLineTerminator = ahead === ObjJTypes.ObjJ_BLOCK_COMMENT ||
                ahead === ObjJTypes.ObjJ_SINGLE_LINE_COMMENT ||
                ahead === ObjJTypes.ObjJ_ELSE ||
                ahead === ObjJTypes.ObjJ_IF ||
                ahead === ObjJTypes.ObjJ_CLOSE_BRACE ||
                ahead === ObjJTypes.ObjJ_WHILE ||
                ahead === ObjJTypes.ObjJ_DO ||
                ahead === ObjJTypes.ObjJ_PP_PRAGMA ||
                ahead === ObjJTypes.ObjJ_PP_IF ||
                ahead === ObjJTypes.ObjJ_PP_ELSE ||
                ahead === ObjJTypes.ObjJ_PP_ELSE_IF ||
                ahead === ObjJTypes.ObjJ_PP_END_IF ||
                ahead === ObjJTypes.ObjJ_SEMI_COLON
        if (isLineTerminator || !ObjJPluginSettings.inferEOS()) {
            if (!isLineTerminator) {
                //LOGGER.log(Level.INFO, "Failed EOS check. Ahead token is <"+ahead.toString()+">");
            }
            return isLineTerminator
        }
        isLineTerminator = hadLineTerminator && (ahead === ObjJTypes.ObjJ_BREAK ||
                ahead === ObjJTypes.ObjJ_VAR ||
                ahead === ObjJTypes.ObjJ_AT_IMPLEMENTATION ||
                ahead === ObjJTypes.ObjJ_AT_IMPORT ||
                ahead === ObjJTypes.ObjJ_AT_GLOBAL ||
                ahead === ObjJTypes.ObjJ_TYPE_DEF ||
                ahead === ObjJTypes.ObjJ_FUNCTION ||
                ahead === ObjJTypes.ObjJ_AT_PROTOCOL ||
                ahead === ObjJTypes.ObjJ_CONTINUE ||
                ahead === ObjJTypes.ObjJ_CONST ||
                ahead === ObjJTypes.ObjJ_RETURN ||
                ahead === ObjJTypes.ObjJ_SWITCH ||
                ahead === ObjJTypes.ObjJ_LET ||
                ahead === ObjJTypes.ObjJ_CASE)
        return isLineTerminator
    }

    @Suppress("unused")
    fun PsiElement?.hasNodeType(elementType: IElementType): Boolean {
        return this != null && this.node.elementType === elementType
    }
}