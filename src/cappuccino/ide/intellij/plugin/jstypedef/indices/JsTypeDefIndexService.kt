package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefFileStub
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.PsiFileStub

class JsTypeDefIndexService : StubIndexService() {

    override fun indexFile(stub:PsiFileStub<*>, sink: IndexSink) {
            if (stub !is JsTypeDefFileStub) {
                return
            }
            // Index file by name
            sink.occurrence<JsTypeDefFile, String>(JsTypeDefFilesByNameIndex.instance.key, stub.fileName)
    }

    companion object {
        const val SOURCE_STUB_VERSION = 1
    }
}