package cappuccino.ide.intellij.plugin.comments.psi.impl

import cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocCommentParsableBlockToken.OBJJ_DOC_COMMENT_PARSABLE_BLOCK
import cappuccino.ide.intellij.plugin.comments.psi.api.ObjJDocCommentComment
import cappuccino.ide.intellij.plugin.comments.psi.api.ObjJDocCommentTagLineBase
import cappuccino.ide.intellij.plugin.comments.psi.stubs.ObjJDocCommentTagLineStruct
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.utils.tokenType
import com.intellij.lang.Language
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil

interface ObjJDocCommentParsableBlock : ObjJCompositeElement, PsiComment {
    val parametersAsStructs: List<ObjJDocCommentTagLineStruct>
    val returnType: InferenceResult?
    val parameterTags: List<ObjJDocCommentTagLineBase>
    val textLines:List<String>
    val comment: ObjJDocCommentComment?
    val tagLinesAsStructs: List<ObjJDocCommentTagLineStruct>
}


class ObjJDocCommentParsableBlockImpl(buffer: CharSequence?) : LazyParseablePsiElement(OBJJ_DOC_COMMENT_PARSABLE_BLOCK, buffer), ObjJDocCommentParsableBlock {

    override fun getTokenType(): IElementType = elementType

    override val comment: ObjJDocCommentComment? get() = getChildOfType(ObjJDocCommentComment::class.java)

    override val parameterTags: List<ObjJDocCommentTagLineBase>
        get() = comment?.parameterTags.orEmpty()

    override val returnType: InferenceResult?
        get() = comment?.returnType

    override val parametersAsStructs: List<ObjJDocCommentTagLineStruct>
        get() = comment?.parametersAsStructs.orEmpty()

    override val textLines: List<String>
        get() = comment?.textLinesAsStrings.orEmpty()

    override val tagLinesAsStructs: List<ObjJDocCommentTagLineStruct>
        get() = comment?.tagLinesAsStructs.orEmpty()

    override fun <T:PsiElement> getParentOfType(parentClass:Class<T>) : T? = PsiTreeUtil.getParentOfType(this, parentClass)
    override fun <T:PsiElement> getChildOfType(childClass:Class<T>) : T? = PsiTreeUtil.getChildOfType(this, childClass)
    override fun <T:PsiElement> getChildrenOfType(childClass:Class<T>) : List<T> = PsiTreeUtil.getChildrenOfTypeAsList(this, childClass)

    override fun getLanguage(): Language = ObjJLanguage.instance
    override fun toString(): String = tokenType().toString()

}