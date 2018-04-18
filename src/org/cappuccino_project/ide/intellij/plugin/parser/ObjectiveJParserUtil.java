package org.cappuccino_project.ide.intellij.plugin.parser;

import com.intellij.lang.LighterASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.util.Key;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import gnu.trove.TObjectLongHashMap;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;

import java.util.Objects;

public class ObjectiveJParserUtil extends GeneratedParserUtilBase {

    private static boolean doAllowSemiColonAfterMethodHeader = false;
    private static final Key<TObjectLongHashMap<String>> MODES_KEY = Key.create("MODES_KEY");

    private static TObjectLongHashMap<String> getParsingModes(PsiBuilder builder_) {
        TObjectLongHashMap<String> flags = builder_.getUserDataUnprotected(MODES_KEY);
        if (flags == null) builder_.putUserDataUnprotected(MODES_KEY, flags = new TObjectLongHashMap<>());
        return flags;
    }

    public static boolean inMode(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level, String mode) {
        //LOGGER.log(Level.INFO, String.format("In Mode: <%s>: <%b>", mode, getParsingModes(builder_).get(mode) > 0));
        return getParsingModes(builder_).get(mode) > 0;
    }


    @SuppressWarnings("unused")
    public static boolean notInMode(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level, String mode) {
        return getParsingModes(builder_).get(mode) < 1;
    }

    public static boolean enterMode(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level, String mode) {
        TObjectLongHashMap<String> flags = getParsingModes(builder_);
        if (!flags.increment(mode)) flags.put(mode, 1);
        return true;
    }

    public static boolean exitMode(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level, String mode) {
        TObjectLongHashMap<String> flags = getParsingModes(builder_);
        long count = flags.get(mode);
        if (count == 1) flags.remove(mode);
        else if (count > 1) flags.put(mode, count -1);
        else builder_.error("Could not exit inactive '" + mode + "' mode at offset " + builder_.getCurrentOffset());
        return true;
    }

    @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
    public static PsiBuilder adapt_builder_(IElementType root, PsiBuilder builder, PsiParser parser, TokenSet[] tokenSets) {
        PsiBuilder result = GeneratedParserUtilBase.adapt_builder_(root, builder, parser, tokenSets);
        ErrorState.get(result).altMode = true;
        return result;
    }

    public static boolean doAllowSemiColonAfterMethodHeader(@SuppressWarnings("UnusedParameters")PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level) {
        return doAllowSemiColonAfterMethodHeader;
    }

    public static boolean doAllowSemiColonAfterMethodHeader(@SuppressWarnings("UnusedParameters") PsiBuilder builder_, @SuppressWarnings("UnusedParameters")int level, String doAllow) {
        doAllowSemiColonAfterMethodHeader = doAllow.equalsIgnoreCase("true");
        return true;
    }

    public static boolean regexPossible(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level_) {

        if (builder_.eof()) {
            return false;
        }
        final LighterASTNode lastNode = builder_.getLatestDoneMarker();
        IElementType lastTokenType = lastNode != null ? lastNode.getTokenType() : null;
        return lastTokenType == null || !lastTokenType.equals(ObjJTypes.ObjJ_ID) && !lastTokenType.equals(ObjJTypes.ObjJ_NULL_LITERAL) && !lastTokenType.equals(ObjJTypes.ObjJ_BOOLEAN_LITERAL) && !lastTokenType.equals(ObjJTypes.ObjJ_THIS) && !lastTokenType.equals(ObjJTypes.ObjJ_CLOSE_BRACKET) && !lastTokenType.equals(ObjJTypes.ObjJ_CLOSE_PAREN) && !lastTokenType.equals(ObjJTypes.ObjJ_OCTAL_INTEGER_LITERAL) && !lastTokenType.equals(ObjJTypes.ObjJ_DECIMAL_LITERAL) && !lastTokenType.equals(ObjJTypes.ObjJ_HEX_INTEGER_LITERAL) && !lastTokenType.equals(ObjJTypes.ObjJ_PLUS_PLUS) && !lastTokenType.equals(ObjJTypes.ObjJ_MINUS_MINUS);

    }

    public static boolean notLineTerminator(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level) {
        return !lineTerminatorAhead(builder_, level);
    }

    /**
     * Returns {@code true} iff on the current index of the parser's
     * token stream a token exists on the {@code HIDDEN} channel which
     * either is a line terminator, or is a multi line comment that
     * contains a line terminator.
     *
     * @return {@code true} iff on the current index of the parser's
     * token stream a token exists on the {@code HIDDEN} channel which
     * either is a line terminator, or is a multi line comment that
     * contains a line terminator.
     */
    public static boolean lineTerminatorAhead(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level_) {
        // Get the token ahead of the current index.

        int i = 0;
        IElementType ahead = builder_.lookAhead(i);
        boolean hadLineTerminator = false;
        while (ahead == com.intellij.psi.TokenType.WHITE_SPACE || ahead == ObjJTypes.ObjJ_LINE_TERMINATOR) {
            if (ahead == ObjJTypes.ObjJ_LINE_TERMINATOR) {
                hadLineTerminator = true;
            }
            ahead = builder_.lookAhead(++i);
        }
        return eof(builder_, level_) || ObjJPsiImplUtil.eosToken(ahead, hadLineTerminator);

    }

    public static boolean closeBrace(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level) {
        final IElementType next = builder_.lookAhead(1);
        return next == null || next.equals(ObjJTypes.ObjJ_CLOSE_BRACE);
    }

    public static boolean notOpenBraceAndNotFunction(PsiBuilder builder_, @SuppressWarnings("UnusedParameters") int level) {
        IElementType nextTokenType = builder_.lookAhead(1);
        return !Objects.equals(nextTokenType,ObjJTypes.ObjJ_OPEN_BRACE) && !Objects.equals(nextTokenType,ObjJTypes.ObjJ_FUNCTION);
    }
}