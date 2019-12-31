package cappuccino.ide.intellij.plugin.comments.lexer;

import cappuccino.ide.intellij.plugin.comments.parser.ObjJDocCommentKnownTag;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.CharArrayUtil;
import com.intellij.psi.TokenType;
import com.intellij.lexer.FlexLexer;
import java.util.List;

import java.util.Arrays;
import java.util.logging.Logger;
import static cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocCommentTypes.*;

%%

%{

	private static final List<String> ID_VALID_CHARS = Arrays.asList("_$@abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".split(""));

		public _ObjJDocCommentLexer() {
			this((java.io.Reader)null);
		}
		public boolean prevCharIs(char c) {
			if (zzMarkedPos == 0)
				  return false;
			int zzMarkedPos = zzCurrentPos;
			while(zzMarkedPos != 0 && Character.isWhitespace(zzBuffer.charAt(zzMarkedPos))) {
				  zzMarkedPos -= 1;
				  Logger.getLogger("_ObjJDocCommentLexer").info("IsAt: " + zzBuffer.charAt(zzMarkedPos));
			}
			Logger.getLogger("_ObjJDocCommentLexer").info("EndsAt: " + zzBuffer.charAt(zzMarkedPos));
			return zzBuffer.charAt(zzMarkedPos) == c;
		}

		public boolean prevElementIsTag() {
			char it = zzBuffer.charAt(zzMarkedPos);
			boolean isDollar = false;
			while(zzMarkedPos != 0 && Character.isWhitespace(it) || ID_VALID_CHARS.contains(it)) {
				if (isDollar && it != '@')
					return false;
				if (it == '$') {
					isDollar = true;
				} else if (it == '@') {
					return true;
				} else {
					isDollar = false;
				}
				zzMarkedPos -= 1;
				it = zzBuffer.charAt(zzMarkedPos);
			}
			return false;
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

ID_FIRST_CHAR=[a-zA-Z_$]
ID_SECONDARY_CHAR=[a-zA-Z_0-9]
BLOCKSTART = \/\*([*]+|[!])?
WHITE_SPACE_CHAR=[ \t\n\x0B\f\r]+
IDENTIFIER={ID_FIRST_CHAR}({ID_SECONDARY_CHAR}*)
TAG_NAME={IDENTIFIER}
TEXT=([^*\n]|"*"[^*/\n])+
BLOCK_END=[*][/]
%state LINE_BEGINNING TAG_BEGINNING TAG_TEXT_BEGINNING CONTENTS
%%

<YYINITIAL> {
	{BLOCKSTART} {
		yybegin(LINE_BEGINNING);
		return ObjJDocComment_START;
	}
}

<YYINITIAL, LINE_BEGINNING, TAG_BEGINNING, TAG_TEXT_BEGINNING, CONTENTS> {
	{BLOCK_END} {
		return ObjJDocComment_END;
	}
	{WHITE_SPACE_CHAR}+ {
		if (yytextContainLineBreaks()) {
			yybegin(LINE_BEGINNING);
		}
		return TokenType.WHITE_SPACE;
	}
}
<LINE_BEGINNING> {
	"*" {
		return ObjJDocComment_LEADING_ASTERISK;
	}
	"@"{TAG_NAME} {
		ObjJDocCommentKnownTag tag = ObjJDocCommentKnownTag.Companion.findByTagName(zzBuffer.subSequence(zzStartRead, zzMarkedPos));
		yybegin(tag != null && tag.isReferenceRequired() ? TAG_BEGINNING : CONTENTS);
		return ObjJDocComment_TAG_NAME;
	}
	[^@\s] {
	  	yybegin(CONTENTS);
	}
}

<TAG_BEGINNING> {
    {IDENTIFIER} {
		return ObjJDocComment_ID;
	}

	"." 	{ return ObjJDocComment_DOT; }

    "|"|","	{ return ObjJDocComment_TAG_VALUE_DELIMITER; }

	[^@\s.|,] {
		yybegin(CONTENTS);
  	}
}

<CONTENTS> {
	{TEXT} {
	  	yybegin(LINE_BEGINNING);
	  	return ObjJDocComment_TEXT_BODY;
  	}
}
[^] { return TokenType.BAD_CHARACTER; }
