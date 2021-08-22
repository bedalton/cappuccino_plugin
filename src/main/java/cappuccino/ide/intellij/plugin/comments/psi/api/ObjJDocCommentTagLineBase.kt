package cappuccino.ide.intellij.plugin.comments.psi.api

import cappuccino.ide.intellij.plugin.comments.parser.ObjJDocCommentKnownTag
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import com.intellij.psi.PsiElement

interface ObjJDocCommentTagLineBase: PsiElement{
    val typesList: List<ObjJDocCommentQualifiedName>
    val textElement: ObjJDocCommentTextElement?
    val parameterNameElement: ObjJDocCommentQualifiedName?
    val parameterNameString: String?
    val tag: ObjJDocCommentKnownTag?
    val types: InferenceResult?
    val commentText: String?
    val tagName: ObjJDocCommentTagNameElement
    val tagNameString: String?
    val optionalParameter: ObjJDocCommentOptionalParameter?
}