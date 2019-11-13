package cappuccino.ide.intellij.plugin.comments;

import cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocTokens;import cappuccino.ide.intellij.plugin.comments.parser.ObjJDocKnownTags;
import com.intellij.psi.tree.IElementType;

import static cappuccino.ide.intellij.plugin.psi.types.ObjJCommentTypes.*;

%%

%{
  public _ObjJCommentLexer() {
    this((java.io.Reader)null);
  }

  public boolean prevCharIsPipe() {
      if (zzMarkedPos < 2)
          return false;
      int zzMarkedPos = zzCurrentPos - 1;
      while(zzMarkedPos != 0 && Character.isWhitespace(zzBuffer.charAt(zzMarkedPos))) {
          zzMarkedPos -= 1;
      }
      return zzBuffer.charAt(zzMarkedPos) == '|';
  }

  private boolean yytextContainLineBreaks() {
    return CharArrayUtil.containLineBreaks(zzBuffer, zzStartRead, zzMarkedPos);
  }

%}

%public
%class _ObjJCommentLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

DIGIT=[0-9]
ALPHA=[:jletter:]
BLOCKSTART = \/\*\*?
WHITE_SPACE_CHAR=[ \t\n\x0B\f\r]+|\*
IDENTIFIER={ALPHA}({ALPHA}|{DIGIT})*
TAG_NAME={IDENTIFIER}
QUALIFIED_NAME={IDENTIFIER}("." {IDENTIFIER})*
LINE_TERMINATOR=[\r\n\u2028\u2029]
TEXT=[^ \n$]+
ID=[_a-zA-Z$][a-zA-Z0-9_]*
%state MARK_CLASSES LINE_BEGINNING TAG_BEGINNING TAG_TEXT_BEGINNING CONTENTS_BEGINNING CONTENTS
%%

<YYINITIAL> {
	"/""*"+ {
		yybegin(CONTENTS_BEGINNING);
		return ObjJCommentTokens.START;
	}
	"*"+ "/" {
          if (isLastToken())
              return KDocTokens.END;
          else
              return KDocTokens.TEXT;
	}
}
<LINE_BEGINNING> "*"+ {
	yybegin(CONTENTS_BEGINNING);
	return KDocTokens.LEADING_ASTERISK;
}

<CONTENTS_BEGINNING> {
	"@"{TAG_NAME} {
				ObjJDocKnownTags tag = ObjJDocKnownTags.Companion.findByTagName(zzBuffer.subSequence(zzStartRead, zzMarkedPos));
				yybegin(tag != null && tag.isReferenceRequired() ? TAG_BEGINNING : TAG_TEXT_BEGINNING);
				return OBjJCommentTokens.TAG_NAME;
			}
}

<TAG_BEGINNING> {
    {WHITE_SPACE_CHAR}+ {
        if (yytextContainLineBreaks()) {
            yybegin(LINE_BEGINNING);
        }
        return TokenType.WHITE_SPACE;
    }

    {QUALIFIED_NAME} {
          yybegin(TAG_TEXT_BEGINNING);
          return ObjJDocTokens.QUALIFIED_NAME;
	}

    . {
        yybegin(CONTENTS);
        return KDocTokens.TEXT;
    }
}

<TAG_TEXT_BEGINNING> {
    {WHITE_SPACE_CHAR}+ {
        if (yytextContainLineBreaks()) {
            yybegin(LINE_BEGINNING);
        }
        return TokenType.WHITE_SPACE;
    }

    "|"|","					{ return ObjJDocTokens.INSTANCE.getTagValueDelim(); }

    /* Example: @return[x] The return value of function x
                       ^^^
    */
    {QUALIFIED_NAME} {
			  if (!prevCharIsPipe())
				  yybegin(CONTENTS);
			  return ObjJDocTokens.INSTANCE.getQualifiedName();
		}

		. {
					yybegin(CONTENTS);
					return ObjJDocTokens.INSTANCE.getText();
				}
}



<YYINITIAL> {
  "@param"                   { yybegin(MARK_CLASSES); return ObjJComment_AT_PARAM; }
  "@return"                  { yybegin(MARK_CLASSES); return ObjJComment_AT_RETURN; }
  "@returns"                 { yybegin(MARK_CLASSES); return ObjJComment_AT_RETURNS; }
  "@ignore"                  { yybegin(MARK_CLASSES); return ObjJComment_AT_IGNORE; }
  "@var"                     { yybegin(MARK_CLASSES); return ObjJComment_AT_VAR; }

  {WHITE_SPACE_CHAR}                       { return ObjJComment_WS; }
  {LINE_TERMINATOR}          { yybegin(LINE_BEGINNING); return ObjJComment_LINE_TERMINATOR; }
  {TEXT}                     { return ObjJComment_TEXT; }
  {ID}                       { return ObjJComment_ID; }

}

[^] { return ObjJComment_TEXT;; }
