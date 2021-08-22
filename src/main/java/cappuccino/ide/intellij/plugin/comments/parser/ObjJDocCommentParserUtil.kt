package cappuccino.ide.intellij.plugin.comments.parser

import cappuccino.ide.intellij.plugin.comments.psi.api.*
import cappuccino.ide.intellij.plugin.comments.psi.stubs.ObjJDocCommentTagLineStruct
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.combine
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJUniversalQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.utils.ObjJQualifiedReferenceUtil
import cappuccino.ide.intellij.plugin.references.ObjJDocCommentNameElementReference
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJQualifiedReferenceComponentPart
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJQualifiedReferenceComponentPartType
import com.intellij.psi.PsiElement

object ObjJDocCommentParserUtil {

    @JvmStatic
    fun getName(element: ObjJDocCommentQualifiedNameComponent): String {
        return element.text
    }

    @JvmStatic
    fun setName(oldName: ObjJDocCommentQualifiedNameComponent, newNameString: String): PsiElement {
        val newName: ObjJDocCommentQualifiedNameComponent =
            ObjJElementFactory.createDocCommentQualifiedReferenceComponent(oldName.project, newNameString)
        return oldName.replace(newName)
    }

    @JvmStatic
    fun getIndexInQualifiedReference(element: ObjJDocCommentQualifiedNameComponent): Int {
        return ObjJQualifiedReferenceUtil.getIndexInQualifiedNameParent(element)
    }

    @JvmStatic
    fun getQualifiedNameParts(element: ObjJDocCommentQualifiedName): List<ObjJUniversalQualifiedReferenceComponent> {
        return element.qualifiedNameComponentList
    }

    @JvmStatic
    fun getQualifiedNamePath(qualifiedReference: ObjJDocCommentQualifiedName): List<ObjJQualifiedReferenceComponentPart> {
        return qualifiedReference.qualifiedNameComponentList
            .flatMap {
                if (it.openBracket != null) {
                    listOf(
                        ObjJQualifiedReferenceComponentPart(it.text,
                            ObjJQualifiedReferenceComponentPartType.VARIABLE_NAME),
                        ObjJQualifiedReferenceComponentPart("[]",
                            ObjJQualifiedReferenceComponentPartType.ARRAY_COMPONENT)
                    )
                } else
                    listOf(
                        ObjJQualifiedReferenceComponentPart(it.text,
                            ObjJQualifiedReferenceComponentPartType.VARIABLE_NAME)
                    )

            }
    }

    @JvmStatic
    fun getReference(namedElement: ObjJDocCommentQualifiedNameComponent): ObjJDocCommentNameElementReference {
        return ObjJDocCommentNameElementReference(namedElement)
    }

    @JvmStatic
    fun isArrayComponent(namedElement: ObjJDocCommentQualifiedNameComponent): Boolean {
        return namedElement.openBracket != null
    }

    @JvmStatic
    fun getTagLinesAsStructs(comment: ObjJDocCommentComment): List<ObjJDocCommentTagLineStruct> {
        return comment.stub?.tagLines
            ?: comment.tagLines
                .mapNotNull {
                    val parameterName = it.parameterNameString
                        ?: return@mapNotNull null
                    val text = it.textElement?.text.orEmpty()
                    ObjJDocCommentTagLineStruct(it.tag ?: ObjJDocCommentKnownTag.UNKNOWN, parameterName, it.types, text)
                }
    }

    @JvmStatic
    fun getParametersAsStructs(comment: ObjJDocCommentComment): List<ObjJDocCommentTagLineStruct> {
        return comment.stub?.parameters ?: getTagLinesAsStructs(comment).filter {
            it.tag == ObjJDocCommentKnownTag.PARAM
        }
    }

    @JvmStatic
    fun getReturnTagAsStruct(comment: ObjJDocCommentComment): ObjJDocCommentTagLineStruct? {
        return getTagLinesAsStructs(comment).firstOrNull {
            it.tag == ObjJDocCommentKnownTag.RETURN
        }
    }

    @JvmStatic
    fun getParameterTags(comment: ObjJDocCommentComment): List<ObjJDocCommentTagLineBase> {
        return getLinesWithTag(comment, ObjJDocCommentKnownTag.PARAM)
    }

    @JvmStatic
    fun getReturnType(comment: ObjJDocCommentComment): InferenceResult? {
        return comment.stub?.returnType ?: getReturnTags(comment)
            .mapNotNull {
                it.types
            }
            .ifEmpty { null }
            ?.combine()
    }

    @JvmStatic
    fun getReturnTags(comment: ObjJDocCommentComment): List<ObjJDocCommentTagLineBase> {
        return getLinesWithTag(comment, ObjJDocCommentKnownTag.RETURN)
    }

    @JvmStatic
    fun getLinesWithTag(comment: ObjJDocCommentComment, tag: ObjJDocCommentKnownTag): List<ObjJDocCommentTagLineBase> {
        return comment
            .tagLines
            .filter {
                it.tag == tag
            }
    }

    @JvmStatic
    fun getTag(tagLine: ObjJDocCommentTagLine): ObjJDocCommentKnownTag? {
        val tagName = tagLine.tagName.text
        return ObjJDocCommentKnownTag.findByTagName(tagName)
    }


    @JvmStatic
    fun getTag(tagLine: ObjJDocCommentOldTagLine): ObjJDocCommentKnownTag? {
        val tagName = tagLine.tagName.text
        return ObjJDocCommentKnownTag.findByTagName(tagName)
    }

    @JvmStatic
    fun getTypes(tagLine: ObjJDocCommentTagLine): InferenceResult? {
        return tagLine.stub?.types ?: tagLine
            .typeList
            .ifEmpty { null }
            ?.mapNotNull {
                JsTypeListType.JsTypeListBasicType(it.text)
            }
            ?.toSet()?.let {
                InferenceResult(types = it)
            }
    }

    @JvmStatic
    fun getTypesList(tagLine: ObjJDocCommentTagLine): List<ObjJDocCommentQualifiedName> {
        return tagLine.typeList.map { it.qualifiedName }
    }


    @JvmStatic
    fun getTypesList(tagLine: ObjJDocCommentOldTagLine): List<ObjJDocCommentQualifiedName> {
        return tagLine.oldTypesList.qualifiedNameList
    }

    @JvmStatic
    fun getTypes(tagLine: ObjJDocCommentOldTagLine): InferenceResult? {
        return tagLine.stub?.types ?: tagLine.oldTypesList
            .qualifiedNameList
            .ifEmpty { null }
            ?.map {
                JsTypeListType.JsTypeListBasicType(it.text)
            }
            ?.toSet()?.let {
                InferenceResult(types = it)
            }
    }

    @JvmStatic
    fun getParameterNameElement(tagLine: ObjJDocCommentOldTagLine): ObjJDocCommentQualifiedName? {
        return tagLine.qualifiedName ?: if (tagLine.oldTypesList.qualifiedNameList.size == 1)
            tagLine.oldTypesList.qualifiedNameList.lastOrNull()
        else null
    }

    @JvmStatic
    fun getParameterNameElement(tagLine: ObjJDocCommentTagLine): ObjJDocCommentQualifiedName? {
        return tagLine.qualifiedName ?: if (tagLine.typeList.size == 1) {
            val out = tagLine.typeList.lastOrNull()
            if (out?.ellipsesLiteral != null || tagLine.asLiteral != null)
                null
            else
                out?.qualifiedName
        } else null
    }

    @JvmStatic
    fun defaultValue(element: ObjJDocCommentDefaultValue): String? {
        return element.textElement?.text
    }

    @JvmStatic
    fun getParameterNameString(tagLine: ObjJDocCommentTagLine): String? {
        return tagLine.stub?.parameterName ?: tagLine.qualifiedName?.text
        ?: tagLine.optionalParameter?.qualifiedName?.text
    }


    @JvmStatic
    fun getParameterNameString(tagLine: ObjJDocCommentOldTagLine): String? {
        return tagLine.stub?.parameterName ?: tagLine.parameterNameElement?.text
    }

    @JvmStatic
    fun getTagNameString(tagLine: ObjJDocCommentTagLine): String? {
        return (tagLine.stub?.tag ?: tagLine.tag)?.tagName
    }


    @JvmStatic
    fun getTagNameString(tagLine: ObjJDocCommentOldTagLine): String? {
        return (tagLine.stub?.tag ?: tagLine.tag)?.tagName
    }

    @JvmStatic
    fun getCommentText(tagLine: ObjJDocCommentTagLine): String? {
        return tagLine.stub?.commentText ?: tagLine.textElement?.text
    }

    @JvmStatic
    fun getCommentText(tagLine: ObjJDocCommentOldTagLine): String? {
        return tagLine.stub?.commentText ?: tagLine.textElement?.text
    }

    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun getOptionalParameter(tagLine: ObjJDocCommentOldTagLine): ObjJDocCommentOptionalParameter? {
        return null
    }

    @JvmStatic
    fun getTextLinesAsStrings(comment: ObjJDocCommentComment): List<String> {
        return comment.stub?.textLines ?: comment.textLineList.mapNotNull { it.text }
    }

    @Suppress("unused", "UNUSED_VARIABLE")
    @JvmStatic
    fun parseJsClass(comment: ObjJDocCommentComment) {
        val name = comment
        val properties = comment
    }

    @JvmStatic
    fun getTagLines(comment: ObjJDocCommentComment): List<ObjJDocCommentTagLineBase> {
        return comment.tagLineList + comment.oldTagLineList
    }

    @Suppress("unused", "UNUSED_VARIABLE")
    @JvmStatic
    fun buildObjectType(comment: ObjJDocCommentComment) {
        // TODO implement object parsing
        val params = comment
            .tagLines
            .filter {
                it.tag == ObjJDocCommentKnownTag.PARAM
            }
    }

    @JvmStatic
    fun getBorrowedThat(tag: ObjJDocCommentTagLine): ObjJDocCommentQualifiedName? {
        return tag.getChildrenOfType(ObjJDocCommentQualifiedName::class.java).firstOrNull()
    }

    @JvmStatic
    fun getBorrowedAs(tag: ObjJDocCommentTagLine): ObjJDocCommentQualifiedName? {
        return tag.getChildrenOfType(ObjJDocCommentQualifiedName::class.java).getOrNull(1)
    }

}