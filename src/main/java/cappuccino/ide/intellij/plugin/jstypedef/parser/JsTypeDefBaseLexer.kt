package cappuccino.ide.intellij.plugin.jstypedef.parser

import cappuccino.ide.intellij.plugin.jstypedef.lexer.JsTypeDefLexer
import com.intellij.psi.tree.IElementType

abstract class JsTypeDefBaseLexer : JsTypeDefLexer() {

    private var lastToken: IElementType? = null

    /**
     * Return the next token from the character stream and records this last
     * token in case it resides on the default channel. This recorded token
     * is used to determine when the lexer could possibly match a regex
     * literal. Also changes scopeStrictModes stack if tokenize special
     * string 'use strict';
     *
     * @return the next token from the character stream.
     */
    override fun advance() {
        super.advance()
        lastToken = tokenType
    }
}
