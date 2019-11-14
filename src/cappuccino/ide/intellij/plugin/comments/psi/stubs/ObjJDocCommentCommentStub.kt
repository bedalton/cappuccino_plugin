package cappuccino.ide.intellij.plugin.comments.psi.stubs

import cappuccino.ide.intellij.plugin.comments.psi.impl.ObjJDocCommentCommentImpl
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readInferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.stubs.writeInferenceResult
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.psi.stubs.*

val ObjJ_DOC_COMMENT_COMMENT_STUB_TYPE = ObjJDocCommentCommentStubType("ObjJDocComment_COMMENT")

interface ObjJDocCommentCommentStub : StubElement<ObjJDocCommentCommentImpl> {
    val parameters:List<ObjJDocCommentParameterStruct>
    val returnType:InferenceResult?
}

class ObjJDocCommentCommentStubImpl(parent:StubElement<*>, override val returnType:InferenceResult?, override val parameters:List<ObjJDocCommentParameterStruct>)
    : StubBase<ObjJDocCommentCommentImpl>(parent, ObjJ_DOC_COMMENT_COMMENT_STUB_TYPE), ObjJDocCommentCommentStub


class ObjJDocCommentCommentStubType(debugName:String): IStubElementType<ObjJDocCommentCommentStub, ObjJDocCommentCommentImpl>(debugName, ObjJLanguage.instance) {
    override fun createPsi(param: ObjJDocCommentCommentStub): ObjJDocCommentCommentImpl {
        return ObjJDocCommentCommentImpl(param, this)
    }

    override fun serialize(stub: ObjJDocCommentCommentStub, stream: StubOutputStream) {
        stream.writeParametersList(stub.parameters)
        stream.writeInferenceResult(stub.returnType)
    }

    override fun deserialize(stream: StubInputStream, parent: StubElement<*>): ObjJDocCommentCommentStub {
        val parameters = stream.readParametersList()
        val returnType = stream.readInferenceResult()
        return ObjJDocCommentCommentStubImpl(parent, returnType, parameters)
    }

    override fun createStub(element: ObjJDocCommentCommentImpl, parent: StubElement<*>): ObjJDocCommentCommentStub {
        val parameters = element.getParametersAsStructs()
        val returnType = element.returnType
        return ObjJDocCommentCommentStubImpl(
                parent = parent,
                parameters = parameters,
                returnType = returnType
        )
    }

    override fun getExternalId(): String {
        return "objj." + toString()
    }

    override fun indexStub(stub: ObjJDocCommentCommentStub, sink: IndexSink) {
    }

}

private fun StubInputStream.readParametersList() : List<ObjJDocCommentParameterStruct> {
    val numProperties = readInt()
    return (0 until numProperties).mapNotNull {
        readParameter()
    }
}

private fun StubInputStream.readParameter() : ObjJDocCommentParameterStruct {
    val name = readName()?.string
    val type = readInferenceResult();
    return ObjJDocCommentParameterStruct(name, type)
}

private fun StubOutputStream.writeParametersList(params:List<ObjJDocCommentParameterStruct>) {
    writeInt(params.size)
    return params.forEach {
        writeParameter(it)
    }
}

private fun StubOutputStream.writeParameter(param:ObjJDocCommentParameterStruct) {
    writeName(param.name);
    writeInferenceResult(param.types)
}

data class ObjJDocCommentParameterStruct(override val name: String?, val types:InferenceResult?) : JsNamedProperty {
    override val static: Boolean = false
}