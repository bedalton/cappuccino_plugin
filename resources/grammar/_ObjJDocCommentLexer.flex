package cappuccino.ide.intellij.plugin.comments;

import cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocCommentTokenType;import cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocCommentTypes;import cappuccino.ide.intellij.plugin.comments.parser.ObjJDocCommentKnownTags;
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes;import com.intellij.psi.tree.IElementType;

import static cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocCommentTypes.*;
import static cappuccino.ide.intellij.plugin.psi.types.ObjJCommentTypes.*;

%%

%{
  public _ObjJCommentLexer() {
    this((java.io.Reader)null);
  }
public boolean prevCharIs(char c) {
      if (zzMarkedPos < 2)
          return false;
      int zzMarkedPos = zzCurrentPos - 1;
      while(zzMarkedPos != 0 && Character.isWhitespace(zzBuffer.charAt(zzMarkedPos))) {
          zzMarkedPos -= 1;
      }
      return zzBuffer.charAt(zzMarkedPos) == c;
  }
  public boolean prevCharIsDot() {
      return prevCharIs('.');
  }
  public boolean prevCharIsPipe() {
      return prevCharIs('|');
    }

    public boolean prevCharIsPipeOrDot() {
      return prevCharIsPipe() || prevCharIsDot();
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
BLOCKSTART = \/\*([*]+|[!])?
WHITE_SPACE_CHAR=[ \t\n\x0B\f\r]+|\*
IDENTIFIER={ALPHA}({ALPHA}|{DIGIT})*
TAG_NAME={IDENTIFIER}
%state MARK_CLASSES LINE_BEGINNING TAG_BEGINNING TAG_TEXT_BEGINNING CONTENTS_BEGINNING CONTENTS
%%

<YYINITIAL> {
	{BLOCKSTART} {
		yybegin(CONTENTS_BEGINNING);
		return ObjJComment_START;
	}
	"*"+ "/" {
          if (isLastToken())
              return ObjJComment_END;
          else
              return ObjJComment_TEXT;
	}
}
<LINE_BEGINNING> "*"+ {
	yybegin(CONTENTS_BEGINNING);
	return ObjJComment_LEADING_ASTERISK;
}

<CONTENTS_BEGINNING> {
	"@"{TAG_NAME} {
			ObjJDocCommentKnownTags tag = ObjJDocCommentKnownTags.Companion.findByTagName(zzBuffer.subSequence(zzStartRead, zzMarkedPos));
			yybegin(tag != null && tag.isReferenceRequired() ? TAG_BEGINNING : TAG_TEXT_BEGINNING);
			return ObjJComment_TAG_NAME;
		}
}

<TAG_BEGINNING> {
    {WHITE_SPACE_CHAR}+ {
        if (yytextContainLineBreaks()) {
            yybegin(LINE_BEGINNING);
        }
        return TokenType.WHITE_SPACE;
    }

    {IDENTIFIER} {
		  yybegin(TAG_TEXT_BEGINNING);
		  return ObjJComment_ID;
	}

    . {
        yybegin(CONTENTS);
        return ObjJComment_TEXT;
    }
}

<TAG_TEXT_BEGINNING> {
    {WHITE_SPACE_CHAR}+ {
        if (yytextContainLineBreaks()) {
            yybegin(LINE_BEGINNING);
        }
        return TokenType.WHITE_SPACE;
    }

    "|"|","	{ return ObjJComment_TAG_VALUE_DELIMITER; }

    {IDENTIFIER} {
		  if (!prevCharIsPipeOrDot)
			  yybegin(CONTENTS);
		  return ObjJComment_ID;
	}

	"." { return ObjJComment_DOT; }

	. {
				yybegin(CONTENTS);
				return ObjJComment_TEXT;
			}
}

<CONTENTS> {
	{WHITE_SPACE_CHAR}+ {
        if (yytextContainLineBreaks()) {
            yybegin(LINE_BEGINNING);
        }
        return TokenType.WHITE_SPACE;
    }
    . { return ObjJComment_TEXT; }
}

[^] { return ObjJComment_TEXT; }
