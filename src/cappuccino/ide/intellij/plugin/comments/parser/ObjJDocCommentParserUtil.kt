package cappuccino.ide.intellij.plugin.comments.parser

import cappuccino.ide.intellij.plugin.comments.psi.api.*
import cappuccino.ide.intellij.plugin.comments.psi.stubs.ObjJDocCommentParameterStruct
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.combine
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.utils.ObjJQualifiedReferenceUtil
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJQualifiedReferenceComponentPart
import cappuccino.ide.intellij.plugin.stubs.types.toStubParts
import com.intellij.psi.PsiElement

object ObjJDocCommentParserUtil {

    @JvmStatic
    fun getName(element:ObjJDocCommentQualifiedNameComponent) : String {
        return element.text
    }

    @JvmStatic
    fun setName(oldName:ObjJDocCommentQualifiedNameComponent, newNameString:String) : PsiElement {
        val newName:ObjJDocCommentQualifiedNameComponent = ObjJElementFactory.createDocCommentQualifiedReferenceComponent(oldName.project, newNameString)
        return oldName.replace(newName)
    }

    @JvmStatic
    fun getIndexInQualifiedReference(element:ObjJDocCommentQualifiedNameComponent) : Int {
        return ObjJQualifiedReferenceUtil.getIndexInQualifiedNameParent(element)
    }

    @JvmStatic
    fun getName(element:ObjJDocCommentParameterName) : String {
        return element.text
    }

    @JvmStatic
    fun setName(oldName:ObjJDocCommentParameterName, newNameString:String) : PsiElement {
        val newName:ObjJDocCommentParameterName = ObjJElementFactory.createDocCommentParameterName(oldName.project, newNameString)
        return oldName.replace(newName)
    }

    @JvmStatic
    fun getIndexInQualifiedReference(element:ObjJDocCommentParameterName) : Int {
        return ObjJQualifiedReferenceUtil.getIndexInQualifiedNameParent(element)
    }

    @JvmStatic
    fun getQualifiedNameParts(element:ObjJDocCommentQualifiedName) : List<ObjJQualifiedReferenceComponent> {
        return element.qualifiedNameComponentList
    }

    @JvmStatic
    fun getQualifiedNamePath(qualifiedReference:ObjJDocCommentQualifiedName) : List<ObjJQualifiedReferenceComponentPart> {
        return qualifiedReference.toStubParts()
    }

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
    fun getTagLinesAsStructs(comment: ObjJDocCommentComment): List<ObjJDocCommentParameterStruct> {
        return comment.stub?.tagLines ?: getParameterTags(comment)
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
    fun getParameterNameElement(tagLine: ObjJDocCommentTagLine): ObjJDocCommentElement? {
        return tagLine.parameterNameElementElement ?:
        if (tagLine.typesList.qualifiedNameList.size == 1)
            tagLine.typesList.qualifiedNameList.lastOrNull()
        else null
    }

    @JvmStatic
    fun getParameterName(tagLine: ObjJDocCommentTagLine) : String? {
        return tagLine.stub?.parameterName ?: tagLine.parameterNameElement?.text
    }

    @JvmStatic
    fun getCommentText(tagLine:ObjJDocCommentTagLine) : String? {
        return tagLine.stub?.commentText ?: tagLine.textLine?.text
    }
}