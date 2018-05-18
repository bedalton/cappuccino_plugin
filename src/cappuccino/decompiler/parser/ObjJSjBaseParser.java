package cappuccino.decompiler.parser;

import cappuccino.decompiler.parser.ObjJSjListener.ProgressCallback;
import org.antlr.v4.runtime.*;

import javax.annotation.Nullable;

/**
 * All parser methods that used in grammar (p, prev, notLineTerminator, etc.)
 * should start with lower case char similar to parser rules.
 * https://github.com/antlr/grammars-v4/blob/master/javascript/Java/ObjJSjBaseParser.java
 */
public abstract class ObjJSjBaseParser extends Parser
{
    private int size;
    private ProgressCallback progressCallback;
    private double lastProgress = 0;
    public ObjJSjBaseParser(TokenStream input) {
        super(input);
        size = input.size();
        this.progressCallback = progressCallback;
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public Token match(int ttype) throws RecognitionException {
        if (progressCallback != null) {
            double progress = (double)getCurrentToken().getStopIndex() / (double)size;
            if (progress - lastProgress > 0.0004) {
                progressCallback.onProgress(progress);
                lastProgress = progress;
            }
        }
        return super.match(ttype);
    }

    /**
     * Short form for prev(String str)
     */
    protected boolean p(String str) {
        return prev(str);
    }

    /**
     * Whether the previous token value equals to @param str
     */
    protected boolean prev(String str) {
        return _input.LT(-1).getText().equals(str);
    }

    protected boolean notLineTerminator() {
        return !here(ObjJSjParser.LineTerminator);
    }

    protected boolean notOpenBraceAndNotFunction() {
        int nextTokenType = _input.LT(1).getType();
        return nextTokenType != ObjJSjParser.OpenBrace && nextTokenType != ObjJSjParser.Function;
    }

    protected boolean closeBrace() {
        return _input.LT(1).getType() == ObjJSjParser.CloseBrace;
    }

    /**
     * Returns {@code true} iff on the current index of the parser's
     * token stream a token of the given {@code type} exists on the
     * {@code HIDDEN} channel.
     *
     * @param type
     *         the type of the token on the {@code HIDDEN} channel
     *         to check.
     *
     * @return {@code true} iff on the current index of the parser's
     * token stream a token of the given {@code type} exists on the
     * {@code HIDDEN} channel.
     */
    private boolean here(final int type) {

        // Get the token ahead of the current index.
        int possibleIndexEosToken = this.getCurrentToken().getTokenIndex() - 1;
        Token ahead = _input.get(possibleIndexEosToken);

        // Check if the token resides on the HIDDEN channel and if it's of the
        // provided type.
        return (ahead.getChannel() == Lexer.HIDDEN) && (ahead.getType() == type);
    }

    /**
     * Returns {@code true} iff on the current index of the parser's
     * token stream a token exists on the {@code HIDDEN} channel which
     * either is a line terminator, or is a multi line comment that
     * contains a line terminator.
     *
     * @return {@code true} iff on the current index of the parser's
     * token stream a token exists on the {@code HIDDEN} channel which
     * either is a line terminator, or is a multi line comment that
     * contains a line terminator.
     */
    protected boolean lineTerminatorAhead() {

        // Get the token ahead of the current index.
        int possibleIndexEosToken = this.getCurrentToken().getTokenIndex() - 1;
        Token ahead = _input.get(possibleIndexEosToken);

        if (ahead.getChannel() != Lexer.HIDDEN) {
            // We're only interested in tokens on the HIDDEN channel.
            return false;
        }

        if (ahead.getType() == ObjJSjParser.LineTerminator) {
            // There is definitely a line terminator ahead.
            return true;
        }

        if (ahead.getType() == ObjJSjParser.WhiteSpaces) {
            // Get the token ahead of the current whitespaces.
            possibleIndexEosToken = this.getCurrentToken().getTokenIndex() - 2;
            ahead = _input.get(possibleIndexEosToken);
        }

        // Get the token's text and type.
        String text = ahead.getText();
        int type = ahead.getType();

        // Check if the token is, or contains a line terminator.
        return (type == ObjJSjParser.MultiLineComment && (text.contains("\r") || text.contains("\n"))) ||
                (type == ObjJSjParser.LineTerminator);
    }
}