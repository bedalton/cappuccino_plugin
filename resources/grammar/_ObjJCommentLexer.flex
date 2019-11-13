package cappuccino.ide.intellij.plugin.comments;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static cappuccino.ide.intellij.plugin.psi.types.ObjJCommentTypes.*;

%%

%{
  public _ObjJCommentLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _ObjJCommentLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

WS=[ \t\n\x0B\f\r]+|\*
LINE_TERMINATOR=[\r\n\u2028\u2029]
TEXT=[^ \n$]+
ID=[_a-zA-Z$][a-zA-Z0-9_]*
%state MARK_CLASSES
%%

<MARK_CLASSES> {
	{ID} 					{ return ObjJComment_ID; }
    '|'						{ return ObjJComment_PIPE; }
    ","                     { return ObjJComment_COMMA; }
  	{TEXT}					{ yybegin(YYINITIAL); return ObjJComment_TEXT; }
}

<YYINITIAL> {
  "@param"                   { yybegin(MARK_CLASSES); return ObjJComment_AT_PARAM; }
  "@return"                  { yybegin(MARK_CLASSES); return ObjJComment_AT_RETURN; }
  "@returns"                 { yybegin(MARK_CLASSES); return ObjJComment_AT_RETURNS; }
  "@ignore"                  { yybegin(MARK_CLASSES); return ObjJComment_AT_IGNORE; }
  "@var"                     { yybegin(MARK_CLASSES); return ObjJComment_AT_VAR; }

  {WS}                       { return ObjJComment_WS; }
  {LINE_TERMINATOR}          { return ObjJComment_LINE_TERMINATOR; }
  {TEXT}                     { return ObjJComment_TEXT; }
  {ID}                       { return ObjJComment_ID; }

}

[^] { return ObjJComment_TEXT;; }
