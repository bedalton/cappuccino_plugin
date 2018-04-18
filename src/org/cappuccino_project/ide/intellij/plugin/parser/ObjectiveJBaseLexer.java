package org.cappuccino_project.ide.intellij.plugin.parser;

import com.intellij.psi.tree.IElementType;
import org.cappuccino_project.ide.intellij.plugin.lexer.ObjJLexer;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;

import java.util.Stack;

public abstract class ObjectiveJBaseLexer extends ObjJLexer {
    /**
     * Stores values of nested modes. By default mode is strict or
     * defined externally (useStrictDefault)
     */
    private Stack<Boolean> scopeStrictModes = new Stack<Boolean>();

    private IElementType lastToken;

    private boolean isSignedVar = false;
    /**
     * Default value of strict mode
     * Can be defined externally by setUseStrictDefault
     */
    private boolean useStrictDefault = false;
    /**
     * Current value of strict mode
     * Can be defined during parsing, see StringFunctions.js and StringGlobal.js samples
     */
    private boolean useStrictCurrent = false;

    /**
     * Return the next token from the character stream and records this last
     * token in case it resides on the default channel. This recorded token
     * is used to determine when the lexer could possibly match a regex
     * literal. Also changes scopeStrictModes stack if tokenize special
     * string 'use strict';
     *
     * @return the next token from the character stream.
     */
    @Override
    public void advance() {
        super.advance();
        lastToken = getTokenType();
    }

    /**
     * Returns {@code true} if the lexer can match a regex literal.
     */
    protected boolean RegexPossible() {

        if (this.lastToken == null) {
            // No token has been produced yet: at the start of the input,
            // no division is possible, so a regex literal _is_ possible.
            return true;
        }
        if (lastToken.equals(ObjJTypes.ObjJ_ID) ||
                lastToken.equals(ObjJTypes.ObjJ_NULL_LITERAL) ||
                lastToken.equals(ObjJTypes.ObjJ_BOOLEAN_LITERAL) ||
                lastToken.equals(ObjJTypes.ObjJ_THIS) ||
                lastToken.equals(ObjJTypes.ObjJ_CLOSE_BRACKET) ||
                lastToken.equals(ObjJTypes.ObjJ_CLOSE_PAREN) ||
                lastToken.equals(ObjJTypes.ObjJ_OCTAL_INTEGER_LITERAL) ||
                lastToken.equals(ObjJTypes.ObjJ_DECIMAL_LITERAL) ||
                lastToken.equals(ObjJTypes.ObjJ_HEX_INTEGER_LITERAL) ||
                lastToken.equals(ObjJTypes.ObjJ_SINGLE_QUOTE_STRING_LITERAL) ||
                lastToken.equals(ObjJTypes.ObjJ_DOUBLE_QUOTE_STRING_LITERAL) ||
                lastToken.equals(ObjJTypes.ObjJ_PLUS_PLUS) ||
                lastToken.equals(ObjJTypes.ObjJ_MINUS_MINUS)) {
            // After any of the tokens above, no regex literal can follow.
            return false;
        }
        // In all other cases, a regex literal _is_ possible.
        return true;
    }
}
