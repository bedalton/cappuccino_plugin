// This is a generated file. Not intended for manual editing.
package org.cappuccino_project.ide.intellij.plugin.extensions.plist.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi.types.ObjJPlistTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class ObjJPlistParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == ObjJPlist_ARRAY) {
      r = array(b, 0);
    }
    else if (t == ObjJPlist_BOOLEAN) {
      r = boolean_$(b, 0);
    }
    else if (t == ObjJPlist_DATA_VALUE) {
      r = dataValue(b, 0);
    }
    else if (t == ObjJPlist_DICT) {
      r = dict(b, 0);
    }
    else if (t == ObjJPlist_DOC_TYPE) {
      r = docType(b, 0);
    }
    else if (t == ObjJPlist_DOC_TYPE_PARAM_LIST) {
      r = docTypeParamList(b, 0);
    }
    else if (t == ObjJPlist_INTEGER) {
      r = integer(b, 0);
    }
    else if (t == ObjJPlist_KEY_PROPERTY) {
      r = keyProperty(b, 0);
    }
    else if (t == ObjJPlist_PLIST) {
      r = plist(b, 0);
    }
    else if (t == ObjJPlist_PLIST_OPEN_TAG) {
      r = plistOpenTag(b, 0);
    }
    else if (t == ObjJPlist_PROPERTY) {
      r = property(b, 0);
    }
    else if (t == ObjJPlist_REAL_NUMBER) {
      r = realNumber(b, 0);
    }
    else if (t == ObjJPlist_STRING) {
      r = string(b, 0);
    }
    else if (t == ObjJPlist_STRING_LITERAL) {
      r = stringLiteral(b, 0);
    }
    else if (t == ObjJPlist_STRING_VALUE) {
      r = stringValue(b, 0);
    }
    else if (t == ObjJPlist_XML_HEADER) {
      r = xmlHeader(b, 0);
    }
    else if (t == ObjJPlist_XML_TAG_PROPERTY) {
      r = xmlTagProperty(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return plistFile(b, l + 1);
  }

  /* ********************************************************** */
  // '<array>' COMMENT* arrayValueList? COMMENT*  '</array>'
  public static boolean array(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array")) return false;
    if (!nextTokenIs(b, ObjJPlist_ARRAY_OPEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_ARRAY, null);
    r = consumeToken(b, ObjJPlist_ARRAY_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, array_1(b, l + 1));
    r = p && report_error_(b, array_2(b, l + 1)) && r;
    r = p && report_error_(b, array_3(b, l + 1)) && r;
    r = p && consumeToken(b, ObjJPlist_ARRAY_CLOSE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // COMMENT*
  private static boolean array_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "array_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // arrayValueList?
  private static boolean array_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_2")) return false;
    arrayValueList(b, l + 1);
    return true;
  }

  // COMMENT*
  private static boolean array_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "array_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // value
  // 	{
  // 		//recoverWhile=arrayValueList_recover
  // 	}
  static boolean arrayValueList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayValueList")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = value(b, l + 1);
    r = r && arrayValueList_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // {
  // 		//recoverWhile=arrayValueList_recover
  // 	}
  private static boolean arrayValueList_1(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // !('</array>'|<<eof>>)
  static boolean arrayValueList_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayValueList_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !arrayValueList_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '</array>'|<<eof>>
  private static boolean arrayValueList_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayValueList_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_ARRAY_CLOSE);
    if (!r) r = eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '<true/>'|'<false/>'
  public static boolean boolean_$(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "boolean_$")) return false;
    if (!nextTokenIs(b, "<boolean $>", ObjJPlist_FALSE, ObjJPlist_TRUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_BOOLEAN, "<boolean $>");
    r = consumeToken(b, ObjJPlist_TRUE);
    if (!r) r = consumeToken(b, ObjJPlist_FALSE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '<data>' COMMENT*  DATA_LITERAL COMMENT*  '</data>'
  public static boolean dataValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dataValue")) return false;
    if (!nextTokenIs(b, ObjJPlist_DATA_OPEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_DATA_OPEN);
    r = r && dataValue_1(b, l + 1);
    r = r && consumeToken(b, ObjJPlist_DATA_LITERAL);
    r = r && dataValue_3(b, l + 1);
    r = r && consumeToken(b, ObjJPlist_DATA_CLOSE);
    exit_section_(b, m, ObjJPlist_DATA_VALUE, r);
    return r;
  }

  // COMMENT*
  private static boolean dataValue_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dataValue_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "dataValue_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMENT*
  private static boolean dataValue_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dataValue_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "dataValue_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // '<dict>'  propertyList? '</dict>'
  public static boolean dict(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dict")) return false;
    if (!nextTokenIs(b, ObjJPlist_DICT_OPEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_DICT, null);
    r = consumeToken(b, ObjJPlist_DICT_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, dict_1(b, l + 1));
    r = p && consumeToken(b, ObjJPlist_DICT_CLOSE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // propertyList?
  private static boolean dict_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dict_1")) return false;
    propertyList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '<!DOCTYPE' docTypeParamList? '>'
  public static boolean docType(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docType")) return false;
    if (!nextTokenIs(b, ObjJPlist_DOCTYPE_OPEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_DOC_TYPE, null);
    r = consumeToken(b, ObjJPlist_DOCTYPE_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, docType_1(b, l + 1));
    r = p && consumeToken(b, ObjJPlist_GT) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // docTypeParamList?
  private static boolean docType_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docType_1")) return false;
    docTypeParamList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (XML_TAG_PROPERTY_KEY|stringLiteral)+
  // 	{
  // 		//recoverWhile=docTypeParamList_recover
  // 	}
  public static boolean docTypeParamList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docTypeParamList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_DOC_TYPE_PARAM_LIST, "<doc type param list>");
    r = docTypeParamList_0(b, l + 1);
    r = r && docTypeParamList_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (XML_TAG_PROPERTY_KEY|stringLiteral)+
  private static boolean docTypeParamList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docTypeParamList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = docTypeParamList_0_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!docTypeParamList_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "docTypeParamList_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // XML_TAG_PROPERTY_KEY|stringLiteral
  private static boolean docTypeParamList_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docTypeParamList_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_XML_TAG_PROPERTY_KEY);
    if (!r) r = stringLiteral(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // {
  // 		//recoverWhile=docTypeParamList_recover
  // 	}
  private static boolean docTypeParamList_1(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // !('>'|'<'|'<?')
  static boolean docTypeParamList_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docTypeParamList_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !docTypeParamList_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '>'|'<'|'<?'
  private static boolean docTypeParamList_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docTypeParamList_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_GT);
    if (!r) r = consumeToken(b, ObjJPlist_LT);
    if (!r) r = consumeToken(b, ObjJPlist_OPENING_HEADER_BRACKET);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '<integer>' COMMENT*  INTEGER_LITERAL COMMENT*  '</integer>'
  public static boolean integer(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "integer")) return false;
    if (!nextTokenIs(b, ObjJPlist_INTEGER_OPEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_INTEGER, null);
    r = consumeToken(b, ObjJPlist_INTEGER_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, integer_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, ObjJPlist_INTEGER_LITERAL)) && r;
    r = p && report_error_(b, integer_3(b, l + 1)) && r;
    r = p && consumeToken(b, ObjJPlist_INTEGER_CLOSE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // COMMENT*
  private static boolean integer_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "integer_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "integer_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMENT*
  private static boolean integer_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "integer_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "integer_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // '<key>' ID '</key>'
  public static boolean keyProperty(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyProperty")) return false;
    if (!nextTokenIs(b, ObjJPlist_KEY_OPEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_KEY_PROPERTY, null);
    r = consumeTokens(b, 1, ObjJPlist_KEY_OPEN, ObjJPlist_ID, ObjJPlist_KEY_CLOSE);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // plistOpenTag COMMENT*  dict? COMMENT* '</plist>'
  public static boolean plist(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plist")) return false;
    if (!nextTokenIs(b, ObjJPlist_PLIST_OPEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_PLIST, null);
    r = plistOpenTag(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, plist_1(b, l + 1));
    r = p && report_error_(b, plist_2(b, l + 1)) && r;
    r = p && report_error_(b, plist_3(b, l + 1)) && r;
    r = p && consumeToken(b, ObjJPlist_PLIST_CLOSE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // COMMENT*
  private static boolean plist_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plist_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "plist_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // dict?
  private static boolean plist_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plist_2")) return false;
    dict(b, l + 1);
    return true;
  }

  // COMMENT*
  private static boolean plist_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plist_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "plist_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // COMMENT* xmlHeader? COMMENT* docType? COMMENT* plist? COMMENT* <<eof>>
  static boolean plistFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plistFile")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = plistFile_0(b, l + 1);
    r = r && plistFile_1(b, l + 1);
    r = r && plistFile_2(b, l + 1);
    r = r && plistFile_3(b, l + 1);
    r = r && plistFile_4(b, l + 1);
    r = r && plistFile_5(b, l + 1);
    r = r && plistFile_6(b, l + 1);
    r = r && eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMENT*
  private static boolean plistFile_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plistFile_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "plistFile_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // xmlHeader?
  private static boolean plistFile_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plistFile_1")) return false;
    xmlHeader(b, l + 1);
    return true;
  }

  // COMMENT*
  private static boolean plistFile_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plistFile_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "plistFile_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // docType?
  private static boolean plistFile_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plistFile_3")) return false;
    docType(b, l + 1);
    return true;
  }

  // COMMENT*
  private static boolean plistFile_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plistFile_4")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "plistFile_4", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // plist?
  private static boolean plistFile_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plistFile_5")) return false;
    plist(b, l + 1);
    return true;
  }

  // COMMENT*
  private static boolean plistFile_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plistFile_6")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "plistFile_6", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // '<plist' xmlTagPropertiesList? '>'
  public static boolean plistOpenTag(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plistOpenTag")) return false;
    if (!nextTokenIs(b, ObjJPlist_PLIST_OPEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_PLIST_OPEN);
    r = r && plistOpenTag_1(b, l + 1);
    r = r && consumeToken(b, ObjJPlist_GT);
    exit_section_(b, m, ObjJPlist_PLIST_OPEN_TAG, r);
    return r;
  }

  // xmlTagPropertiesList?
  private static boolean plistOpenTag_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plistOpenTag_1")) return false;
    xmlTagPropertiesList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // COMMENT* keyProperty COMMENT*  value* COMMENT*
  public static boolean property(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property")) return false;
    if (!nextTokenIs(b, "<property>", ObjJPlist_KEY_OPEN, ObjJPlist_COMMENT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_PROPERTY, "<property>");
    r = property_0(b, l + 1);
    r = r && keyProperty(b, l + 1);
    r = r && property_2(b, l + 1);
    r = r && property_3(b, l + 1);
    r = r && property_4(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // COMMENT*
  private static boolean property_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_0")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "property_0", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMENT*
  private static boolean property_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_2")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "property_2", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // value*
  private static boolean property_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!value(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "property_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMENT*
  private static boolean property_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_4")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "property_4", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // property+
  // 	{
  // 		//recoverWhile=propertyList_recover
  // 	}
  static boolean propertyList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "propertyList")) return false;
    if (!nextTokenIs(b, "", ObjJPlist_KEY_OPEN, ObjJPlist_COMMENT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = propertyList_0(b, l + 1);
    r = r && propertyList_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // property+
  private static boolean propertyList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "propertyList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = property(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!property(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "propertyList_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // {
  // 		//recoverWhile=propertyList_recover
  // 	}
  private static boolean propertyList_1(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // !('</plist>'|<<eof>>)
  static boolean propertyList_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "propertyList_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !propertyList_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '</plist>'|<<eof>>
  private static boolean propertyList_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "propertyList_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_PLIST_CLOSE);
    if (!r) r = eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !('<array>'|'<data>'|'<string>'|'<integer>'|'<real>'|'<key>'|'</plist>')
  static boolean property_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !property_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '<array>'|'<data>'|'<string>'|'<integer>'|'<real>'|'<key>'|'</plist>'
  private static boolean property_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_ARRAY_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_DATA_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_STRING_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_INTEGER_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_REAL_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_KEY_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_PLIST_CLOSE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '<real>'COMMENT*  DECIMAL_LITERAL COMMENT*  '</real>'
  public static boolean realNumber(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "realNumber")) return false;
    if (!nextTokenIs(b, ObjJPlist_REAL_OPEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_REAL_NUMBER, null);
    r = consumeToken(b, ObjJPlist_REAL_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, realNumber_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, ObjJPlist_DECIMAL_LITERAL)) && r;
    r = p && report_error_(b, realNumber_3(b, l + 1)) && r;
    r = p && consumeToken(b, ObjJPlist_REAL_CLOSE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // COMMENT*
  private static boolean realNumber_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "realNumber_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "realNumber_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMENT*
  private static boolean realNumber_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "realNumber_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "realNumber_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // '<string>' COMMENT* stringValue COMMENT* '</string>'
  public static boolean string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string")) return false;
    if (!nextTokenIs(b, ObjJPlist_STRING_OPEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_STRING, null);
    r = consumeToken(b, ObjJPlist_STRING_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, string_1(b, l + 1));
    r = p && report_error_(b, stringValue(b, l + 1)) && r;
    r = p && report_error_(b, string_3(b, l + 1)) && r;
    r = p && consumeToken(b, ObjJPlist_STRING_CLOSE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // COMMENT*
  private static boolean string_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "string_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMENT*
  private static boolean string_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!consumeToken(b, ObjJPlist_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "string_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // DOUBLE_QUOTE_STRING_LITERAL
  // 	|	SINGLE_QUOTE_STRING_LITERAL
  public static boolean stringLiteral(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringLiteral")) return false;
    if (!nextTokenIs(b, "<string literal>", ObjJPlist_DOUBLE_QUOTE_STRING_LITERAL, ObjJPlist_SINGLE_QUOTE_STRING_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_STRING_LITERAL, "<string literal>");
    r = consumeToken(b, ObjJPlist_DOUBLE_QUOTE_STRING_LITERAL);
    if (!r) r = consumeToken(b, ObjJPlist_SINGLE_QUOTE_STRING_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // STRING_TAG_LITERAL
  public static boolean stringValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "stringValue")) return false;
    if (!nextTokenIs(b, "<value>", ObjJPlist_STRING_TAG_LITERAL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_STRING_VALUE, "<value>");
    r = consumeToken(b, ObjJPlist_STRING_TAG_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // boolean
  // 	|	dict
  // 	|	array
  // 	|	realNumber
  // 	|	integer
  // 	| 	dataValue
  // 	|	string
  static boolean value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = boolean_$(b, l + 1);
    if (!r) r = dict(b, l + 1);
    if (!r) r = array(b, l + 1);
    if (!r) r = realNumber(b, l + 1);
    if (!r) r = integer(b, l + 1);
    if (!r) r = dataValue(b, l + 1);
    if (!r) r = string(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '<?''xml' xmlTagPropertiesList? '?>'
  public static boolean xmlHeader(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "xmlHeader")) return false;
    if (!nextTokenIs(b, ObjJPlist_OPENING_HEADER_BRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_XML_HEADER, null);
    r = consumeTokens(b, 1, ObjJPlist_OPENING_HEADER_BRACKET, ObjJPlist_XML);
    p = r; // pin = 1
    r = r && report_error_(b, xmlHeader_2(b, l + 1));
    r = p && consumeToken(b, ObjJPlist_CLOSING_HEADER_BRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // xmlTagPropertiesList?
  private static boolean xmlHeader_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "xmlHeader_2")) return false;
    xmlTagPropertiesList(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // xmlTagProperty+
  // 	{
  // 		//recoverWhile=xmlTagPropertiesList_recover
  // 	}
  static boolean xmlTagPropertiesList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "xmlTagPropertiesList")) return false;
    if (!nextTokenIs(b, "", ObjJPlist_VERSION, ObjJPlist_XML_TAG_PROPERTY_KEY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = xmlTagPropertiesList_0(b, l + 1);
    r = r && xmlTagPropertiesList_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // xmlTagProperty+
  private static boolean xmlTagPropertiesList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "xmlTagPropertiesList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = xmlTagProperty(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!xmlTagProperty(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "xmlTagPropertiesList_0", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // {
  // 		//recoverWhile=xmlTagPropertiesList_recover
  // 	}
  private static boolean xmlTagPropertiesList_1(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // !('?>'|'>'|'<'|<<eof>>)
  static boolean xmlTagPropertiesList_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "xmlTagPropertiesList_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !xmlTagPropertiesList_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '?>'|'>'|'<'|<<eof>>
  private static boolean xmlTagPropertiesList_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "xmlTagPropertiesList_recover_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_CLOSING_HEADER_BRACKET);
    if (!r) r = consumeToken(b, ObjJPlist_GT);
    if (!r) r = consumeToken(b, ObjJPlist_LT);
    if (!r) r = eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // XML_TAG_PROPERTY_KEY '=' stringLiteral
  // 	|	'version' '=' stringLiteral
  public static boolean xmlTagProperty(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "xmlTagProperty")) return false;
    if (!nextTokenIs(b, "<xml tag property>", ObjJPlist_VERSION, ObjJPlist_XML_TAG_PROPERTY_KEY)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_XML_TAG_PROPERTY, "<xml tag property>");
    r = xmlTagProperty_0(b, l + 1);
    if (!r) r = xmlTagProperty_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // XML_TAG_PROPERTY_KEY '=' stringLiteral
  private static boolean xmlTagProperty_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "xmlTagProperty_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokens(b, 2, ObjJPlist_XML_TAG_PROPERTY_KEY, ObjJPlist_EQUALS);
    p = r; // pin = 2
    r = r && stringLiteral(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // 'version' '=' stringLiteral
  private static boolean xmlTagProperty_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "xmlTagProperty_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokens(b, 2, ObjJPlist_VERSION, ObjJPlist_EQUALS);
    p = r; // pin = 2
    r = r && stringLiteral(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

}
