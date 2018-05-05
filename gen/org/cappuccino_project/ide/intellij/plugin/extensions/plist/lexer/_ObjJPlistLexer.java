/* The following code was generated by JFlex 1.7.0 tweaked for IntelliJ platform */

package org.cappuccino_project.ide.intellij.plugin.extensions.plist.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi.types.ObjJPlistTypes.*;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.7.0
 * from the specification file <tt>_ObjJPlistLexer.flex</tt>
 */
public class _ObjJPlistLexer implements FlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int IN_TAG = 2;
  public static final int IN_STRING = 4;
  public static final int IN_DATA = 6;
  public static final int COMMENT = 8;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1,  1,  2,  2,  3,  3,  4, 4
  };

  /** 
   * Translates characters to character classes
   * Chosen bits are [7, 7, 7]
   * Total runtime size is 1928 bytes
   */
  public static int ZZ_CMAP(int ch) {
    return ZZ_CMAP_A[(ZZ_CMAP_Y[ZZ_CMAP_Z[ch>>14]|((ch>>7)&0x7f)]<<7)|(ch&0x7f)];
  }

  /* The ZZ_CMAP_Z table has 68 entries */
  static final char ZZ_CMAP_Z[] = zzUnpackCMap(
    "\1\0\103\200");

  /* The ZZ_CMAP_Y table has 256 entries */
  static final char ZZ_CMAP_Y[] = zzUnpackCMap(
    "\1\0\1\1\53\2\1\3\22\2\1\4\37\2\1\3\237\2");

  /* The ZZ_CMAP_A table has 640 entries */
  static final char ZZ_CMAP_A[] = zzUnpackCMap(
    "\11\0\1\40\1\2\2\17\1\2\22\0\1\40\1\16\1\6\4\0\1\1\3\0\1\12\1\0\1\14\1\10"+
    "\1\21\12\7\2\0\1\15\1\34\1\30\1\33\1\5\2\13\1\51\1\47\1\11\11\13\1\50\1\54"+
    "\3\13\1\52\4\13\1\53\1\13\1\0\1\3\2\0\1\13\1\0\1\32\1\4\1\46\1\31\1\41\1\43"+
    "\1\27\1\13\1\25\1\13\1\44\1\37\1\36\1\26\1\57\1\55\1\13\1\24\1\22\1\23\1\42"+
    "\1\56\1\13\1\35\1\45\1\13\12\0\1\17\32\0\1\40\337\0\1\40\177\0\13\40\35\0"+
    "\2\20\5\0\1\40\57\0\1\40\40\0");

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\5\0\2\1\1\2\1\3\1\4\1\1\1\5\1\1"+
    "\1\6\1\1\2\3\1\1\1\7\2\1\1\10\1\7"+
    "\2\1\1\2\2\1\1\2\4\0\1\11\1\0\1\12"+
    "\6\0\1\13\3\0\1\14\1\15\2\3\1\0\1\16"+
    "\3\0\1\17\1\7\12\0\1\2\24\0\1\20\1\3"+
    "\1\20\7\0\1\21\1\11\1\0\1\22\23\0\1\3"+
    "\1\0\1\22\2\0\1\22\1\0\1\23\22\0\1\24"+
    "\1\0\1\3\14\0\1\25\3\0\1\26\1\0\1\27"+
    "\1\30\1\31\2\0\1\32\1\3\6\0\1\33\1\0"+
    "\1\34\1\35\1\36\3\0\1\37\1\0\1\40\1\0"+
    "\1\41\3\0\1\42\3\0\1\43\1\44\1\45\1\0"+
    "\1\46\2\0\1\47\1\50\1\51\1\0\1\52\1\0"+
    "\1\53\1\54\1\55";

  private static int [] zzUnpackAction() {
    int [] result = new int[217];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\60\0\140\0\220\0\300\0\360\0\u0120\0\u0150"+
    "\0\u0180\0\u01b0\0\u01e0\0\u0210\0\u0240\0\360\0\u0270\0\u02a0"+
    "\0\u02d0\0\u0300\0\u0330\0\u0360\0\u0390\0\360\0\u03c0\0\u03f0"+
    "\0\u0420\0\u0450\0\u0480\0\u04b0\0\u04e0\0\u0510\0\u0540\0\u0570"+
    "\0\u01e0\0\u05a0\0\u05d0\0\u0600\0\u0630\0\u0660\0\u0690\0\u06c0"+
    "\0\u06f0\0\u0720\0\360\0\u0750\0\u0780\0\u07b0\0\360\0\360"+
    "\0\u07e0\0\u0810\0\u0300\0\360\0\u0840\0\u0390\0\u0870\0\360"+
    "\0\u08a0\0\u03f0\0\u08d0\0\u0900\0\u0930\0\u0480\0\u0960\0\u0990"+
    "\0\u09c0\0\u09f0\0\u0a20\0\360\0\u0a50\0\u0a80\0\u0ab0\0\u0ae0"+
    "\0\u0b10\0\u0b40\0\u0b70\0\u0ba0\0\u0bd0\0\u0c00\0\u0c30\0\u0c60"+
    "\0\u0c90\0\u0cc0\0\u0cf0\0\u0d20\0\u0d50\0\u0d80\0\u0db0\0\u0de0"+
    "\0\u0180\0\u0e10\0\u0330\0\u0e40\0\u0e70\0\u0ea0\0\u0ed0\0\u0f00"+
    "\0\u0f30\0\u0f60\0\u0510\0\u0f90\0\u0f90\0\360\0\u0fc0\0\u0ff0"+
    "\0\u1020\0\u1050\0\u1080\0\u10b0\0\u10e0\0\u1110\0\u1140\0\u1170"+
    "\0\u11a0\0\u11d0\0\u1200\0\u1230\0\u1260\0\u1290\0\u12c0\0\u12f0"+
    "\0\u1320\0\u1350\0\u1380\0\u03f0\0\u13b0\0\u13e0\0\u0480\0\u1410"+
    "\0\u0510\0\u1440\0\u1470\0\u14a0\0\u14d0\0\u1500\0\u1530\0\u1560"+
    "\0\u1590\0\u15c0\0\u15f0\0\u1620\0\u1650\0\u1680\0\u16b0\0\u16e0"+
    "\0\u1710\0\u1740\0\u1770\0\360\0\u17a0\0\u17d0\0\u1800\0\u1830"+
    "\0\u1860\0\u1890\0\u18c0\0\u18f0\0\u1920\0\u1950\0\u1980\0\u19b0"+
    "\0\u19e0\0\u1a10\0\360\0\u1a40\0\u1a70\0\u1aa0\0\360\0\u1ad0"+
    "\0\360\0\360\0\360\0\u1b00\0\u1b30\0\360\0\u1b60\0\u1b90"+
    "\0\u1bc0\0\u1bf0\0\u1c20\0\u1c50\0\u1c80\0\360\0\u1cb0\0\360"+
    "\0\360\0\360\0\u1ce0\0\u1d10\0\u1d40\0\360\0\u1d70\0\360"+
    "\0\u1da0\0\u0180\0\u1dd0\0\u1e00\0\u1e30\0\u0480\0\u1e60\0\u1e90"+
    "\0\u1ec0\0\360\0\360\0\360\0\u1ef0\0\360\0\u1f20\0\u1f50"+
    "\0\u0480\0\360\0\360\0\u1f80\0\360\0\u1fb0\0\u03f0\0\360"+
    "\0\u03f0";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[217];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\6\1\7\1\10\1\6\1\11\2\6\1\12\1\13"+
    "\1\11\1\6\1\11\1\6\1\14\1\6\2\10\1\15"+
    "\6\11\1\16\2\11\1\17\1\6\1\20\2\11\1\10"+
    "\15\11\1\21\1\11\1\6\1\22\1\10\1\6\1\23"+
    "\1\24\1\25\2\6\1\23\1\6\2\23\2\6\2\10"+
    "\1\6\6\23\1\16\2\23\1\17\1\26\1\27\2\23"+
    "\1\10\17\23\2\30\1\10\12\30\1\31\1\30\2\10"+
    "\17\30\1\32\17\30\2\33\1\10\12\33\1\34\1\33"+
    "\2\10\17\33\1\35\17\33\14\36\1\37\43\36\62\0"+
    "\1\40\15\0\1\40\41\0\1\10\14\0\2\10\17\0"+
    "\1\10\23\0\1\11\2\0\1\11\1\0\1\11\1\0"+
    "\1\11\6\0\6\11\1\0\2\11\2\0\3\11\1\0"+
    "\17\11\7\0\1\12\1\41\56\0\1\42\66\0\1\43"+
    "\2\0\1\44\1\45\1\46\1\47\1\50\3\0\1\51"+
    "\1\52\1\53\7\0\1\54\1\55\10\0\1\56\32\0"+
    "\1\57\57\0\1\60\33\0\1\11\2\0\1\11\1\0"+
    "\1\11\1\0\1\11\6\0\6\11\1\0\2\11\2\0"+
    "\1\11\1\61\1\11\1\0\17\11\4\0\1\11\2\0"+
    "\1\11\1\0\1\11\1\0\1\11\6\0\6\11\1\0"+
    "\2\11\2\0\3\11\1\0\1\62\16\11\1\63\1\64"+
    "\1\0\1\65\54\63\4\0\1\23\2\0\1\23\1\0"+
    "\1\23\1\0\2\23\5\0\6\23\1\0\2\23\2\0"+
    "\3\23\1\0\17\23\6\0\1\66\51\0\2\66\1\0"+
    "\1\67\2\66\1\70\51\66\4\0\1\23\2\0\1\23"+
    "\1\0\1\23\1\0\2\23\5\0\6\23\1\0\2\23"+
    "\2\0\1\23\1\71\1\23\1\0\17\23\2\72\1\0"+
    "\12\72\1\73\1\72\2\0\41\72\1\0\12\72\1\73"+
    "\1\74\2\0\1\75\40\72\1\10\12\72\1\73\1\72"+
    "\2\10\17\72\1\32\17\72\2\76\1\0\12\76\1\77"+
    "\1\76\2\0\41\76\1\0\12\76\1\77\1\100\2\0"+
    "\1\101\40\76\1\10\12\76\1\77\1\76\2\10\17\76"+
    "\1\35\17\76\14\36\1\102\57\36\1\103\43\36\1\0"+
    "\1\104\65\0\1\42\1\0\1\105\27\0\1\105\32\0"+
    "\1\106\32\0\1\107\32\0\1\110\1\0\1\111\1\112"+
    "\3\0\1\113\1\114\11\0\1\115\10\0\1\116\25\0"+
    "\1\117\60\0\1\120\74\0\1\121\44\0\1\122\56\0"+
    "\1\123\4\0\1\124\51\0\1\125\65\0\1\126\66\0"+
    "\1\127\55\0\1\130\24\0\1\11\2\0\1\11\1\0"+
    "\1\11\1\0\1\11\6\0\6\11\1\0\2\11\2\0"+
    "\2\11\1\131\1\0\17\11\4\0\1\11\2\0\1\11"+
    "\1\0\1\11\1\0\1\11\6\0\2\11\1\132\3\11"+
    "\1\0\2\11\2\0\3\11\1\0\17\11\1\0\1\63"+
    "\1\0\2\63\16\0\2\63\1\0\1\63\14\0\1\63"+
    "\12\0\1\63\4\0\2\66\1\0\1\66\14\0\2\66"+
    "\1\0\1\66\14\0\1\66\12\0\1\66\5\0\1\23"+
    "\2\0\1\23\1\0\1\23\1\0\2\23\5\0\6\23"+
    "\1\0\2\23\2\0\2\23\1\133\1\0\17\23\2\72"+
    "\1\0\12\72\1\73\1\72\2\0\1\134\40\72\1\0"+
    "\11\72\1\135\1\73\1\72\2\0\41\72\1\0\12\72"+
    "\1\73\1\72\2\0\1\72\1\136\35\72\2\76\1\0"+
    "\12\76\1\77\1\76\2\0\1\137\40\76\1\0\11\76"+
    "\1\140\1\77\1\76\2\0\41\76\1\0\12\76\1\77"+
    "\1\76\2\0\10\76\1\141\26\76\14\36\1\142\57\36"+
    "\1\142\13\36\1\143\27\36\7\0\1\144\2\0\1\145"+
    "\1\0\1\145\57\0\1\146\113\0\1\147\32\0\1\150"+
    "\75\0\1\151\44\0\1\152\56\0\1\153\4\0\1\154"+
    "\51\0\1\155\74\0\1\156\55\0\1\157\44\0\1\160"+
    "\75\0\1\161\47\0\1\162\50\0\1\163\102\0\1\164"+
    "\34\0\1\165\60\0\1\166\72\0\1\167\65\0\1\170"+
    "\37\0\1\171\36\0\1\11\2\0\1\11\1\0\1\11"+
    "\1\0\1\11\6\0\1\172\5\11\1\0\2\11\2\0"+
    "\3\11\1\0\17\11\2\72\1\0\12\72\1\73\1\72"+
    "\2\0\1\72\1\173\37\72\1\0\11\72\1\174\1\73"+
    "\1\72\2\0\41\72\1\0\12\72\1\73\1\72\2\0"+
    "\2\72\1\175\34\72\2\76\1\0\12\76\1\77\1\76"+
    "\2\0\10\76\1\176\30\76\1\0\11\76\1\177\1\77"+
    "\1\76\2\0\41\76\1\0\12\76\1\77\1\76\2\0"+
    "\11\76\1\200\25\76\14\36\1\142\13\36\1\201\27\36"+
    "\7\0\1\144\121\0\1\202\32\0\1\203\65\0\1\204"+
    "\50\0\1\205\102\0\1\206\34\0\1\207\60\0\1\210"+
    "\100\0\1\211\37\0\1\212\57\0\1\213\73\0\1\214"+
    "\55\0\1\215\61\0\1\216\41\0\1\217\66\0\1\220"+
    "\6\0\1\221\50\0\1\222\47\0\1\223\65\0\1\224"+
    "\51\0\1\225\41\0\1\11\2\0\1\11\1\0\1\11"+
    "\1\0\1\11\6\0\3\11\1\226\2\11\1\0\2\11"+
    "\2\0\3\11\1\0\17\11\2\72\1\0\12\72\1\73"+
    "\1\72\2\0\2\72\1\227\36\72\1\0\12\72\1\73"+
    "\1\72\2\0\3\72\1\230\33\72\2\76\1\0\12\76"+
    "\1\77\1\76\2\0\11\76\1\231\27\76\1\0\12\76"+
    "\1\77\1\76\2\0\2\76\1\232\34\76\52\0\1\233"+
    "\32\0\1\234\71\0\1\235\61\0\1\236\41\0\1\237"+
    "\66\0\1\240\6\0\1\241\50\0\1\242\55\0\1\243"+
    "\51\0\1\244\63\0\1\245\52\0\1\246\66\0\1\247"+
    "\56\0\1\250\60\0\1\251\57\0\1\252\57\0\1\253"+
    "\74\0\1\254\53\0\1\255\41\0\1\256\40\0\1\11"+
    "\2\0\1\11\1\0\1\11\1\0\1\11\6\0\6\11"+
    "\1\0\2\11\2\0\3\11\1\0\16\11\1\257\2\72"+
    "\1\0\12\72\1\73\1\72\2\0\3\72\1\260\35\72"+
    "\1\0\12\72\1\73\1\72\2\0\4\72\1\261\32\72"+
    "\2\76\1\0\12\76\1\77\1\76\2\0\2\76\1\262"+
    "\36\76\1\0\12\76\1\77\1\76\2\0\11\76\1\263"+
    "\25\76\53\0\1\264\32\0\1\265\61\0\1\266\56\0"+
    "\1\267\60\0\1\270\57\0\1\271\57\0\1\272\74\0"+
    "\1\273\35\0\1\274\63\0\1\275\60\0\1\276\70\0"+
    "\1\277\46\0\1\300\50\0\1\301\42\0\1\11\2\0"+
    "\1\11\1\0\1\11\1\0\1\11\6\0\4\11\1\302"+
    "\1\11\1\0\2\11\2\0\3\11\1\0\17\11\2\72"+
    "\1\0\12\72\1\73\1\72\2\0\4\72\1\303\34\72"+
    "\1\0\12\72\1\73\1\72\2\0\5\72\1\304\31\72"+
    "\2\76\1\0\12\76\1\77\1\76\2\0\11\76\1\305"+
    "\27\76\1\0\12\76\1\77\1\76\2\0\7\76\1\306"+
    "\27\76\54\0\1\307\32\0\1\310\71\0\1\311\46\0"+
    "\1\312\57\0\1\313\57\0\1\314\53\0\1\315\63\0"+
    "\1\316\27\0\2\72\1\0\12\72\1\73\1\72\2\0"+
    "\5\72\1\317\33\72\1\0\12\72\1\73\1\72\2\0"+
    "\6\72\1\320\30\72\2\76\1\0\12\76\1\77\1\76"+
    "\2\0\7\76\1\321\27\76\11\0\1\322\76\0\1\323"+
    "\53\0\1\324\63\0\1\325\27\0\2\72\1\0\12\72"+
    "\1\73\1\72\2\0\6\72\1\326\32\72\1\0\12\72"+
    "\1\73\1\72\2\0\7\72\1\327\27\72\30\0\1\330"+
    "\27\0\2\72\1\0\12\72\1\73\1\72\2\0\7\72"+
    "\1\331\27\72";

  private static int [] zzUnpackTrans() {
    int [] result = new int[8160];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String[] ZZ_ERROR_MSG = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\5\0\1\11\7\1\1\11\7\1\1\11\7\1\4\0"+
    "\1\1\1\0\1\1\6\0\1\11\3\0\2\11\2\1"+
    "\1\0\1\11\3\0\1\11\1\1\12\0\1\11\24\0"+
    "\3\1\7\0\2\1\1\0\1\11\23\0\1\1\1\0"+
    "\1\1\2\0\1\1\1\0\1\1\22\0\1\11\1\0"+
    "\1\1\14\0\1\11\3\0\1\11\1\0\3\11\2\0"+
    "\1\11\1\1\6\0\1\11\1\0\3\11\3\0\1\11"+
    "\1\0\1\11\1\0\1\1\3\0\1\1\3\0\3\11"+
    "\1\0\1\11\2\0\1\1\2\11\1\0\1\11\1\0"+
    "\1\1\1\11\1\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[217];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private CharSequence zzBuffer = "";

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /**
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /* user code: */
	private int stateBeforeComment;
	private static final Logger LOGGER = Logger.getLogger("_PLIST_LEXER");
	public _ObjJPlistLexer() {
		this((java.io.Reader)null);
		log("Creating Plist lexer");
	}

	private static void log(String message) {
		LOGGER.log(Level.INFO, message);
	}



  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public _ObjJPlistLexer(java.io.Reader in) {
    this.zzReader = in;
  }


  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    int size = 0;
    for (int i = 0, length = packed.length(); i < length; i += 2) {
      size += packed.charAt(i);
    }
    char[] map = new char[size];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < packed.length()) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }

  public final int getTokenStart() {
    return zzStartRead;
  }

  public final int getTokenEnd() {
    return getTokenStart() + yylength();
  }

  public void reset(CharSequence buffer, int start, int end, int initialState) {
    zzBuffer = buffer;
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
  }

  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return zzBuffer.subSequence(zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position <tt>pos</tt> from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer.charAt(zzStartRead+pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public IElementType advance() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    CharSequence zzBufferL = zzBuffer;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL/*, zzEndReadL*/);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL/*, zzEndReadL*/);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + ZZ_CMAP(zzInput) ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
        return null;
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1: 
            { return BAD_CHARACTER;
            } 
            // fall through
          case 46: break;
          case 2: 
            { return WHITE_SPACE;
            } 
            // fall through
          case 47: break;
          case 3: 
            { return ObjJPlist_ID;
            } 
            // fall through
          case 48: break;
          case 4: 
            { return ObjJPlist_INTEGER_LITERAL;
            } 
            // fall through
          case 49: break;
          case 5: 
            { yybegin(IN_TAG); return ObjJPlist_LT;
            } 
            // fall through
          case 50: break;
          case 6: 
            { yybegin(YYINITIAL); return ObjJPlist_GT;
            } 
            // fall through
          case 51: break;
          case 7: 
            { return ObjJPlist_XML_TAG_PROPERTY_KEY;
            } 
            // fall through
          case 52: break;
          case 8: 
            { return ObjJPlist_EQUALS;
            } 
            // fall through
          case 53: break;
          case 9: 
            { return ObjJPlist_DECIMAL_LITERAL;
            } 
            // fall through
          case 54: break;
          case 10: 
            { return ObjJPlist_LT_SLASH;
            } 
            // fall through
          case 55: break;
          case 11: 
            { yybegin(IN_TAG); return ObjJPlist_OPENING_HEADER_BRACKET;
            } 
            // fall through
          case 56: break;
          case 12: 
            { yybegin(YYINITIAL); return ObjJPlist_SLASH_GT;
            } 
            // fall through
          case 57: break;
          case 13: 
            { yybegin(YYINITIAL); return ObjJPlist_CLOSING_HEADER_BRACKET;
            } 
            // fall through
          case 58: break;
          case 14: 
            { return ObjJPlist_SINGLE_QUOTE_STRING_LITERAL;
            } 
            // fall through
          case 59: break;
          case 15: 
            { return ObjJPlist_DOUBLE_QUOTE_STRING_LITERAL;
            } 
            // fall through
          case 60: break;
          case 16: 
            { return ObjJPlist_XML;
            } 
            // fall through
          case 61: break;
          case 17: 
            { yybegin(stateBeforeComment >= 0 ? stateBeforeComment : YYINITIAL); stateBeforeComment = -1; return ObjJPlist_COMMENT;
            } 
            // fall through
          case 62: break;
          case 18: 
            { stateBeforeComment = yystate(); yybegin(COMMENT);
            } 
            // fall through
          case 63: break;
          case 19: 
            // lookahead expression with fixed lookahead length
            zzMarkedPos = Character.offsetByCodePoints
                (zzBufferL/*, zzStartRead, zzEndRead - zzStartRead*/, zzMarkedPos, -3);
            { 
            } 
            // fall through
          case 64: break;
          case 20: 
            { return ObjJPlist_KEY_OPEN;
            } 
            // fall through
          case 65: break;
          case 21: 
            { return ObjJPlist_KEY_CLOSE;
            } 
            // fall through
          case 66: break;
          case 22: 
            { return ObjJPlist_REAL_OPEN;
            } 
            // fall through
          case 67: break;
          case 23: 
            { return ObjJPlist_DICT_OPEN;
            } 
            // fall through
          case 68: break;
          case 24: 
            { return ObjJPlist_DATA_OPEN;
            } 
            // fall through
          case 69: break;
          case 25: 
            { return ObjJPlist_DATE_OPEN;
            } 
            // fall through
          case 70: break;
          case 26: 
            { yybegin(IN_TAG); return ObjJPlist_PLIST_OPEN;
            } 
            // fall through
          case 71: break;
          case 27: 
            { return ObjJPlist_REAL_CLOSE;
            } 
            // fall through
          case 72: break;
          case 28: 
            { return ObjJPlist_DICT_CLOSE;
            } 
            // fall through
          case 73: break;
          case 29: 
            { return ObjJPlist_DATA_CLOSE;
            } 
            // fall through
          case 74: break;
          case 30: 
            { return ObjJPlist_DATE_CLOSE;
            } 
            // fall through
          case 75: break;
          case 31: 
            { return ObjJPlist_TRUE;
            } 
            // fall through
          case 76: break;
          case 32: 
            { return ObjJPlist_ARRAY_OPEN;
            } 
            // fall through
          case 77: break;
          case 33: 
            { return ObjJPlist_VERSION;
            } 
            // fall through
          case 78: break;
          case 34: 
            { yybegin(YYINITIAL); return ObjJPlist_DATA_CLOSE;
            } 
            // fall through
          case 79: break;
          case 35: 
            { return ObjJPlist_ARRAY_CLOSE;
            } 
            // fall through
          case 80: break;
          case 36: 
            { return ObjJPlist_PLIST_CLOSE;
            } 
            // fall through
          case 81: break;
          case 37: 
            { yybegin(IN_STRING); return ObjJPlist_STRING_OPEN;
            } 
            // fall through
          case 82: break;
          case 38: 
            { return ObjJPlist_FALSE;
            } 
            // fall through
          case 83: break;
          case 39: 
            // lookahead expression with fixed lookahead length
            zzMarkedPos = Character.offsetByCodePoints
                (zzBufferL/*, zzStartRead, zzEndRead - zzStartRead*/, zzMarkedPos, -7);
            { return ObjJPlist_DATA_LITERAL;
            } 
            // fall through
          case 84: break;
          case 40: 
            { yybegin(IN_TAG); return ObjJPlist_DOCTYPE_OPEN;
            } 
            // fall through
          case 85: break;
          case 41: 
            { return ObjJPlist_STRING_CLOSE;
            } 
            // fall through
          case 86: break;
          case 42: 
            { return ObjJPlist_INTEGER_OPEN;
            } 
            // fall through
          case 87: break;
          case 43: 
            { yybegin(YYINITIAL); return ObjJPlist_STRING_CLOSE;
            } 
            // fall through
          case 88: break;
          case 44: 
            { return ObjJPlist_INTEGER_CLOSE;
            } 
            // fall through
          case 89: break;
          case 45: 
            // lookahead expression with fixed lookahead length
            zzMarkedPos = Character.offsetByCodePoints
                (zzBufferL/*, zzStartRead, zzEndRead - zzStartRead*/, zzMarkedPos, -9);
            { return ObjJPlist_STRING_TAG_LITERAL;
            } 
            // fall through
          case 90: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
