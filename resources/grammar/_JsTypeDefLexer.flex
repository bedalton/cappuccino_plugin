package cappuccino.ide.intellij.plugin.jstypedef.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static cappuccino.ide.intellij.plugin.jstypedef.psi.types.JsTypeDefTypes.*;
import static cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*;

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
ID=[$_a-zA-Z][_a-zA-Z0-9]*

%state DOUBLE_QUOTE_STRING SINGLE_QUOTE_STRING BLOCK_COMMENT

%%


<SINGLE_QUOTE_STRING> {
	"'" 								 { yybegin(YYINITIAL); return JS_SINGLE_QUOTE_STRING; }
	{LINE_TERMINATOR}					 { return JS_INCOMPLETE_STRING; }
	{SINGLE_QUOTE_TEXT}				 	 {  }
}

<DOUBLE_QUOTE_STRING> {
	"\"" 								 { yybegin(YYINITIAL); return JS_DOUBLE_QUOTE_STRING; }
	{LINE_TERMINATOR}					 { return JS_INCOMPLETE_STRING; }
	{DOUBLE_QUOTE_TEXT}				 	 {  }
}

<BLOCK_COMMENT> {
	"*/"								 { yybegin(YYINITIAL); return ObjJ_BLOCK_COMMENT_END; }
  	"*"									 { return ObjJ_BLOCK_COMMENT_LEADING_ASTERISK; }
 	'.*'/('\n'|'\*\/')							 { return ObjJ_BLOCK_COMMENT_LINE; }
}

<YYINITIAL> {
	\'						 	{ yybegin(SINGLE_QUOTE_STRING); }
	\"							{ yybegin(DOUBLE_QUOTE_STRING); }
  	"="                        	{ return JS_EG; }
  	":"     	              	{ return JS_COLON; }
  	";"                        	{ return JS_SEMI_COLON; }
  	"|"                        	{ return JS_PIPE; }
	"<"                        	{ return JS_OPEN_ARROW; }
  	">"                        	{ return JS_CLOSE_ARROW; }
  	"?"                        	{ return JS_NULLABLE; }
  	","                        	{ return JS_COMMA; }
  	"{"                        	{ return JS_OPEN_BRACE; }
  	"}"                        	{ return JS_CLOSE_BRACE; }
  	"["                        	{ return JS_OPEN_BRACKET; }
  	"]"                        	{ return JS_CLOSE_BRACKET; }
  	"("                        	{ return JS_OPEN_PAREN; }
  	")"                        	{ return JS_CLOSE_PAREN; }
  	"."                        	{ return JS_DOT; }
 	"..."						{ return JS_ELLIPSIS; }
  	"&"							{ return JS_UNION; }
  	"readonly"                 	{ return JS_READONLY; }
  	"var"                      	{ return JS_VAR; }
  	"Array"                    	{ return JS_ARRAY; }
  	"interface"                	{ return JS_INTERFACE; }
  	"class"					 	{ return JS_CLASS_KEYWORD; }
  	"extends"                  	{ return JS_EXTENDS; }
  	"new"		              	{ return JS_CONST; }
  	"function"                 	{ return JS_FUNCTION_KEYWORD; }
  	"void"                     	{ return JS_VOID; }
  	"module"                   	{ return JS_MODULE_KEYWORD; }
  	"Map"                      	{ return JS_MAP; }
  	"typemap"                  	{ return JS_TYPE_MAP_KEYWORD; }
  	"keyof"                    	{ return JS_KEYOF; }
  	"keyset"                    { return JS_KEYS_KEYWORD; }
  	"alias"                    	{ return JS_ALIAS; }
  	"internal"                 	{ return JS_INTERNAL; }
  	"static"                   	{ return JS_STATIC_KEYWORD; }
  	"@quiet"                   	{ return JS_AT_QUIET; }
  	"@silent"                  	{ return JS_AT_SILENT; }
    "@file"                     { return JS_FILE_KEYWORD; }
    "declare"                   { return JS_DECLARE; }

 	{BLOCK_COMMENT}            	{ return JS_BLOCK_COMMENT; }
  	{SINGLE_LINE_COMMENT}      	{ return JS_SINGLE_LINE_COMMENT; }
  	{ESCAPED_ID}               	{ return JS_ESCAPED_ID; }
  	{INTEGER_LITERAL}          	{ return JS_INTEGER_LITERAL; }
  	{ID}                       	{ return JS_ID; }
  	{LINE_TERMINATOR}          	{ return WHITE_SPACE; }
  	{BAD_BLOCK_COMMENT}	    	{ return JS_BLOCK_COMMENT; }
  	{WHITE_SPACE}+            	{ return WHITE_SPACE; }

}

[^] { return BAD_CHARACTER; }
