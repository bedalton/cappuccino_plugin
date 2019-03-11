package cappuccino.ide.intellij.plugin.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*;
import java.util.logging.Level;
import java.util.logging.Logger;

%%

%{
	private boolean canRegex;
	StringBuffer pragmaString;
	private static final Logger LOGGER = Logger.getLogger("_ObjJLexer.flex");
	private boolean inPreProc = false;
	private boolean canWhitespace = true;

  	public _ObjJLexer() {
    	this((java.io.Reader)null);
  	}

	protected void canRegex(final boolean canRegex) {
		this.canRegex = canRegex;
	}

	protected boolean canRegex() {
		return canRegex;
	}

	protected boolean canWhiteSpace() {
  	    return canWhitespace;
	}

	protected void canWhiteSpace(boolean canWhitespace) {
  	    this.canWhitespace = canWhitespace;
	}

	private static void log(String message) {
		LOGGER.log(Level.INFO, message);
	}

%}

%public
%class _ObjJLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

PREPROCESSOR_CONTINUE_ON_NEXT_LINE=\\[ ]*\r?\n
LINE_TERMINATOR=[\r\n\u2028\u2029]
VAR_TYPE_BYTE=((unsigned|signed)[ ]+)?byte
VAR_TYPE_CHAR=((unsigned|signed)[ ]+)?char
VAR_TYPE_SHORT=((unsigned|signed)[ ]+)?short
VAR_TYPE_INT=((unsigned|signed)[ ]+)?int
VAR_TYPE_LONG_LONG=((unsigned|signed)[ ]+)?long[ ]+long
VAR_TYPE_LONG=((unsigned|signed)[ ]+)?long
IMPORT_FRAMEWORK_LITERAL=<.*"/".*>
//BAD_SINGLE_QUOTE_STRING_LITERAL=\' SINGLE_QUOTE_STRING
SINGLE_QUOTE_TEXT = ([^\\\'\r\n]|\\ [\\\'brfntvuU0])+
//SINGLE_QUOTE_STRING_LITERAL = {BAD_SINGLE_QUOTE_STRING_LITERAL}\'
//BAD_DOUBLE_QUOTE_STRING_LITERAL=(@\"|\")
DOUBLE_QUOTE_TEXT = ([^\\\"\r\n]|\\ [\\\"brfntvuU0])+
//DOUBLE_QUOTE_STRING_LITERAL = {BAD_DOUBLE_QUOTE_STRING_LITERAL}\"
BOOLEAN_LITERAL=(true|false|YES|yes|NO|no)
HEX_INTEGER_LITERAL=0 [xX] [0-9a-fA-F]+
OCTAL_INTEGER_LITERAL=0 [0-7]+
OCTAL_INTEGER_LITERAL2=0 [oO] [0-7]+
BINARY_INTEGER_LITERAL=0 [bB] [01]+
DECIMAL_LITERAL1=[0-9]*\.([0-9]*([eE] [+-]? [0-9]+)?|[eE] [+-]? [0-9]+)
DECIMAL_LITERAL2=[0-9]+\.?([eE] [+-]? [0-9]+)
INTEGER_LITERAL=[0-9]+
BAD_BLOCK_COMMENT="/"\* {BLOCK_COMMENT_TEXT}
BLOCK_COMMENT_TEXT = ([^*]|\*[^/]|'\n'|'\r'|";"|\s)+
BLOCK_COMMENT = {BAD_BLOCK_COMMENT}\*"/"
SINGLE_LINE_COMMENT="//"[^\r\n\u2028\u2029]*
ID=[$_a-zA-Z][_a-zA-Z0-9]*

%state PREPROCESSOR PRAGMA DOUBLE_QUOTE_STRING SINGLE_QUOTE_STRING BLOCK_COMMENT
%%

<YYINITIAL,PREPROCESSOR> {

	{BAD_BLOCK_COMMENT}			 		 { canRegex(false); return ObjJ_BLOCK_COMMENT; }
	{BAD_DOUBLE_QUOTE_STRING_LITERAL}	 { canRegex(false);	return ObjJ_QUO_TEXT; }
	{BAD_SINGLE_QUOTE_STRING_LITERAL}	 { canRegex(false); return ObjJ_QUO_TEXT; }

}
<YYINITIAL> {
	{WHITE_SPACE}+                       { return WHITE_SPACE; }
}
[^] { return BAD_CHARACTER; }
