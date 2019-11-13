package cappuccino.ide.intellij.plugin.comments.psi.impl

import cappuccino.ide.intellij.plugin.comments.psi.api.ObjJCommentDocComment
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.lang.Language
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement


class ObjJCommentDocCommentImpl(buffer:CharSequence?) : LazyParseablePsiElement(ObjJDocTokens.OBJJ_DOC_COMMENT, buffer), ObjJCommentDocComment  {
    override fun getTokenType() = ObjJDocTokens.OBJJ_DOC_COMMENT
    override fun getLanguage(): Language = ObjJLanguage.instance
    override fun toString(): String = node.elementType.toString()

}