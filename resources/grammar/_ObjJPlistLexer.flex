package org.cappuccino_project.ide.intellij.plugin.extensions.plist.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi.types.ObjJPlistTypes.*;

%%

%{
	private int stateBeforeComment;
	private static final Logger LOGGER = Logger.getLogger("_PLIST_LEXER");
	public _ObjJPlistLexer() {
		this((java.io.Reader)null);
		log("Creating Plist lexer");
	}

	private static void log(String message) {
		LOGGER.log(Level.INFO, message);
	}

%}

%public
%class _ObjJPlistLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

SINGLE_QUOTE_STRING_LITERAL='([^'\\\r\n]|\\['\\bfnrtv])*'
DOUBLE_QUOTE_STRING_LITERAL=(@\"|\")([^\"\\\r\n]|\\[\"\\bfnrtv])*\"
DECIMAL_LITERAL=[0-9]*\.[0-9]+([eE] [+-]? [0-9]+)?
INTEGER_LITERAL=[0-9]+
ID = [_a-zA-Z][_a-zA-Z0-9]*
XML_TAG_PROPERTY_KEY = [_a-zA-Z\-][_a-zA-Z0-9\-]*

%state IN_TAG IN_STRING IN_DATA
%x COMMENT
%%


<IN_DATA, IN_STRING, YYINITIAL> {
  "<!--"							 { stateBeforeComment = yystate(); yybegin(COMMENT); }
}
<IN_STRING> {
	("."|.)+/"</string>" 					{ return ObjJPlist_STRING_TAG_LITERAL; }
	"</string>"						{ yybegin(YYINITIAL); return ObjJPlist_STRING_CLOSE; }
}
<IN_DATA> {
	"</data>"						{ yybegin(YYINITIAL); return ObjJPlist_DATA_CLOSE; }
	("."|.)+/"</data>" 					{ return ObjJPlist_DATA_LITERAL; }
}

<IN_TAG> {
  "?>"                               { yybegin(YYINITIAL); return ObjJPlist_CLOSING_HEADER_BRACKET; }
  ">"                                { yybegin(YYINITIAL); return ObjJPlist_GT; }
  "="                                { return ObjJPlist_EQUALS; }
  "xml"								 { return ObjJPlist_XML; }
  {XML_TAG_PROPERTY_KEY}			 { return ObjJPlist_XML_TAG_PROPERTY_KEY; }
  {SINGLE_QUOTE_STRING_LITERAL}      { return ObjJPlist_SINGLE_QUOTE_STRING_LITERAL; }
  {DOUBLE_QUOTE_STRING_LITERAL}      { return ObjJPlist_DOUBLE_QUOTE_STRING_LITERAL; }

}

<COMMENT> {
	(.|\s)+/"-->" 					 { }
	"-->"							 { yybegin(stateBeforeComment >= 0 ? stateBeforeComment : YYINITIAL); stateBeforeComment = -1; return ObjJPlist_COMMENT; }
}


<YYINITIAL> {
  "<string>"                         { yybegin(IN_STRING); return ObjJPlist_STRING_OPEN; }
  "</string>"                        { return ObjJPlist_STRING_CLOSE; }
  "<real>"                           { return ObjJPlist_REAL_OPEN; }
  "</real>"                          { return ObjJPlist_REAL_CLOSE; }
  "<integer>"                        { return ObjJPlist_INTEGER_OPEN; }
  "</integer>"                       { return ObjJPlist_INTEGER_CLOSE; }
  "<true/>"                          { return ObjJPlist_TRUE; }
  "<false/>"                         { return ObjJPlist_FALSE; }
  "<key>"                            { return ObjJPlist_KEY_OPEN; }
  "</key>"                           { return ObjJPlist_KEY_CLOSE; }
  "<dict>"                           { return ObjJPlist_DICT_OPEN; }
  "</dict>"                          { return ObjJPlist_DICT_CLOSE; }
  "<array>"                          { return ObjJPlist_ARRAY_OPEN; }
  "</array>"                         { return ObjJPlist_ARRAY_CLOSE; }
  "<data>"                           { return ObjJPlist_DATA_OPEN; }
  "</data>"                          { return ObjJPlist_DATA_CLOSE; }
  "<date>"                           { return ObjJPlist_DATE_OPEN; }
  "</date>"                          { return ObjJPlist_DATE_CLOSE; }

  "<!DOCTYPE"                        { yybegin(IN_TAG); return ObjJPlist_DOCTYPE_OPEN; }
  "<plist"                           { yybegin(IN_TAG); return ObjJPlist_PLIST_OPEN; }
  "</plist>"                         { return ObjJPlist_PLIST_CLOSE; }
  "xml"                              { return ObjJPlist_XML; }
  "version"                          { return ObjJPlist_VERSION; }

  ">"                                { yybegin(YYINITIAL); return ObjJPlist_GT; }
  "<"                                { yybegin(IN_TAG); return ObjJPlist_LT; }
  "</"                               { return ObjJPlist_LT_SLASH; }
  "/>"                               { yybegin(YYINITIAL); return ObjJPlist_SLASH_GT; }
  "<?"                               { yybegin(IN_TAG); return ObjJPlist_OPENING_HEADER_BRACKET; }
  "?>"                               { yybegin(YYINITIAL); return ObjJPlist_CLOSING_HEADER_BRACKET; }
  {DECIMAL_LITERAL}                  { return ObjJPlist_DECIMAL_LITERAL; }
  {INTEGER_LITERAL}                  { return ObjJPlist_INTEGER_LITERAL; }
  {ID}								 { return ObjJPlist_ID; }
  '[\r\n\u2028\u2029]'               { return WHITE_SPACE; }
}

\s+                       { return WHITE_SPACE; }


[^] { return BAD_CHARACTER; }
