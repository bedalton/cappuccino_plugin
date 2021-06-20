package cappuccino.ide.intellij.plugin.comments.psi.stubs

import cappuccino.ide.intellij.plugin.comments.parser.ObjJDocCommentKnownTag
import cappuccino.ide.intellij.plugin.comments.psi.impl.ObjJDocCommentOldTagLineImpl
import cappuccino.ide.intellij.plugin.comments.psi.impl.ObjJDocCommentTagLineImpl
import cappuccino.ide.intellij.plugin.inference.INFERRED_ANY_TYPE
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readInferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.stubs.readNameAsString
import cappuccino.ide.intellij.plugin.jstypedef.stubs.writeInferenceResult
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes.ObjJ_DOC_COMMENT_TAG_LINE
import cappuccino.ide.intellij.plugin.utils.orElse
import com.intellij.psi.stubs.*


interface ObjJDocCommentOldTagLineStub : StubElement<ObjJDocCommentOldTagLineImpl> {
    val tag:ObjJDocCommentKnownTag
    val parameterName:String?
    val types:InferenceResult
    val commentText:String?
}

class ObjJDocCommentOldTagLineStubImpl(
        parent:StubElement<*>,
        override val tag:ObjJDocCommentKnownTag,
        override val parameterName:String?,
        override val types:InferenceResult = INFERRED_ANY_TYPE,
        override val commentText:String?
): StubBase<ObjJDocCommentOldTagLineImpl>(parent, ObjJ_DOC_COMMENT_TAG_LINE), ObjJDocCommentOldTagLineStub;

class ObjJDocCommentOldTagLineStubElementType(debugName:String)
    : IStubElementType<ObjJDocCommentOldTagLineStub, ObjJDocCommentOldTagLineImpl>(debugName, ObjJLanguage.instance)
{
    override fun createPsi(stub: ObjJDocCommentOldTagLineStub): ObjJDocCommentOldTagLineImpl {
        return ObjJDocCommentOldTagLineImpl(stub, this);
    }

    override fun serialize(stub: ObjJDocCommentOldTagLineStub, stream: StubOutputStream) {
        stream.writeInt(ObjJDocCommentKnownTag.PARAM.ordinal)
        stream.writeName(stub.parameterName)
        val types = if (stub.types == INFERRED_ANY_TYPE) null else stub.types
        stream.writeInferenceResult(types)
        stream.writeUTFFast(stub.commentText ?: "")
    }

    override fun deserialize(stream: StubInputStream, parent: StubElement<*>): ObjJDocCommentOldTagLineStub {
        val tagOrdinal = stream.readInt();
        val tagType = ObjJDocCommentKnownTag
                .values()
                .firstOrNull() {
                    it.ordinal == tagOrdinal
                }
                .orElse(ObjJDocCommentKnownTag.UNKNOWN)
        val parameterName = stream.readNameAsString();
        val types = stream.readInferenceResult() ?: INFERRED_ANY_TYPE
        val commentText = stream.readUTFFast()
        return ObjJDocCommentOldTagLineStubImpl(
                parent = parent,
                tag = tagType,
                types = types,
                parameterName = parameterName,
                commentText = commentText
        )
    }

    override fun createStub(element: ObjJDocCommentOldTagLineImpl, parent: StubElement<*>): ObjJDocCommentOldTagLineStub {
        val tagType = element.tag ?: ObjJDocCommentKnownTag.UNKNOWN
        val types = element.types ?: INFERRED_ANY_TYPE
        val parameterName = element.parameterNameString
        val text = element.commentText
        return ObjJDocCommentOldTagLineStubImpl(
                parent = parent,
                tag = tagType,
                types = types,
                parameterName = parameterName,
                commentText = text
        )
    }

    override fun getExternalId(): String {
        return "objj." + toString()
    }

    override fun indexStub(stub: ObjJDocCommentOldTagLineStub, indexSink: IndexSink) {
    }

}