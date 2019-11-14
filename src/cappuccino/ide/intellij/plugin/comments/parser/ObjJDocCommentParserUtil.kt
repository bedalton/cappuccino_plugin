package cappuccino.ide.intellij.plugin.comments.parser

import cappuccino.ide.intellij.plugin.comments.psi.api.ObjJDocCommentComment
import cappuccino.ide.intellij.plugin.comments.psi.api.ObjJDocCommentTagLine
import cappuccino.ide.intellij.plugin.comments.psi.stubs.ObjJDocCommentParameterStruct
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.combine
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType

object ObjJDocCommentParserUtil {

    @JvmStatic
    fun getParametersAsStructs(comment: ObjJDocCommentComment): List<ObjJDocCommentParameterStruct> {
        return comment.stub?.parameters ?: getParameterTags(comment)
                .mapNotNull {
                    val parameterName = it.parameterName
                            ?: return@mapNotNull null
                    ObjJDocCommentParameterStruct(parameterName, it.types)
                }
    }

    @JvmStatic
    fun getParameterTags(comment: ObjJDocCommentComment): List<ObjJDocCommentTagLine> {
        return getLinesWithTag(comment, ObjJDocCommentKnownTag.PARAM)
    }

    @JvmStatic
    fun getReturnType(comment: ObjJDocCommentComment): InferenceResult? {
        return comment.stub?.returnType ?: getReturnTags(comment)
                .mapNotNull {
                    it.types
                }
                .combine()
    }

    @JvmStatic
    fun getReturnTags(comment: ObjJDocCommentComment): List<ObjJDocCommentTagLine> {
        return getLinesWithTag(comment, ObjJDocCommentKnownTag.RETURN)
    }

    @JvmStatic
    fun getLinesWithTag(comment: ObjJDocCommentComment, tag: ObjJDocCommentKnownTag): List<ObjJDocCommentTagLine> {
        return comment.tagLineList.filter {
            it.tag == tag
        }
    }

    @JvmStatic
    fun getTag(tagLine: ObjJDocCommentTagLine): ObjJDocCommentKnownTag? {
        val tagName = tagLine.tagName.text
        return ObjJDocCommentKnownTag.findByTagName(tagName)
    }

    @JvmStatic
    fun getTypes(tagLine: ObjJDocCommentTagLine): InferenceResult? {
        return tagLine.stub?.types ?: tagLine.typesList
                .qualifiedNameList
                .mapNotNull {
                    JsTypeListType.JsTypeListBasicType(it.text)
                }
                .toSet().let {
                    InferenceResult(types = it)
                }
    }

    @JvmStatic
    fun getParameterName(tagLine: ObjJDocCommentTagLine) : String? {
        return tagLine.stub?.paramName ?: tagLine.parameterNameElement?.text
    }

    @JvmStatic
    fun getCommentText(tagLine:ObjJDocCommentTagLine) : String? {
        return tagLine.stub?.commentText ?: tagLine.textLine?.text
    }
}