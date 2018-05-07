package org.cappuccino_project.ide.intellij.plugin.parser;

import grammar.ObjJSJLexer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Token;

import java.util.Stack;

public abstract class ObjJSJBaseLexer extends Lexer {
    /**
     * Stores values of nested modes. By default mode is strict or
     * defined externally (useStrictDefault)
     */
    private Stack<Boolean> scopeStrictModes = new Stack<Boolean>();

    private Token lastToken = null;
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

    public ObjJSJBaseLexer(CharStream input) {
        super(input);
    }

    public boolean getStrictDefault() {
        return useStrictDefault;
    }

    public void setUseStrictDefault(boolean value) {
        useStrictDefault = value;
        useStrictCurrent = value;
    }

    public boolean IsSrictMode() {
        return useStrictCurrent;
    }

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
    public Token nextToken() {
        Token next = super.nextToken();

        if (next.getChannel() == Token.DEFAULT_CHANNEL) {
            // Keep track of the last token on the default channel.
            this.lastToken = next;
        }

        return next;
    }

    protected void ProcessOpenBrace()
    {
        useStrictCurrent = scopeStrictModes.size() > 0 && scopeStrictModes.peek() ? true : useStrictDefault;
        scopeStrictModes.push(useStrictCurrent);
    }

    protected void ProcessCloseBrace()
    {
        useStrictCurrent = scopeStrictModes.size() > 0 ? scopeStrictModes.pop() : useStrictDefault;
    }

    protected void ProcessStringLiteral()
    {
        if (lastToken == null || lastToken.getType() == ObjJSJLexer.OpenBrace)
        {
            String text = getText();
            if (text.equals("\"use strict\"") || text.equals("'use strict'"))
            {
                if (scopeStrictModes.size() > 0)
                    scopeStrictModes.pop();
                useStrictCurrent = true;
                scopeStrictModes.push(useStrictCurrent);
            }
        }
    }

    /**
     * Returns {@code true} if the lexer can match a regex literal.
     */
    protected boolean IsRegexPossible() {

        if (this.lastToken == null) {
            // No token has been produced yet: at the start of the input,
            // no division is possible, so a regex literal _is_ possible.
            return true;
        }

        switch (this.lastToken.getType()) {
            case ObjJSJLexer.Identifier:
            case ObjJSJLexer.NullLiteral:
            case ObjJSJLexer.BooleanLiteral:
            case ObjJSJLexer.This:
            case ObjJSJLexer.CloseBracket:
            case ObjJSJLexer.CloseParen:
            case ObjJSJLexer.OctalIntegerLiteral:
            case ObjJSJLexer.DecimalLiteral:
            case ObjJSJLexer.HexIntegerLiteral:
            case ObjJSJLexer.StringLiteral:
            case ObjJSJLexer.PlusPlus:
            case ObjJSJLexer.MinusMinus:
                // After any of the tokens above, no regex literal can follow.
                return false;
            default:
                // In all other cases, a regex literal _is_ possible.
                return true;
        }
    }
}
