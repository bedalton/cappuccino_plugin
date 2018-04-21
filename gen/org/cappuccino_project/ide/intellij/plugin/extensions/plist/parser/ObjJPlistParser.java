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
    else if (t == ObjJPlist_KEY_NAME) {
      r = keyName(b, 0);
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
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_ARRAY, "<array>");
    r = consumeToken(b, ObjJPlist_ARRAY_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, array_1(b, l + 1));
    r = p && report_error_(b, array_2(b, l + 1)) && r;
    r = p && report_error_(b, array_3(b, l + 1)) && r;
    r = p && consumeToken(b, ObjJPlist_ARRAY_CLOSE) && r;
    exit_section_(b, l, m, r, p, recover_array_parser_);
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
  // value+
  static boolean arrayValueList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "arrayValueList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = value(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!value(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "arrayValueList", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, arrayValueList_recover_parser_);
    return r;
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
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_DATA_VALUE, "<data value>");
    r = consumeToken(b, ObjJPlist_DATA_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, dataValue_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, ObjJPlist_DATA_LITERAL)) && r;
    r = p && report_error_(b, dataValue_3(b, l + 1)) && r;
    r = p && consumeToken(b, ObjJPlist_DATA_CLOSE) && r;
    exit_section_(b, l, m, r, p, recover_dataValue_parser_);
    return r || p;
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
  public static boolean docTypeParamList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docTypeParamList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_DOC_TYPE_PARAM_LIST, "<doc type param list>");
    r = docTypeParamList_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!docTypeParamList_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "docTypeParamList", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, docTypeParamList_recover_parser_);
    return r;
  }

  // XML_TAG_PROPERTY_KEY|stringLiteral
  private static boolean docTypeParamList_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "docTypeParamList_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_XML_TAG_PROPERTY_KEY);
    if (!r) r = stringLiteral(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
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
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_INTEGER, "<integer>");
    r = consumeToken(b, ObjJPlist_INTEGER_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, integer_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, ObjJPlist_INTEGER_LITERAL)) && r;
    r = p && report_error_(b, integer_3(b, l + 1)) && r;
    r = p && consumeToken(b, ObjJPlist_INTEGER_CLOSE) && r;
    exit_section_(b, l, m, r, p, recover_integer_parser_);
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
  // ID ('.' ID)?
  public static boolean keyName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_KEY_NAME, "<key name>");
    r = consumeToken(b, ObjJPlist_ID);
    r = r && keyName_1(b, l + 1);
    exit_section_(b, l, m, r, false, recover_keyName_parser_);
    return r;
  }

  // ('.' ID)?
  private static boolean keyName_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyName_1")) return false;
    keyName_1_0(b, l + 1);
    return true;
  }

  // '.' ID
  private static boolean keyName_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyName_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ".");
    r = r && consumeToken(b, ObjJPlist_ID);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '<key>' keyName '</key>'
  public static boolean keyProperty(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "keyProperty")) return false;
    if (!nextTokenIs(b, ObjJPlist_KEY_OPEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_KEY_PROPERTY, null);
    r = consumeToken(b, ObjJPlist_KEY_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, keyName(b, l + 1));
    r = p && consumeToken(b, ObjJPlist_KEY_CLOSE) && r;
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
  // COMMENT* xmlHeader? COMMENT* docType? COMMENT* plist? COMMENT* <<eof>>?
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
    r = r && plistFile_7(b, l + 1);
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

  // <<eof>>?
  private static boolean plistFile_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "plistFile_7")) return false;
    eof(b, l + 1);
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
  static boolean propertyList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "propertyList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = property(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!property(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "propertyList", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, propertyList_recover_parser_);
    return r;
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
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_REAL_NUMBER, "<real number>");
    r = consumeToken(b, ObjJPlist_REAL_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, realNumber_1(b, l + 1));
    r = p && report_error_(b, consumeToken(b, ObjJPlist_DECIMAL_LITERAL)) && r;
    r = p && report_error_(b, realNumber_3(b, l + 1)) && r;
    r = p && consumeToken(b, ObjJPlist_REAL_CLOSE) && r;
    exit_section_(b, l, m, r, p, recover_realNumber_parser_);
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
  // !(
  // 		'<key>'	|'</key>'
  // 	|	'<data>'|'</data>'
  // 	|	'<dict>'|'</dict>'
  // 	|	'<array>'
  // 	|	'<real>'|'</real>'
  // 	|	'<integer>'|'</integer>'
  // 	|	'<string>'|'</string>'
  // 	|	'<true/>' | '<false/>'
  // 	|	'</plist>'
  // 	|	<<eof>>
  // 	)
  static boolean recover_array(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_array")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_array_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '<key>'	|'</key>'
  // 	|	'<data>'|'</data>'
  // 	|	'<dict>'|'</dict>'
  // 	|	'<array>'
  // 	|	'<real>'|'</real>'
  // 	|	'<integer>'|'</integer>'
  // 	|	'<string>'|'</string>'
  // 	|	'<true/>' | '<false/>'
  // 	|	'</plist>'
  // 	|	<<eof>>
  private static boolean recover_array_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_array_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_KEY_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_KEY_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_DATA_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_DATA_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_DICT_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_DICT_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_ARRAY_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_REAL_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_REAL_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_INTEGER_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_INTEGER_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_STRING_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_STRING_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_TRUE);
    if (!r) r = consumeToken(b, ObjJPlist_FALSE);
    if (!r) r = consumeToken(b, ObjJPlist_PLIST_CLOSE);
    if (!r) r = eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(
  // 		'<key>'	|'</key>'
  // 	|	'<data>'
  // 	|	'<dict>'|'</dict>'
  // 	|	'<array>'|'</array>'
  // 	|	'<real>'|'</real>'
  // 	|	'<integer>'|'</integer>'
  // 	|	'<string>'|'</string>'
  // 	|	'<true/>' | '<false/>'
  // 	|	'</plist>'
  // 	|	<<eof>>
  // 	)
  static boolean recover_dataValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_dataValue")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_dataValue_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '<key>'	|'</key>'
  // 	|	'<data>'
  // 	|	'<dict>'|'</dict>'
  // 	|	'<array>'|'</array>'
  // 	|	'<real>'|'</real>'
  // 	|	'<integer>'|'</integer>'
  // 	|	'<string>'|'</string>'
  // 	|	'<true/>' | '<false/>'
  // 	|	'</plist>'
  // 	|	<<eof>>
  private static boolean recover_dataValue_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_dataValue_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_KEY_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_KEY_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_DATA_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_DICT_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_DICT_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_ARRAY_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_ARRAY_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_REAL_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_REAL_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_INTEGER_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_INTEGER_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_STRING_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_STRING_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_TRUE);
    if (!r) r = consumeToken(b, ObjJPlist_FALSE);
    if (!r) r = consumeToken(b, ObjJPlist_PLIST_CLOSE);
    if (!r) r = eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(
  // 		'<key>'	|'</key>'
  // 	|	'<data>'|'</data>'
  // 	|	'<dict>'|'</dict>'
  // 	|	'<array>'|'</array>'
  // 	|	'<real>'|'</real>'
  // 	|	'<integer>'
  // 	|	'<string>'|'</string>'
  // 	|	'<true/>' | '<false/>'
  // 	|	'</plist>'
  // 	|	<<eof>>
  // 	)
  static boolean recover_integer(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_integer")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_integer_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '<key>'	|'</key>'
  // 	|	'<data>'|'</data>'
  // 	|	'<dict>'|'</dict>'
  // 	|	'<array>'|'</array>'
  // 	|	'<real>'|'</real>'
  // 	|	'<integer>'
  // 	|	'<string>'|'</string>'
  // 	|	'<true/>' | '<false/>'
  // 	|	'</plist>'
  // 	|	<<eof>>
  private static boolean recover_integer_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_integer_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_KEY_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_KEY_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_DATA_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_DATA_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_DICT_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_DICT_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_ARRAY_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_ARRAY_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_REAL_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_REAL_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_INTEGER_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_STRING_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_STRING_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_TRUE);
    if (!r) r = consumeToken(b, ObjJPlist_FALSE);
    if (!r) r = consumeToken(b, ObjJPlist_PLIST_CLOSE);
    if (!r) r = eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(
  // 		'<key>'|'</key>'
  // 	|	'<data>'|'</data>'
  // 	|	'<dict>'|'</dict>'
  // 	|	'<array>'|'</array>'
  // 	|	'<real>'|'</real>'
  // 	|	'<integer>'|'</integer>'
  // 	|	'<string>'|'</string>'
  // 	|	'<true/>' | '<false/>'
  // 	|	'</plist>'
  // 	)
  static boolean recover_keyName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_keyName")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_keyName_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '<key>'|'</key>'
  // 	|	'<data>'|'</data>'
  // 	|	'<dict>'|'</dict>'
  // 	|	'<array>'|'</array>'
  // 	|	'<real>'|'</real>'
  // 	|	'<integer>'|'</integer>'
  // 	|	'<string>'|'</string>'
  // 	|	'<true/>' | '<false/>'
  // 	|	'</plist>'
  private static boolean recover_keyName_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_keyName_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_KEY_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_KEY_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_DATA_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_DATA_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_DICT_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_DICT_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_ARRAY_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_ARRAY_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_REAL_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_REAL_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_INTEGER_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_INTEGER_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_STRING_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_STRING_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_TRUE);
    if (!r) r = consumeToken(b, ObjJPlist_FALSE);
    if (!r) r = consumeToken(b, ObjJPlist_PLIST_CLOSE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(
  // 		'<key>'	|'</key>'
  // 	|	'<data>'|'</data>'
  // 	|	'<dict>'|'</dict>'
  // 	|	'<array>'|'</array>'
  // 	|	'<real>'
  // 	|	'<integer>'|'</integer>'
  // 	|	'<string>'|'</string>'
  // 	|	'<true/>' | '<false/>'
  // 	|	'</plist>'
  // 	|	<<eof>>
  // 	)
  static boolean recover_realNumber(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_realNumber")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_realNumber_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '<key>'	|'</key>'
  // 	|	'<data>'|'</data>'
  // 	|	'<dict>'|'</dict>'
  // 	|	'<array>'|'</array>'
  // 	|	'<real>'
  // 	|	'<integer>'|'</integer>'
  // 	|	'<string>'|'</string>'
  // 	|	'<true/>' | '<false/>'
  // 	|	'</plist>'
  // 	|	<<eof>>
  private static boolean recover_realNumber_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_realNumber_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_KEY_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_KEY_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_DATA_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_DATA_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_DICT_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_DICT_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_ARRAY_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_ARRAY_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_REAL_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_INTEGER_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_INTEGER_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_STRING_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_STRING_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_TRUE);
    if (!r) r = consumeToken(b, ObjJPlist_FALSE);
    if (!r) r = consumeToken(b, ObjJPlist_PLIST_CLOSE);
    if (!r) r = eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(
  // 		'<key>'	|'</key>'
  // 	|	'<data>'|'</data>'
  // 	|	'<dict>'|'</dict>'
  // 	|	'<array>'|'</array>'
  // 	|	'<real>'|'</real>'
  // 	|	'<integer>'|'</integer>'
  // 	|	'<string>'
  // 	|	'<true/>' | '<false/>'
  // 	|	'</plist>'
  // 	|	<<eof>>
  // 	)
  static boolean recover_string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_string")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !recover_string_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '<key>'	|'</key>'
  // 	|	'<data>'|'</data>'
  // 	|	'<dict>'|'</dict>'
  // 	|	'<array>'|'</array>'
  // 	|	'<real>'|'</real>'
  // 	|	'<integer>'|'</integer>'
  // 	|	'<string>'
  // 	|	'<true/>' | '<false/>'
  // 	|	'</plist>'
  // 	|	<<eof>>
  private static boolean recover_string_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "recover_string_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ObjJPlist_KEY_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_KEY_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_DATA_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_DATA_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_DICT_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_DICT_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_ARRAY_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_ARRAY_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_REAL_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_REAL_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_INTEGER_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_INTEGER_CLOSE);
    if (!r) r = consumeToken(b, ObjJPlist_STRING_OPEN);
    if (!r) r = consumeToken(b, ObjJPlist_TRUE);
    if (!r) r = consumeToken(b, ObjJPlist_FALSE);
    if (!r) r = consumeToken(b, ObjJPlist_PLIST_CLOSE);
    if (!r) r = eof(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '<string>' COMMENT* stringValue COMMENT* '</string>'
  public static boolean string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_STRING, "<string>");
    r = consumeToken(b, ObjJPlist_STRING_OPEN);
    p = r; // pin = 1
    r = r && report_error_(b, string_1(b, l + 1));
    r = p && report_error_(b, stringValue(b, l + 1)) && r;
    r = p && report_error_(b, string_3(b, l + 1)) && r;
    r = p && consumeToken(b, ObjJPlist_STRING_CLOSE) && r;
    exit_section_(b, l, m, r, p, recover_string_parser_);
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
  static boolean xmlTagPropertiesList(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "xmlTagPropertiesList")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = xmlTagProperty(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!xmlTagProperty(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "xmlTagPropertiesList", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, xmlTagPropertiesList_recover_parser_);
    return r;
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
    if (!nextTokenIs(b, "<tag property>", ObjJPlist_VERSION, ObjJPlist_XML_TAG_PROPERTY_KEY)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ObjJPlist_XML_TAG_PROPERTY, "<tag property>");
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

  final static Parser arrayValueList_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return arrayValueList_recover(b, l + 1);
    }
  };
  final static Parser docTypeParamList_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return docTypeParamList_recover(b, l + 1);
    }
  };
  final static Parser propertyList_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return propertyList_recover(b, l + 1);
    }
  };
  final static Parser recover_array_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return recover_array(b, l + 1);
    }
  };
  final static Parser recover_dataValue_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return recover_dataValue(b, l + 1);
    }
  };
  final static Parser recover_integer_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return recover_integer(b, l + 1);
    }
  };
  final static Parser recover_keyName_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return recover_keyName(b, l + 1);
    }
  };
  final static Parser recover_realNumber_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return recover_realNumber(b, l + 1);
    }
  };
  final static Parser recover_string_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return recover_string(b, l + 1);
    }
  };
  final static Parser xmlTagPropertiesList_recover_parser_ = new Parser() {
    public boolean parse(PsiBuilder b, int l) {
      return xmlTagPropertiesList_recover(b, l + 1);
    }
  };
}
