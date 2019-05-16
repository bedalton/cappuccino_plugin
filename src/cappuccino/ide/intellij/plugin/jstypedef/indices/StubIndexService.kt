package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.jstypedef.stubs.impl.JsTypeDefFileStubImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefFileStub
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.PsiFileStub

open class StubIndexService {


    open fun indexFile(stub:PsiFileStub<*>, sink: IndexSink) {
        LOGGER.warning("IndexFile should be overridden")
    }

    fun createFileStub(file:JsTypeDefFile) : JsTypeDefFileStub {
        val fileName = file.containerName
        return JsTypeDefFileStubImpl(file, fileName)
    }

    companion object {

        /**
         * Emtpy service implementation
         */
        private val EMPTY_SERVICE = StubIndexService()

        val instance: StubIndexService
            get() {
                val service = ServiceManager.getService(JsTypeDefIndexService::class.java)
                return service ?: EMPTY_SERVICE
            }
    }
}