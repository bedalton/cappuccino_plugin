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

	protected boolean canWhitespace() {
  	    return canWhitespace;
	}

	protected void canWhitespace(boolean canWhitespace) {
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

PREPROCESSOR_CONTINUE_ON_NEXT_LINE=\\\n
LINE_TERMINATOR=[\r\n\u2028\u2029]
VAR_TYPE_BYTE=((unsigned|signed)[ ]+)?byte
VAR_TYPE_CHAR=((unsigned|signed)[ ]+)?char
VAR_TYPE_SHORT=((unsigned|signed)[ ]+)?short
VAR_TYPE_INT=((unsigned|signed)[ ]+)?int
VAR_TYPE_LONG_LONG=((unsigned|signed)[ ]+)?long[ ]+long
VAR_TYPE_LONG=((unsigned|signed)[ ]+)?long
//BAD_SINGLE_QUOTE_STRING_LITERAL=\' SINGLE_QUOTE_STRING
SINGLE_QUOTE_TEXT = ([^\\\'\r\n]|\\ [\\\'brfntvuU0])+
//SINGLE_QUOTE_STRING_LITERAL = {BAD_SINGLE_QUOTE_STRING_LITERAL}\'
//BAD_DOUBLE_QUOTE_STRING_LITERAL=(@\"|\")
DOUBLE_QUOTE_TEXT = ([^\\\"\r\n]|\\ [\\\"brfntvuU0])+
//DOUBLE_QUOTE_STRING_LITERAL = {BAD_DOUBLE_QUOTE_STRING_LITERAL}\"
BOOLEAN_LITERAL=(true|false|YES|NO)
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
REGEXP_LITERAL_SET = \[ ([^\]]|\\[\[\]]|\/)* \]
REGEXP_ESCAPED_CHAR = \\ [^\r\n\u2028\u2029]
REGEXP_CHAR_SIMPLE = [^\r\n\u2028\u2029\\/\[\]]
REGEXP_VALID_CHAR = {REGEXP_LITERAL_SET} | {REGEXP_ESCAPED_CHAR} | {REGEXP_CHAR_SIMPLE}
REGEXP_END = \/ [a-zA-Z]*
REGEXP = \/ {REGEXP_VALID_CHAR}+ {REGEXP_END}
ID=[$_a-zA-Z][_a-zA-Z0-9]*
PP_UNDEF = #\s*undef
PP_IF_DEF = #\s*ifdef
PP_IFNDEF = #\s*ifndef
PP_IF = #\s*if
PP_ELSE = #\s*else
PP_ENDIF = #\s*endif
PP_ELIF = #\s*elif
PP_PRAGMA = #\s*pragma
PP_DEFINE = #\s*define
PP_DEFINED = #\s*defined
PP_ERROR = #\s*error
PP_WARNING = #\s*warning
PP_INCLUDE = #\s*include
PP_FRAGMENT = #\s+{ID}
WHITE_SPACE=\p{Blank}+
FILE_NAME_LITERAL = [^.>\n]+([.][j]?)?
FRAMEWORK_NAME = [a-zA-Z0-9_-]+
%state PREPROCESSOR PRAGMA DOUBLE_QUOTE_STRING SINGLE_QUOTE_STRING BLOCK_COMMENT IN_REGEXP IN_IMPORT IN_FILENAME
%%


<IN_FILENAME, IN_IMPORT> {
    {LINE_TERMINATOR}					{
          canRegex(true);   yybegin(YYINITIAL);
          IElementType type = inPreProc ? ObjJ_LINE_TERMINATOR : WHITE_SPACE;
          inPreProc = false;
          pragmaString = null;
          return type; }
    "<"                                 { canRegex(false); canWhitespace(true);  return ObjJ_LESS_THAN; }
    ">"                                 { canRegex(false); canWhitespace(true);  return ObjJ_GREATER_THAN; }
	"'"									{ canRegex(false); canWhitespace(true);  yybegin(SINGLE_QUOTE_STRING); return ObjJ_SINGLE_QUO; }
	"\""						        { canRegex(false); canWhitespace(true);  yybegin(DOUBLE_QUOTE_STRING); return ObjJ_DOUBLE_QUO; }
	"/"						        	{ canRegex(false); canWhitespace(true);  yybegin(IN_FILENAME); return ObjJ_DIVIDE; }
    {FRAMEWORK_NAME}                    { canRegex(false); canWhitespace(true);  return ObjJ_FRAMEWORK_NAME; }
}

<IN_FILENAME> {
	{FILE_NAME_LITERAL}					{ canRegex(false); canWhitespace(true); yybegin(IN_IMPORT); return ObjJ_FILE_NAME_LITERAL; }
}

<PRAGMA> {
	'mark'								{ canWhitespace(true); if (pragmaString == null) pragmaString = new StringBuffer(); return ObjJ_MARK; }
	[^\r\n\u2028\u2029]+				{ canWhitespace(true); if (pragmaString != null) pragmaString.append(yytext());}
	{LINE_TERMINATOR}					{ canWhitespace(true); yybegin(YYINITIAL); return ObjJ_PRAGMA_MARKER; }
}

<PREPROCESSOR> {
	{LINE_TERMINATOR} 				   	 { canWhitespace(true); yybegin(YYINITIAL); inPreProc = false; pragmaString = null; return ObjJ_LINE_TERMINATOR; }
	'#'									 { canWhitespace(true); return WHITE_SPACE; }
 	{WHITE_SPACE}  						 { canWhitespace(true); return WHITE_SPACE; }
	{PREPROCESSOR_CONTINUE_ON_NEXT_LINE} { canWhitespace(true); return WHITE_SPACE; }
}

<SINGLE_QUOTE_STRING> {
	"'" 								 { canWhitespace(true); yybegin(inPreProc ? PREPROCESSOR : pragmaString != null ? PRAGMA : YYINITIAL); return ObjJ_SINGLE_QUO; }
	{LINE_TERMINATOR}					 { canWhitespace(true); yybegin(inPreProc ? PREPROCESSOR : pragmaString != null ? PRAGMA : YYINITIAL);  return ObjJ_LINE_TERMINATOR; }
	{SINGLE_QUOTE_TEXT}				 	 { canWhitespace(true); return ObjJ_QUO_TEXT; }
}

<DOUBLE_QUOTE_STRING> {
	"\"" 								 { canWhitespace(true); yybegin(inPreProc ? PREPROCESSOR : pragmaString != null ? PRAGMA : YYINITIAL); return ObjJ_DOUBLE_QUO; }
	{LINE_TERMINATOR}					 { canWhitespace(true); yybegin(inPreProc ? PREPROCESSOR : pragmaString != null ? PRAGMA : YYINITIAL); return ObjJ_LINE_TERMINATOR; }
	{DOUBLE_QUOTE_TEXT}				 	 { return ObjJ_QUO_TEXT; }
}

<BLOCK_COMMENT> {
	"*/"								 { canWhitespace(true); yybegin(YYINITIAL); canRegex(true); /*log("Ending Comment");*/ return ObjJ_BLOCK_COMMENT_END; }
  	"*"									 { canWhitespace(true); return ObjJ_BLOCK_COMMENT_LEADING_ASTERISK; }
 	'.*'/'\n'							 { canWhitespace(true); return ObjJ_BLOCK_COMMENT_LINE; }
}

<IN_REGEXP> {
  	{REGEXP_VALID_CHAR}					 { canWhitespace(true);}
  	{REGEXP_ESCAPED_CHAR}				 { canWhitespace(true);}
  	{REGEXP_END}						 { yybegin(YYINITIAL); canWhitespace(true); return ObjJ_REGULAR_EXPRESSION_LITERAL_TOKEN; }
	{LINE_TERMINATOR}					 { yybegin(YYINITIAL); canWhitespace(true); return BAD_CHARACTER;}
	<<EOF>>								 { yybegin(YYINITIAL); canWhitespace(true); return BAD_CHARACTER; }
}

<YYINITIAL,PREPROCESSOR> {
	"?*__ERR_SEMICOLON__*?"			 	 { return ObjJ_ERROR_SEQUENCE_TOKEN; }
	"'"									 { canRegex(false); canWhitespace(true);  yybegin(SINGLE_QUOTE_STRING); return ObjJ_SINGLE_QUO; }
	"\""						 		 { canRegex(false); canWhitespace(true);  yybegin(DOUBLE_QUOTE_STRING); return ObjJ_DOUBLE_QUO; }
	"/*"								 { canRegex(false); canWhitespace(true);  /*log("Starting Comment");*/ yybegin(BLOCK_COMMENT); return ObjJ_BLOCK_COMMENT_START; }
	"@["                                 { canRegex(true); canWhitespace(true); return ObjJ_AT_OPENBRACKET; }
	"["                                  { canRegex(true); canWhitespace(true); return ObjJ_OPEN_BRACKET; }
	"]"                                  { canRegex(false); canWhitespace(true); return ObjJ_CLOSE_BRACKET; }
	"("                                  { canRegex(true); canWhitespace(true); return ObjJ_OPEN_PAREN; }
	")"                                  { canRegex(false); canWhitespace(true); return ObjJ_CLOSE_PAREN; }
	"{"                                  { canRegex(true); canWhitespace(true); return ObjJ_OPEN_BRACE; }
	"}"                                  { canRegex(false); canWhitespace(true); return ObjJ_CLOSE_BRACE; }
	","                                  { canRegex(true); canWhitespace(true); return ObjJ_COMMA; }
	"="                                  { canRegex(true); canWhitespace(true); return ObjJ_ASSIGN; }
	"?"                                  { canRegex(true); canWhitespace(true); return ObjJ_QUESTION_MARK; }
	":"                                  { canRegex(true); canWhitespace(true); return ObjJ_COLON; }
	"..."                                { canRegex(false); canWhitespace(true); return ObjJ_ELLIPSIS; }
	"."                                  { canRegex(false); canWhitespace(true); return ObjJ_DOT; }
	"++"                                 { canRegex(false); canWhitespace(true); return ObjJ_PLUS_PLUS; }
	"--"                                 { canRegex(false); canWhitespace(true); return ObjJ_MINUS_MINUS; }
	"+"                                  { canRegex(true); canWhitespace(true); return ObjJ_PLUS; }
	"-"                                  { canRegex(true); canWhitespace(true); return ObjJ_MINUS; }
	"~"                                  { canRegex(true); canWhitespace(true); return ObjJ_BIT_NOT; }
	"!"                                  { canRegex(true); canWhitespace(true); 	return ObjJ_NOT; }
	"*"                                  { canRegex(true); canWhitespace(true); return ObjJ_MULTIPLY; }
	"/"                                  { canRegex(true); canWhitespace(true); return ObjJ_DIVIDE; }
	"%"                                  { canRegex(true); canWhitespace(true); return ObjJ_MODULUS; }
	">>"                                 { canRegex(true); canWhitespace(true); return ObjJ_RIGHT_SHIFT_ARITHMATIC; }
	"<<"                                 { canRegex(true); canWhitespace(true); return ObjJ_LEFT_SHIFT_ARITHMATIC; }
	">>>"                                { canRegex(true); canWhitespace(true); return ObjJ_RIGHT_SHIFT_LOGICAL; }
	"<<<"                                { canRegex(true); canWhitespace(true); return ObjJ_LEFT_SHIFT_LOGICAL; }
	"<"                                  { canRegex(true); canWhitespace(true); return ObjJ_LESS_THAN; }
	">"                                  { canRegex(true); canWhitespace(true); return ObjJ_GREATER_THAN; }
	"<="                                 { canRegex(true); canWhitespace(true); return ObjJ_LESS_THAN_EQUALS; }
	">="                                 { canRegex(true); canWhitespace(true); return ObjJ_GREATER_THAN_EQUALS; }
	"=="                                 { canRegex(true); canWhitespace(true); return ObjJ_EQUALS; }
	"!="                                 { canRegex(true); canWhitespace(true); return ObjJ_NOT_EQUALS; }
	"==="                                { canRegex(true); canWhitespace(true); return ObjJ_IDENTITY_EQUALS; }
	"!=="                                { canRegex(true); canWhitespace(true); return ObjJ_IDENTITY_NOT_EQUALS; }
	"&"                                  { canRegex(true); canWhitespace(true); return ObjJ_BIT_AND; }
	"^"                                  { canRegex(true); canWhitespace(true); return ObjJ_BIT_XOR; }
	"|"                                  { canRegex(true); canWhitespace(true); return ObjJ_BIT_OR; }
	"&&"                                 { canRegex(true); canWhitespace(true); return ObjJ_AND; }
	"||"                                 { canRegex(true); canWhitespace(true); return ObjJ_OR; }
	"*="                                 { canRegex(true); canWhitespace(true); return ObjJ_MULTIPLY_ASSIGN; }
	"/="                                 { canRegex(true); canWhitespace(true); return ObjJ_DIVIDE_ASSIGN; }
	"%="                                 { canRegex(true); canWhitespace(true); return ObjJ_MODULUS_ASSIGN; }
	"+="                                 { canRegex(true); canWhitespace(true); return ObjJ_PLUS_ASSIGN; }
	"-="                                 { canRegex(true); canWhitespace(true); return ObjJ_MINUS_ASSIGN; }
	"<<="                                { canRegex(true); canWhitespace(true); return ObjJ_LEFT_SHIFT_ARITHMATIC_ASSIGN; }
	">>="                                { canRegex(true); canWhitespace(true); return ObjJ_RIGHT_SHIFT_ARITHMATIC_ASSIGN; }
	"<<<="                               { canRegex(true); canWhitespace(true); return ObjJ_LEFT_SHIFT_LOGICAL_ASSIGN; }
	">>>="                               { canRegex(true); canWhitespace(true); return ObjJ_RIGHT_SHIFT_LOGICAL_ASSIGN; }
	"&="                                 { canRegex(true); canWhitespace(true); return ObjJ_BIT_AND_ASSIGN; }
	"^="                                 { canRegex(true); canWhitespace(true); return ObjJ_BIT_XOR_ASSIGN; }
	"|="                                 { canRegex(true); canWhitespace(true); return ObjJ_BIT_OR_ASSIGN; }
	"=>"                                 { canRegex(false); canWhitespace(true); return ObjJ_ARROW; }
	"@import"                            { canRegex(false); canWhitespace(true); yybegin(IN_IMPORT); return ObjJ_AT_IMPORT; }
	"@accessors"                         { canRegex(false); canWhitespace(true); return ObjJ_AT_ACCESSORS; }
	"@end"                               { canRegex(false); canWhitespace(true); return ObjJ_AT_END; }
	"@action"                            { canRegex(false); canWhitespace(true); return ObjJ_AT_ACTION; }
	"@selector"                          { canRegex(false); canWhitespace(true); return ObjJ_AT_SELECTOR; }
	"@class"                             { canRegex(false); canWhitespace(true); return ObjJ_AT_CLASS; }
	"@global"                            { canRegex(false); canWhitespace(true); return ObjJ_AT_GLOBAL; }
	"@ref"                               { canRegex(false); canWhitespace(true); return ObjJ_AT_REF; }
	"@deref"                             { canRegex(false); canWhitespace(true); return ObjJ_AT_DEREF; }
	"@protocol"                          { canRegex(false); canWhitespace(true); return ObjJ_AT_PROTOCOL; }
	"@optional"                          { canRegex(false); canWhitespace(true); return ObjJ_AT_OPTIONAL; }
	"@required"                          { canRegex(false); canWhitespace(true); return ObjJ_AT_REQUIRED; }
	"@interface"                         { canRegex(false); canWhitespace(true); return ObjJ_AT_INTERFACE; }
	"@typedef"                           { canRegex(false); canWhitespace(true); return ObjJ_AT_TYPE_DEF; }
	"@implementation"                    { canRegex(false); canWhitespace(true); return ObjJ_AT_IMPLEMENTATION; }
	"@outlet"                            { canRegex(false); canWhitespace(true); return ObjJ_AT_OUTLET; }
	"@{"                                 { canRegex(false); canWhitespace(true); return ObjJ_AT_OPEN_BRACE; }
	"null"|"NULL"                        { canRegex(false); canWhitespace(true); return ObjJ_NULL_LITERAL; }
	"nil"|"Nil"                          { canRegex(false); canWhitespace(true); return ObjJ_NIL; }
	"undefined"                          { canRegex(false); canWhitespace(true); return ObjJ_UNDEFINED; }
	"@"					 		 	 	 { canRegex(false); canWhitespace(false); return ObjJ_AT; }
	{PP_DEFINE}                          { canRegex(false); canWhitespace(true); yybegin(PREPROCESSOR); inPreProc = true; return ObjJ_PP_DEFINE; }
	{PP_UNDEF}                           { canRegex(false); canWhitespace(true); yybegin(PREPROCESSOR); inPreProc = true; return ObjJ_PP_UNDEF; }
	{PP_IF_DEF}                          { canRegex(false); canWhitespace(true); yybegin(PREPROCESSOR); inPreProc = true;return ObjJ_PP_IF_DEF; }
	{PP_IFNDEF}                          { canRegex(false); canWhitespace(true); yybegin(PREPROCESSOR); inPreProc = true;return ObjJ_PP_IF_NDEF; }
	{PP_IF}                              { canRegex(true); canWhitespace(true); yybegin(PREPROCESSOR); inPreProc = true;return ObjJ_PP_IF; }
	{PP_ELSE}                            { canRegex(true); canWhitespace(true); yybegin(PREPROCESSOR); inPreProc = true;return ObjJ_PP_ELSE; }
	{PP_ENDIF}                           { canRegex(true); canWhitespace(true); yybegin(PREPROCESSOR); inPreProc = true;return ObjJ_PP_END_IF; }
	{PP_ELIF}                            { canRegex(false); canWhitespace(true); yybegin(PREPROCESSOR); inPreProc = true; return ObjJ_PP_ELSE_IF; }
	{PP_PRAGMA}                          { canRegex(false); canWhitespace(true); yybegin(PRAGMA); return ObjJ_PP_PRAGMA; }
	{PP_DEFINED}                         { canRegex(false); canWhitespace(true); return ObjJ_PP_DEFINED; }
	{PP_ERROR}                           { canRegex(false); canWhitespace(true); yybegin(PREPROCESSOR); inPreProc = true; return ObjJ_PP_ERROR; }
	{PP_WARNING}                         { canRegex(false); canWhitespace(true); yybegin(PREPROCESSOR); inPreProc = true; return ObjJ_PP_WARNING; }
	{PP_INCLUDE}                         { canRegex(false); canWhitespace(true);	yybegin(IN_IMPORT); inPreProc = true;return ObjJ_PP_INCLUDE; }
	"signed"                             { canRegex(false); canWhitespace(true);  return ObjJ_VAR_TYPE_SIGNED; }
	"unsigned"                           { canRegex(false); canWhitespace(true);  return ObjJ_VAR_TYPE_UNSIGNED; }
	"IBAction"                           { canRegex(false); canWhitespace(true); return ObjJ_VAR_TYPE_IBACTION; }
	"IBOutlet"                           { canRegex(false); canWhitespace(true); return ObjJ_VAR_TYPE_IBOUTLET; }
	"SEL"                                { canRegex(false); canWhitespace(true); return ObjJ_VAR_TYPE_SEL; }
	"float"                              { canRegex(false); canWhitespace(true);  return ObjJ_VAR_TYPE_FLOAT; }
	"double"                             { canRegex(false); canWhitespace(true);  return ObjJ_VAR_TYPE_DOUBLE; }
	"BOOL"                               { canRegex(false); canWhitespace(true);  return ObjJ_VAR_TYPE_BOOL; }
	"break"                              { canRegex(false); canWhitespace(true);  return ObjJ_BREAK; }
	"do"                                 { canRegex(false); canWhitespace(true);  return ObjJ_DO; }
	"instanceof"                         { canRegex(true); canWhitespace(true); return ObjJ_INSTANCE_OF; }
	"typeof"                             { canRegex(true); canWhitespace(true); return ObjJ_TYPE_OF; }
	"case"                               { canRegex(false); canWhitespace(true); return ObjJ_CASE; }
	"else"                               { canRegex(false); canWhitespace(true); return ObjJ_ELSE; }
	"new"                                { canRegex(false); canWhitespace(true); return ObjJ_NEW; }
	"var"                                { canRegex(false); canWhitespace(true); return ObjJ_VAR; }
	"catch"                              { canRegex(false); canWhitespace(true); return ObjJ_CATCH; }
	"finally"                            { canRegex(false); canWhitespace(true); return ObjJ_FINALLY; }
	"return"                             { canRegex(true); canWhitespace(true); return ObjJ_RETURN; }
	"void"                               { canRegex(false); canWhitespace(true); return ObjJ_VOID; }
	"continue"                           { canRegex(true); canWhitespace(true); return ObjJ_CONTINUE; }
	"for"                                { canRegex(false); canWhitespace(true); return ObjJ_FOR; }
	"switch"                             { canRegex(false); canWhitespace(true); return ObjJ_SWITCH; }
	"while"                              { canRegex(false); canWhitespace(true); return ObjJ_WHILE; }
	"debugger"                           { canRegex(true); canWhitespace(true); return ObjJ_DEBUGGER; }
	"function"                           { canRegex(false); canWhitespace(true); return ObjJ_FUNCTION; }
	"this"                               { canRegex(false); canWhitespace(true); return ObjJ_THIS; }
	"with"                               { canRegex(true); canWhitespace(true); return ObjJ_WITH; }
	"default"                            { canRegex(true); canWhitespace(true); return ObjJ_DEFAULT; }
	"if"                                 { canRegex(false); canWhitespace(true); return ObjJ_IF; }
	"throw"                              { canRegex(false); canWhitespace(true); return ObjJ_THROW; }
	"delete"                             { canRegex(false); canWhitespace(true); return ObjJ_DELETE; }
	"in"                                 { canRegex(true); canWhitespace(true); return ObjJ_IN; }
	"try"                                { canRegex(false); canWhitespace(true); return ObjJ_TRY; }
	"let"                                { canRegex(false); canWhitespace(true); return ObjJ_LET; }
	"const"                              { canRegex(false); canWhitespace(true); return ObjJ_CONST; }
	";"                                  { canRegex(true); canWhitespace(true); return ObjJ_SEMI_COLON; }
	{BLOCK_COMMENT}                      { canRegex(true); canWhitespace(true); return ObjJ_BLOCK_COMMENT; }
	{PREPROCESSOR_CONTINUE_ON_NEXT_LINE} { canRegex(true); canWhitespace(true); return ObjJ_PREPROCESSOR_CONTINUE_ON_NEXT_LINE; }
	{LINE_TERMINATOR}                    { canRegex(true); canWhitespace(true); return WHITE_SPACE; }
	{VAR_TYPE_BYTE}                      { canRegex(false); canWhitespace(true);  return ObjJ_VAR_TYPE_BYTE; }
	{VAR_TYPE_CHAR}                      { canRegex(false); canWhitespace(true);  return ObjJ_VAR_TYPE_CHAR; }
	{VAR_TYPE_SHORT}                     { canRegex(false); canWhitespace(true);  return ObjJ_VAR_TYPE_SHORT; }
	{VAR_TYPE_INT}                       { canRegex(false); canWhitespace(true);  return ObjJ_VAR_TYPE_INT; }
	{VAR_TYPE_LONG_LONG}                 { canRegex(false); canWhitespace(true);  return ObjJ_VAR_TYPE_LONG_LONG; }
	{VAR_TYPE_LONG}                      { canRegex(false); canWhitespace(true);  return ObjJ_VAR_TYPE_LONG; }
	//{SINGLE_QUOTE_STRING_LITERAL}        { canRegex(false); canWhitespace(true);  return ObjJ_SINGLE_QUOTE_STRING_LITERAL; }
	//{DOUBLE_QUOTE_STRING_LITERAL}        { canRegex(false); canWhitespace(true);  return ObjJ_DOUBLE_QUOTE_STRING_LITERAL; }
	{BOOLEAN_LITERAL}                    { canRegex(false); canWhitespace(true);  return ObjJ_BOOLEAN_LITERAL; }
	{HEX_INTEGER_LITERAL}                { canRegex(false); canWhitespace(true);  return ObjJ_HEX_INTEGER_LITERAL; }
	{OCTAL_INTEGER_LITERAL}              { canRegex(false); canWhitespace(true);  return ObjJ_OCTAL_INTEGER_LITERAL; }
	{OCTAL_INTEGER_LITERAL2}             { canRegex(false); canWhitespace(true);  return ObjJ_OCTAL_INTEGER_LITERAL2; }
	{BINARY_INTEGER_LITERAL}             { canRegex(false); canWhitespace(true);  return ObjJ_BINARY_INTEGER_LITERAL; }
	{DECIMAL_LITERAL1} | {DECIMAL_LITERAL2} { canRegex(false); canWhitespace(true);  return ObjJ_DECIMAL_LITERAL; }
	{INTEGER_LITERAL}                    { canRegex(false); canWhitespace(true);  return ObjJ_INTEGER_LITERAL; }
	{ID}                                 { canRegex(false); canWhitespace(true); return ObjJ_ID; }
	"/"{REGEXP_VALID_CHAR}
        								 {
											canWhitespace(true);
											if (canRegex()) {
												canRegex(false);
												yybegin(IN_REGEXP);
										 	} else if (yytext().toString().substring(1,2).equals("=")){
										 		yypushback(yytext().length()-2);
										 		return ObjJ_DIVIDE_ASSIGN;
											} else {
												yypushback(yytext().length()-1);
										 		return ObjJ_DIVIDE;
										 	}
										 }
	{BAD_BLOCK_COMMENT}			 		 { canRegex(false); canWhitespace(true); return ObjJ_BLOCK_COMMENT; }
	{SINGLE_LINE_COMMENT}                { canRegex(true); canWhitespace(true); return ObjJ_SINGLE_LINE_COMMENT; }
	//{BAD_DOUBLE_QUOTE_STRING_LITERAL}	 { canRegex(false);	return ObjJ_QUO_TEXT; }
	//{BAD_SINGLE_QUOTE_STRING_LITERAL}	 { canRegex(false); return ObjJ_QUO_TEXT; }

}
{PP_FRAGMENT}				             { canRegex(false); canWhitespace(true); return ObjJ_PP_FRAGMENT; }
"@"{ID}						 			 { canRegex(false); canWhitespace(true); return ObjJ_AT_FRAGMENT; }
<YYINITIAL, IN_FILENAME, IN_IMPORT> {
	{WHITE_SPACE}+                       { return canWhitespace() ? WHITE_SPACE : BAD_CHARACTER; }
  	"\\"								 { return ObjJ_FORWARD_SLASH; }
}
[^] { return BAD_CHARACTER; }
