package org.cappuccino_project.ide.intellij.plugin.extensions.plist;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi.types.ObjJPlistTypes.*;

public class ObjJPlistBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = new BracePair[]{
            new BracePair(ObjJPlist_ARRAY_OPEN, ObjJPlist_ARRAY_CLOSE, false),
            new BracePair(ObjJPlist_ARRAY_OPEN, ObjJPlist_PLIST_CLOSE, false),
            new BracePair(ObjJPlist_DICT_OPEN, ObjJPlist_DICT_CLOSE, false),
            new BracePair(ObjJPlist_REAL_OPEN, ObjJPlist_REAL_CLOSE, false),
            new BracePair(ObjJPlist_INTEGER_OPEN, ObjJPlist_INTEGER_CLOSE, false),
            new BracePair(ObjJPlist_STRING_OPEN, ObjJPlist_STRING_CLOSE, false),
            new BracePair(ObjJPlist_DATA_OPEN, ObjJPlist_DATA_CLOSE, false),
            new BracePair(ObjJPlist_DATE_OPEN, ObjJPlist_DATE_CLOSE, false),
            new BracePair(ObjJPlist_KEY_OPEN, ObjJPlist_KEY_CLOSE, false)
    };

    @NotNull
    @Override
    public BracePair[] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull
                                                           IElementType lbraceType, @Nullable
                                                           IElementType contextType) {
        return (lbraceType == ObjJPlist_ARRAY_OPEN && contextType != ObjJPlist_ARRAY_CLOSE) ||
                (lbraceType == ObjJPlist_DATE_OPEN && contextType != ObjJPlist_DATE_CLOSE) ||
                (lbraceType == ObjJPlist_DICT_OPEN && contextType != ObjJPlist_DICT_CLOSE) ||
                (lbraceType == ObjJPlist_DATA_OPEN && contextType != ObjJPlist_DATA_CLOSE) ||
                (lbraceType == ObjJPlist_INTEGER_OPEN && contextType != ObjJPlist_INTEGER_CLOSE) ||
                (lbraceType == ObjJPlist_REAL_OPEN && contextType != ObjJPlist_REAL_CLOSE) ||
                (lbraceType == ObjJPlist_PLIST_OPEN && contextType != ObjJPlist_PLIST_CLOSE);
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
