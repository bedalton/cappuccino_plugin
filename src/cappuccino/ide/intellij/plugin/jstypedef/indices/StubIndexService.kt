package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.jstypedef.stubs.impl.JsTypeDefFileStubImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.*
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.ifEmptyNull
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.PsiFileStub

open class StubIndexService {


    open fun indexFile(stub: PsiFileStub<*>, sink: IndexSink) {
        LOGGER.warning("IndexFile should be overridden")
    }

    open fun indexFunction(stub: JsTypeDefFunctionStub, sink: IndexSink) {
        throw NotImplementedError("index functions method must be overridden")
    }

    open fun indexTypeAlias(stub:JsTypeDefTypeAliasStub, sink:IndexSink) {
        throw NotImplementedError("index typealias method must be overridden")
    }

    open fun indexProperty(stub: JsTypeDefPropertyStub, sink: IndexSink) {
        throw NotImplementedError("index property method must be overridden")
    }

    open fun indexModule(stub: JsTypeDefModuleStub, sink: IndexSink) {
        throw NotImplementedError("index module method must be overridden")
    }

    open fun indexModuleName(stub: JsTypeDefModuleNameStub, sink: IndexSink) {
        throw NotImplementedError("index module name method must be overridden")
    }

    open fun indexInterface(stub: JsTypeDefInterfaceStub, sink: IndexSink) {
        throw NotImplementedError("index interface indexing method must be overridden")
    }

    open fun indexClass(stub: JsTypeDefClassStub, sink: IndexSink) {
        throw NotImplementedError("index interface indexing method must be overridden")
    }

    open fun indexTypeMap(stub: JsTypeDefTypeMapStub, sink: IndexSink) {
        throw NotImplementedError("index interface indexing method must be overridden")
    }
    open fun indexKeyList(stub:JsTypeDefKeysListStub, sink: IndexSink) {
        throw NotImplementedError("index interface indexing method must be overridden")
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