package cappuccino.ide.intellij.plugin.comments.parser

import cappuccino.ide.intellij.plugin.comments.psi.api.ObjJDocCommentComment
import cappuccino.ide.intellij.plugin.comments.psi.stubs.ObjJDocCommentTagLineStruct
import cappuccino.ide.intellij.plugin.contributor.JsProperty
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJQualifiedReferenceComponentPartType

object ObjJDocCommentParser {

    fun getParameters(comment: ObjJDocCommentComment): Map<String, JsTypeListType> {
        val lines = comment.tagLines
        val builders = mutableMapOf<String, Builder>()

        for (line in lines) {
            if (line.tag != ObjJDocCommentKnownTag.PARAM) {
                continue
            }
            val types = line.types
                ?: continue
            val path = line.parameterNameElement?.qualifiedNamePath
                ?: continue
            var i = 0
            while (i < path.size) {
                var part = path[i++]
                val isArray = if (i < path.size) {
                    path[i].type == ObjJQualifiedReferenceComponentPartType.ARRAY_COMPONENT
                } else {
                    false
                }
                val isMethodCall
            }
        }
    }


    fun getElements(comment: ObjJDocCommentComment): List<JsTypeListType> {
        val out = mutableListOf<JsTypeListType>()
        val tagLines = comment.tagLinesAsStructs
        val text = comment.textLinesAsStrings.joinToString("\n")
        out.addAll(readCallbacks(text, comment))
    }

    fun readCallbacks(commentText: String, tagLines: List<ObjJDocCommentTagLineStruct>): List<JsTypeListType.JsTypeListFunctionType> {
        var i = 0
        var current: Builder? = null
        val out = mutableListOf<Builder>()
        while (i < tagLines.size) {
            val item = tagLines[i++]
            when (item.tag) {
                ObjJDocCommentKnownTag.PARAM -> {
                    val nameComponents = item.name?.split('.')
                        ?: continue
                    var builder = current
                        ?: continue
                    for (name in nameComponents.dropLast(1)) {
                        builder = builder.properties.firstOrNull {
                            it.name.toLowerCase() == name
                        } ?: Builder(name).apply {
                            builder.properties.add(this)
                        }
                    }
                }
                ObjJDocCommentKnownTag.CALLBACK -> {
                    val name = item.name
                    if (name == null) {
                        current = null
                        continue
                    }
                    current = Builder(
                        name,
                        InferenceResult(JsTypeListType.JsTypeListFunctionType(
                            name,
                            emptyList(),
                            null,
                            true,
                            null
                        )),
                        isArray = false,

                    )
                    out.add(current)
                }
            }
            if (item.tag == ) {
                continue
            }

        }
    }

}


private data class Builder(
    val name: String,
    var types: InferenceResult,
    var isArray: Boolean,
    var isObject: Boolean,
    var properties: MutableList<Builder> = mutableListOf()
)