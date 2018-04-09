package org.cappuccino_project.ide.intellij.plugin.contributor;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes.*;

public class ObjJBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = new BracePair[]{
            new BracePair(ObjJ_OPEN_PAREN, ObjJ_CLOSE_PAREN, false),
            new BracePair(ObjJ_OPEN_BRACKET, ObjJ_CLOSE_BRACKET, false),
            new BracePair(ObjJ_OPEN_BRACE, ObjJ_CLOSE_BRACE, false),
            new BracePair(ObjJ_AT_OPENBRACKET, ObjJ_CLOSE_BRACKET, false),
            new BracePair(ObjJ_AT_OPEN_BRACE, ObjJ_CLOSE_BRACE, false)
    };

    private static final IElementType[] NOT_BEFORE = new IElementType[] {ObjJ_RETURN, ObjJ_CONTINUE, ObjJ_BREAK, ObjJ_VAR, ObjJ_FOR, ObjJ_WHILE, ObjJ_DO, ObjJ_IF, ObjJ_OPEN_BRACE, ObjJ_OPEN_BRACKET, ObjJ_OPEN_PAREN, ObjJ_AT_OPEN_BRACE, ObjJ_AT_OPENBRACKET};
    private static final IElementType[] NO_BRACE_BEFORE = new IElementType[] {ObjJ_VAR};


    @NotNull
    @Override
    public BracePair[] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull
                                                           IElementType lbraceType, @Nullable IElementType contextType) {
        if (lbraceType == ObjJTypes.ObjJ_OPEN_BRACKET) {
            return isPairedBracketAllowedBeforeType(contextType);
        }
        return isAllowed(NOT_BEFORE, contextType);
    }

    private boolean isPairedBracketAllowedBeforeType(IElementType contextType) {
        return isAllowed(NOT_BEFORE, contextType) && isAllowed(NO_BRACE_BEFORE, contextType);
    }

    private boolean isAllowed(IElementType[] notBeforeElements, IElementType contextType) {
        for (IElementType notBefore : notBeforeElements) {
            if (contextType == notBefore) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
