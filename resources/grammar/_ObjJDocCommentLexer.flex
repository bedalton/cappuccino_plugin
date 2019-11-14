package cappuccino.ide.intellij.plugin.comments.lexer;

import cappuccino.ide.intellij.plugin.comments.parser.ObjJDocCommentKnownTag;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.CharArrayUtil;
import com.intellij.psi.TokenType;
import com.intellij.lexer.FlexLexer;

import static cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocCommentTypes.*;

%%

%{
  public _ObjJDocCommentLexer() {
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

  private boolean isLastToken() {
    return zzMarkedPos == zzBuffer.length();
  }

%}

%public
%class _ObjJDocCommentLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

DIGIT=[0-9]
ALPHA=[:jletter:]
BLOCKSTART = \/\*([*]+|[!])?
WHITE_SPACE_CHAR=[ \t\n\x0B\f\r]+
IDENTIFIER={ALPHA}({ALPHA}|{DIGIT})*
TAG_NAME={IDENTIFIER}
%state MARK_CLASSES LINE_BEGINNING TAG_BEGINNING TAG_TEXT_BEGINNING CONTENTS_BEGINNING CONTENTS
%%

<YYINITIAL> {
	{BLOCKSTART} {
		yybegin(CONTENTS_BEGINNING);
		return ObjJDocComment_START;
	}
}

<YYINITIAL,MARK_CLASSES, LINE_BEGINNING, TAG_BEGINNING, TAG_TEXT_BEGINNING, CONTENTS_BEGINNING, CONTENTS> {
	"*"+ "/" {
          if (isLastToken())
              return ObjJDocComment_END;
          else
              return ObjJDocComment_TEXT_BODY;
	}
	{WHITE_SPACE_CHAR}+ {
            if (yytextContainLineBreaks()) {
                yybegin(LINE_BEGINNING);
            }
            return TokenType.WHITE_SPACE;
        }
}
<LINE_BEGINNING> {
	"*"+ {
		yybegin(CONTENTS_BEGINNING);
		return ObjJDocComment_LEADING_ASTERISK;
	}
}

<CONTENTS_BEGINNING> {
	"@"{TAG_NAME} {
				ObjJDocCommentKnownTag tag = ObjJDocCommentKnownTag.Companion.findByTagName(zzBuffer.subSequence(zzStartRead, zzMarkedPos));
				yybegin(tag != null && tag.isReferenceRequired() ? TAG_BEGINNING : TAG_TEXT_BEGINNING);
				return ObjJDocComment_TAG_NAME;
			}
	. {
          yybegin(CONTENTS);
          return ObjJDocComment_TEXT_BODY;
      }
}

<TAG_BEGINNING> {
    {IDENTIFIER} {
		  yybegin(TAG_TEXT_BEGINNING);
		  return ObjJDocComment_ID;
	}

	'.' { return ObjJDocComment_DOT; }

    [^\.] {
        yybegin(CONTENTS);
        return ObjJDocComment_TEXT_BODY;
    }
}

<TAG_TEXT_BEGINNING> {

    "|"|","	{ return ObjJDocComment_TAG_VALUE_DELIMITER; }

    {IDENTIFIER} {
		  if (!prevCharIsPipeOrDot())
			  yybegin(CONTENTS);
		  return ObjJDocComment_ID;
	}

	"." { return ObjJDocComment_DOT; }

	[^\.] {
				yybegin(CONTENTS);
				return ObjJDocComment_TEXT_BODY;
			}
}

<CONTENTS> {
    . { return ObjJDocComment_TEXT_BODY; }
}
[^] { return TokenType.ERROR_ELEMENT; }
