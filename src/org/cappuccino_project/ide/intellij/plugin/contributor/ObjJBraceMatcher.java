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
            new BracePair(ObjJ_AT_OPEN_BRACE, ObjJ_CLOSE_BRACE, false),
            new BracePair(ObjJ_DOUBLE_QUO, ObjJ_DOUBLE_QUO, false),
            new BracePair(ObjJ_SINGLE_QUO, ObjJ_SINGLE_QUO, false),
            new BracePair(ObjJ_BLOCK_COMMENT_START, ObjJ_BLOCK_COMMENT_END, false)
    };

    private static final IElementType[] NOT_IN_CONTEXT = new IElementType[] {};
    private static final IElementType[] BRACE_NOT_IN_CONTEXT = new IElementType[] {};


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
        return isAllowed(NOT_IN_CONTEXT, contextType);
    }

    private boolean isPairedBracketAllowedBeforeType(IElementType contextType) {
        return isAllowed(NOT_IN_CONTEXT, contextType) && isAllowed(BRACE_NOT_IN_CONTEXT, contextType);
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
