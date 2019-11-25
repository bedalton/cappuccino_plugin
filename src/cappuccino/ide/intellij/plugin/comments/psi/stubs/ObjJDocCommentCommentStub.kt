package cappuccino.ide.intellij.plugin.comments.psi.stubs

import cappuccino.ide.intellij.plugin.comments.parser.ObjJDocCommentKnownTag
import cappuccino.ide.intellij.plugin.comments.psi.impl.ObjJDocCommentCommentImpl
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readInferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readNameAsString
import cappuccino.ide.intellij.plugin.jstypedef.stubs.writeInferenceResult
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.psi.stubs.*

val ObjJ_DOC_COMMENT_COMMENT_STUB_TYPE = ObjJDocCommentCommentStubType("ObjJDocComment_COMMENT")

interface ObjJDocCommentCommentStub : StubElement<ObjJDocCommentCommentImpl> {
    val parameters:List<ObjJDocCommentTagLineStruct>
    val tagLines:List<ObjJDocCommentTagLineStruct>
    val textLines:List<String>
    val returnType:InferenceResult?
}

class ObjJDocCommentCommentStubImpl(
        parent:StubElement<*>,
        override val returnType:InferenceResult?,
        override val tagLines:List<ObjJDocCommentTagLineStruct>,
        override val textLines:List<String>
) : StubBase<ObjJDocCommentCommentImpl>(parent, ObjJ_DOC_COMMENT_COMMENT_STUB_TYPE), ObjJDocCommentCommentStub {
    override val parameters:List<ObjJDocCommentTagLineStruct> by lazy {
        tagLines.filter { it.tag == ObjJDocCommentKnownTag.PARAM || it.tag == ObjJDocCommentKnownTag.VAR }
    }
}


class ObjJDocCommentCommentStubType(debugName:String): IStubElementType<ObjJDocCommentCommentStub, ObjJDocCommentCommentImpl>(debugName, ObjJLanguage.instance) {
    override fun createPsi(param: ObjJDocCommentCommentStub): ObjJDocCommentCommentImpl {
        return ObjJDocCommentCommentImpl(param, this)
    }

    override fun serialize(stub: ObjJDocCommentCommentStub, stream: StubOutputStream) {
        stream.writeTagLineList(stub.tagLines)
        stream.writeInferenceResult(stub.returnType)
        stream.writeStringList(stub.textLines)
    }

    override fun deserialize(stream: StubInputStream, parent: StubElement<*>): ObjJDocCommentCommentStub {
        val parameters = stream.readTagLineList()
        val returnType = stream.readInferenceResult()
        val textLines = stream.readStringList()
        return ObjJDocCommentCommentStubImpl(
                parent = parent,
                returnType = returnType,
                tagLines = parameters,
                textLines = textLines
        )
    }

    override fun createStub(element: ObjJDocCommentCommentImpl, parent: StubElement<*>): ObjJDocCommentCommentStub {
        val tagLines = element.tagLinesAsStructs
        val returnType = element.returnType
        val textLines = element.textLinesAsStrings
        return ObjJDocCommentCommentStubImpl(
                parent = parent,
                tagLines = tagLines,
                returnType = returnType,
                textLines = textLines
        )
    }

    override fun getExternalId(): String {
        return "objj." + toString()
    }

    override fun indexStub(stub: ObjJDocCommentCommentStub, sink: IndexSink) {
    }

}

private fun StubInputStream.readTagLineList() : List<ObjJDocCommentTagLineStruct> {
    val numProperties = readInt()
    return (0 until numProperties).mapNotNull {
        readTagLine()
    }
}

private fun StubInputStream.readStringList() : List<String> {
    val numProperties = readInt()
    return (0 until numProperties).mapNotNull {
        readUTFFast()
    }
}

private fun StubInputStream.readTagLine() : ObjJDocCommentTagLineStruct {
    val tag:ObjJDocCommentKnownTag = ObjJDocCommentKnownTag.valueOf(readNameAsString() ?: "");
    val name = readNameAsString()
    val type = readInferenceResult();
    val text = readUTFFast()
    return ObjJDocCommentTagLineStruct(tag, name, type, text)
}

private fun StubOutputStream.writeTagLineList(params:List<ObjJDocCommentTagLineStruct>) {
    writeInt(params.size)
    return params.forEach {
        writeTagLine(it)
    }
}

private fun StubOutputStream.writeTagLine(param:ObjJDocCommentTagLineStruct) {
    writeName(param.tag.name)
    writeName(param.name);
    writeInferenceResult(param.types)
    writeUTFFast(param.text)
}

private fun StubOutputStream.writeStringList(params:List<String>) {
    writeInt(params.size)
    return params.forEach {
        writeUTFFast(it)
    }
}

data class ObjJDocCommentTagLineStruct(val tag:ObjJDocCommentKnownTag, override val name: String?, val types:InferenceResult?, val text:String) : JsNamedProperty {
    override val static: Boolean = false
}

