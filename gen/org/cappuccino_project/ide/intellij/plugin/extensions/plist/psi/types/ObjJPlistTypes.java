// This is a generated file. Not intended for manual editing.
package org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi.types;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi.impl.*;

public interface ObjJPlistTypes {

  IElementType ObjJPlist_ARRAY = new IElementType("ObjJPlist_ARRAY", null);
  IElementType ObjJPlist_BOOLEAN = new IElementType("ObjJPlist_BOOLEAN", null);
  IElementType ObjJPlist_DATA_VALUE = new IElementType("ObjJPlist_DATA_VALUE", null);
  IElementType ObjJPlist_DICT = new IElementType("ObjJPlist_DICT", null);
  IElementType ObjJPlist_DOC_TYPE = new IElementType("ObjJPlist_DOC_TYPE", null);
  IElementType ObjJPlist_DOC_TYPE_PARAM_LIST = new IElementType("ObjJPlist_DOC_TYPE_PARAM_LIST", null);
  IElementType ObjJPlist_INTEGER = new IElementType("ObjJPlist_INTEGER", null);
  IElementType ObjJPlist_KEY_PROPERTY = new IElementType("ObjJPlist_KEY_PROPERTY", null);
  IElementType ObjJPlist_PLIST = new IElementType("ObjJPlist_PLIST", null);
  IElementType ObjJPlist_PLIST_OPEN_TAG = new IElementType("ObjJPlist_PLIST_OPEN_TAG", null);
  IElementType ObjJPlist_PROPERTY = new IElementType("ObjJPlist_PROPERTY", null);
  IElementType ObjJPlist_REAL_NUMBER = new IElementType("ObjJPlist_REAL_NUMBER", null);
  IElementType ObjJPlist_STRING = new IElementType("ObjJPlist_STRING", null);
  IElementType ObjJPlist_STRING_LITERAL = new IElementType("ObjJPlist_STRING_LITERAL", null);
  IElementType ObjJPlist_STRING_VALUE = new IElementType("ObjJPlist_STRING_VALUE", null);
  IElementType ObjJPlist_XML_HEADER = new IElementType("ObjJPlist_XML_HEADER", null);
  IElementType ObjJPlist_XML_TAG_PROPERTY = new IElementType("ObjJPlist_XML_TAG_PROPERTY", null);

  IElementType ObjJPlist_ARRAY_CLOSE = new ObjJPlistTokenType("</array>");
  IElementType ObjJPlist_ARRAY_OPEN = new ObjJPlistTokenType("<array>");
  IElementType ObjJPlist_CLOSING_HEADER_BRACKET = new ObjJPlistTokenType("?>");
  IElementType ObjJPlist_COMMENT = new ObjJPlistTokenType("COMMENT");
  IElementType ObjJPlist_DATA_CLOSE = new ObjJPlistTokenType("</data>");
  IElementType ObjJPlist_DATA_LITERAL = new ObjJPlistTokenType("regexp=.+?(!</data>)");
  IElementType ObjJPlist_DATA_OPEN = new ObjJPlistTokenType("<data>");
  IElementType ObjJPlist_DATE_CLOSE = new ObjJPlistTokenType("</date>");
  IElementType ObjJPlist_DATE_OPEN = new ObjJPlistTokenType("<date>");
  IElementType ObjJPlist_DECIMAL_LITERAL = new ObjJPlistTokenType("DECIMAL_LITERAL");
  IElementType ObjJPlist_DICT_CLOSE = new ObjJPlistTokenType("</dict>");
  IElementType ObjJPlist_DICT_OPEN = new ObjJPlistTokenType("<dict>");
  IElementType ObjJPlist_DOCTYPE_OPEN = new ObjJPlistTokenType("<!DOCTYPE");
  IElementType ObjJPlist_DOUBLE_QUOTE_STRING_LITERAL = new ObjJPlistTokenType("DOUBLE_QUOTE_STRING_LITERAL");
  IElementType ObjJPlist_EQUALS = new ObjJPlistTokenType("=");
  IElementType ObjJPlist_FALSE = new ObjJPlistTokenType("<false/>");
  IElementType ObjJPlist_GT = new ObjJPlistTokenType(">");
  IElementType ObjJPlist_ID = new ObjJPlistTokenType("ID");
  IElementType ObjJPlist_INTEGER_CLOSE = new ObjJPlistTokenType("</integer>");
  IElementType ObjJPlist_INTEGER_LITERAL = new ObjJPlistTokenType("INTEGER_LITERAL");
  IElementType ObjJPlist_INTEGER_OPEN = new ObjJPlistTokenType("<integer>");
  IElementType ObjJPlist_KEY_CLOSE = new ObjJPlistTokenType("</key>");
  IElementType ObjJPlist_KEY_OPEN = new ObjJPlistTokenType("<key>");
  IElementType ObjJPlist_LT = new ObjJPlistTokenType("<");
  IElementType ObjJPlist_LT_SLASH = new ObjJPlistTokenType("</");
  IElementType ObjJPlist_OPENING_HEADER_BRACKET = new ObjJPlistTokenType("<?");
  IElementType ObjJPlist_PLIST_CLOSE = new ObjJPlistTokenType("</plist>");
  IElementType ObjJPlist_PLIST_OPEN = new ObjJPlistTokenType("<plist");
  IElementType ObjJPlist_REAL_CLOSE = new ObjJPlistTokenType("</real>");
  IElementType ObjJPlist_REAL_OPEN = new ObjJPlistTokenType("<real>");
  IElementType ObjJPlist_SINGLE_QUOTE_STRING_LITERAL = new ObjJPlistTokenType("SINGLE_QUOTE_STRING_LITERAL");
  IElementType ObjJPlist_SLASH_GT = new ObjJPlistTokenType("/>");
  IElementType ObjJPlist_STRING_CLOSE = new ObjJPlistTokenType("</string>");
  IElementType ObjJPlist_STRING_OPEN = new ObjJPlistTokenType("<string>");
  IElementType ObjJPlist_STRING_TAG_LITERAL = new ObjJPlistTokenType("regexp=.+(!</string>)");
  IElementType ObjJPlist_TRUE = new ObjJPlistTokenType("<true/>");
  IElementType ObjJPlist_VERSION = new ObjJPlistTokenType("version");
  IElementType ObjJPlist_XML = new ObjJPlistTokenType("xml");
  IElementType ObjJPlist_XML_TAG_PROPERTY_KEY = new ObjJPlistTokenType("XML_TAG_PROPERTY_KEY");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == ObjJPlist_ARRAY) {
        return new ObjJPlistArrayImpl(node);
      }
      else if (type == ObjJPlist_BOOLEAN) {
        return new ObjJPlistBooleanImpl(node);
      }
      else if (type == ObjJPlist_DATA_VALUE) {
        return new ObjJPlistDataValueImpl(node);
      }
      else if (type == ObjJPlist_DICT) {
        return new ObjJPlistDictImpl(node);
      }
      else if (type == ObjJPlist_DOC_TYPE) {
        return new ObjJPlistDocTypeImpl(node);
      }
      else if (type == ObjJPlist_DOC_TYPE_PARAM_LIST) {
        return new ObjJPlistDocTypeParamListImpl(node);
      }
      else if (type == ObjJPlist_INTEGER) {
        return new ObjJPlistIntegerImpl(node);
      }
      else if (type == ObjJPlist_KEY_PROPERTY) {
        return new ObjJPlistKeyPropertyImpl(node);
      }
      else if (type == ObjJPlist_PLIST) {
        return new ObjJPlistPlistImpl(node);
      }
      else if (type == ObjJPlist_PLIST_OPEN_TAG) {
        return new ObjJPlistPlistOpenTagImpl(node);
      }
      else if (type == ObjJPlist_PROPERTY) {
        return new ObjJPlistPropertyImpl(node);
      }
      else if (type == ObjJPlist_REAL_NUMBER) {
        return new ObjJPlistRealNumberImpl(node);
      }
      else if (type == ObjJPlist_STRING) {
        return new ObjJPlistStringImpl(node);
      }
      else if (type == ObjJPlist_STRING_LITERAL) {
        return new ObjJPlistStringLiteralImpl(node);
      }
      else if (type == ObjJPlist_STRING_VALUE) {
        return new ObjJPlistStringValueImpl(node);
      }
      else if (type == ObjJPlist_XML_HEADER) {
        return new ObjJPlistXmlHeaderImpl(node);
      }
      else if (type == ObjJPlist_XML_TAG_PROPERTY) {
        return new ObjJPlistXmlTagPropertyImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
