package org.cappuccino_project.ide.intellij.plugin.extensions.plist

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

import org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi.types.ObjJPlistTypes.*

class ObjJPlistBraceMatcher : PairedBraceMatcher {

    override fun getPairs(): Array<BracePair> {
        return PAIRS
    }

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
        return lbraceType === ObjJPlist_ARRAY_OPEN && contextType !== ObjJPlist_ARRAY_CLOSE ||
                lbraceType === ObjJPlist_DATE_OPEN && contextType !== ObjJPlist_DATE_CLOSE ||
                lbraceType === ObjJPlist_DICT_OPEN && contextType !== ObjJPlist_DICT_CLOSE ||
                lbraceType === ObjJPlist_DATA_OPEN && contextType !== ObjJPlist_DATA_CLOSE ||
                lbraceType === ObjJPlist_INTEGER_OPEN && contextType !== ObjJPlist_INTEGER_CLOSE ||
                lbraceType === ObjJPlist_REAL_OPEN && contextType !== ObjJPlist_REAL_CLOSE ||
                lbraceType === ObjJPlist_PLIST_OPEN && contextType !== ObjJPlist_PLIST_CLOSE
    }

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }

    companion object {
        private val PAIRS = arrayOf<BracePair>(BracePair(ObjJPlist_ARRAY_OPEN, ObjJPlist_ARRAY_CLOSE, false), BracePair(ObjJPlist_ARRAY_OPEN, ObjJPlist_PLIST_CLOSE, false), BracePair(ObjJPlist_DICT_OPEN, ObjJPlist_DICT_CLOSE, false), BracePair(ObjJPlist_REAL_OPEN, ObjJPlist_REAL_CLOSE, false), BracePair(ObjJPlist_INTEGER_OPEN, ObjJPlist_INTEGER_CLOSE, false), BracePair(ObjJPlist_STRING_OPEN, ObjJPlist_STRING_CLOSE, false), BracePair(ObjJPlist_DATA_OPEN, ObjJPlist_DATA_CLOSE, false), BracePair(ObjJPlist_DATE_OPEN, ObjJPlist_DATE_CLOSE, false), BracePair(ObjJPlist_KEY_OPEN, ObjJPlist_KEY_CLOSE, false))
    }
}
