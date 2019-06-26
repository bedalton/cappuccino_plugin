package cappuccino.ide.intellij.plugin.jstypedef.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes.*;

%%

%{
  public _JsTypeDefLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _JsTypeDefLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+
SINGLE_QUOTE_TEXT = ([^\\\'\r\n]|\\ [\\\'brfntvuU0])+
//SINGLE_QUOTE_STRING_LITERAL = {BAD_SINGLE_QUOTE_STRING_LITERAL}\'
//BAD_DOUBLE_QUOTE_STRING_LITERAL=(@\"|\")
DOUBLE_QUOTE_TEXT = ([^\\\"\r\n]|\\ [\\\"brfntvuU0])+
LINE_TERMINATOR=[\r\n\u2028\u2029]
BAD_BLOCK_COMMENT="/"\* {BLOCK_COMMENT_TEXT}
BLOCK_COMMENT_TEXT = ([^*]|\*[^/]|'\n'|'\r'|";"|\s)+
BLOCK_COMMENT = {BAD_BLOCK_COMMENT}\*"/"
SINGLE_LINE_COMMENT="//"[^\r\n\u2028\u2029]*
ESCAPED_ID=`[ \t\n\x0B\f\r]*[_a-zA-Z][_a-zA-Z0-9]*[ \t\n\x0B\f\r]*`

INTEGER_LITERAL=[0-9]+
ID=[_a-zA-Z][_a-zA-Z0-9]*

%state DOUBLE_QUOTE_STRING SINGLE_QUOTE_STRING BLOCK_COMMENT

%%


<SINGLE_QUOTE_STRING> {
	"'" 								 { yybegin(YYINITIAL); return ObjJ_SINGLE_QUO; }
	{LINE_TERMINATOR}					 { return ObjJ_LINE_TERMINATOR; }
	{SINGLE_QUOTE_TEXT}				 	 { return ObjJ_QUO_TEXT; }
}

<DOUBLE_QUOTE_STRING> {
	"\"" 								 { yybegin(YYINITIAL); return ObjJ_DOUBLE_QUO; }
	{LINE_TERMINATOR}					 { return ObjJ_LINE_TERMINATOR; }
	{DOUBLE_QUOTE_TEXT}				 	 { return ObjJ_QUO_TEXT; }
}

<BLOCK_COMMENT> {
	"*/"								 { yybegin(YYINITIAL); canRegex(true); /*log("Ending Comment");*/ return ObjJ_BLOCK_COMMENT_END; }
  	"*"									 { return ObjJ_BLOCK_COMMENT_LEADING_ASTERISK; }
 	'.*'/'\n'							 { return ObjJ_BLOCK_COMMENT_LINE; }
}

<YYINITIAL> {
  "="                        { return JS_EG; }
  ":"                        { return JS_COLON; }
  ";"                        { return JS_SEMI_COLON; }
  "|"                        { return JS_PIPE; }
  "<"                        { return JS_OPEN_ARROW; }
  ">"                        { return JS_CLOSE_ARROW; }
  "?"                        { return JS_NULLABLE; }
  ","                        { return JS_COMMA; }
  "{"                        { return JS_OPEN_BRACE; }
  "}"                        { return JS_CLOSE_BRACE; }
  "["                        { return JS_OPEN_BRACKET; }
  "]"                        { return JS_CLOSE_BRACKET; }
  "("                        { return JS_OPEN_PAREN; }
  ")"                        { return JS_CLOSE_PAREN; }
  "."                        { return JS_DOT; }
  "declare"                  { return JS_DECLARE; }
  "readonly"                 { return JS_READONLY; }
  "var"                      { return JS_VAR; }
  "Array"                    { return JS_ARRAY; }
  "interface"                { return JS_INTERFACE; }
  "extends"                  { return JS_EXTENDS; }
  "const"                    { return JS_CONST; }
  "function"                 { return JS_FUNCTION; }
  "void"                     { return JS_VOID; }
  "null"                     { return JS_NULL; }
  "module"                   { return JS_MODULE; }
  "readonly"                 { JS_READONLY; }
  "var"                      { JS_VAR; }
  "Array"                    { JS_ARRAY; }
  "interface"                { JS_INTERFACE; }
  "extends"                  { JS_EXTENDS; }
  "constructor"              { JS_CONST; }
  "function"                 { JS_FUNCTION; }
  "void"                     { JS_VOID; }
  "null"                     { JS_NULL_TYPE; }
  "module"                   { JS_MODULE; }
  "Map"                      { JS_MAP; }
  "typemap"                  { JS_TYPE_MAP_KEYWORD; }
  "keyof"                    { JS_KEYOF; }
  "keys"                     { JS_KEYS_KEYWORD; }
  "alias"                    { JS_ALIAS; }
  "internal"                 { JS_INTERNAL; }
  "static"                   { JS_STATIC_KEYWORD; }

  {BLOCK_COMMENT}            { return JS_BLOCK_COMMENT; }
  {SINGLE_LINE_COMMENT}      { return JS_SINGLE_LINE_COMMENT; }
  {ESCAPED_ID}               { return JS_ESCAPED_ID; }
  {INTEGER_LITERAL}                    { return JS_INTEGER_LITERAL; }
  {ID}                       { return JS_ID; }

	{LINE_TERMINATOR}                    { return WHITE_SPACE; }

	{BAD_BLOCK_COMMENT}			 		 { return JS_BLOCK_COMMENT; }

  {WHITE_SPACE}+              { return WHITE_SPACE; }

}

[^] { return BAD_CHARACTER; }
