package cappuccino.ide.intellij.plugin.jstypedef.stubs.types

import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefIndexService
import cappuccino.ide.intellij.plugin.jstypedef.indices.StubIndexService
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefLanguage
import cappuccino.ide.intellij.plugin.jstypedef.stubs.JsTypeDefStubVersion
import cappuccino.ide.intellij.plugin.jstypedef.stubs.impl.JsTypeDefFileStubImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefFileStub
import com.intellij.psi.PsiFile
import com.intellij.psi.StubBuilder
import com.intellij.psi.stubs.*
import com.intellij.psi.tree.IStubFileElementType

class JsTypeDefFileStubType internal constructor(): IStubFileElementType<JsTypeDefFileStub>(NAME, JsTypeDefLanguage.instance) {

    override fun getBuilder(): StubBuilder {
        return JsTypeDefFileStubBuilder()
    }

    override fun getStubVersion(): Int {
        return JsTypeDefStubVersion.VERSION
    }

    override fun getExternalId(): String {
        return NAME
    }

    override fun serialize(stub: JsTypeDefFileStub, stream: StubOutputStream) {
        super.serialize(stub, stream)
        stream.writeName(stub.fileName)
    }

    override fun deserialize(stream: StubInputStream, parentStub: StubElement<*>?): JsTypeDefFileStub {
        super.deserialize(stream, parentStub)
        val fileName = stream.readNameString() ?: "???"
        return JsTypeDefFileStubImpl(null, fileName)
    }

    override fun indexStub(stub: PsiFileStub<*>, sink: IndexSink) {
        StubIndexService.instance.indexFile(stub, sink)
    }

    companion object {

        private const val NAME = "jstypedef.FILE"
    }
}

private class JsTypeDefFileStubBuilder : DefaultStubBuilder() {
    override fun createStubForFile(file: PsiFile): StubElement<*> {
        return if (file !is JsTypeDefFile) {
            super.createStubForFile(file)
        } else cappuccino.ide.intellij.plugin.jstypedef.indices.StubIndexService.instance.createFileStub(file)
    }
}
