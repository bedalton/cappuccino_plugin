package cappuccino.ide.intellij.plugin.contributor

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes

import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*
import java.util.logging.Level
import java.util.logging.Logger

class ObjJBraceMatcher : PairedBraceMatcher {


    override fun getPairs(): Array<BracePair> {
        return PAIRS
    }

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, nextTokenType: IElementType?): Boolean {
        Logger.getAnonymousLogger().log(Level.INFO, "Matching Brace: "+lbraceType.toString() + " in context: "+nextTokenType.toString())
        return if (lbraceType === ObjJTypes.ObjJ_OPEN_BRACKET) {
            isPairedBracketAllowedBeforeType(nextTokenType)
        } else if (lbraceType == ObjJ_LESS_THAN) {
            nextTokenType == ObjJ_INHERITED_PROTOCOL_LIST
        } else {
            isAllowed(NOT_BEFORE, nextTokenType)
        }
    }

    private fun isPairedBracketAllowedBeforeType(contextType: IElementType?): Boolean {
        return isAllowed(NOT_BEFORE, contextType) && isAllowed(NO_BRACE_BEFORE, contextType)
    }

    private fun isAllowed(notBeforeElements: Array<IElementType>, contextType: IElementType?): Boolean {
        for (notBefore in notBeforeElements) {
            if (contextType === notBefore) {
                return false
            }
        }
        return true
    }

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }

    companion object {
        private val PAIRS = arrayOf(
                BracePair(ObjJ_OPEN_PAREN, ObjJ_CLOSE_PAREN, false),
                BracePair(ObjJ_OPEN_BRACKET, ObjJ_CLOSE_BRACKET, false),
                BracePair(ObjJ_OPEN_BRACE, ObjJ_CLOSE_BRACE, false),
                BracePair(ObjJ_AT_OPENBRACKET, ObjJ_CLOSE_BRACKET, false),
                BracePair(ObjJ_AT_OPEN_BRACE, ObjJ_CLOSE_BRACE, false),
                BracePair(ObjJ_LESS_THAN, ObjJ_GREATER_THAN, true),
                BracePair(ObjJ_DOUBLE_QUO, ObjJ_DOUBLE_QUO, true),
                BracePair(ObjJ_SINGLE_QUO, ObjJ_SINGLE_QUO, true),
                BracePair(ObjJ_BLOCK_COMMENT_START, ObjJ_BLOCK_COMMENT_END, true)
        )

        private val NOT_BEFORE = arrayOf(ObjJ_RETURN, ObjJ_CONTINUE, ObjJ_BREAK, ObjJ_VAR, ObjJ_FOR, ObjJ_WHILE, ObjJ_DO, ObjJ_IF, ObjJ_OPEN_BRACE, ObjJ_OPEN_BRACKET, ObjJ_OPEN_PAREN, ObjJ_AT_OPEN_BRACE, ObjJ_AT_OPENBRACKET, ObjJ_SINGLE_QUO, ObjJ_DOUBLE_QUO)
        private val NO_BRACE_BEFORE = arrayOf(ObjJ_VAR)
    }
}
