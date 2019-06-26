package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.*
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.PsiFileStub

class JsTypeDefIndexService : StubIndexService() {

    override fun indexFile(stub:PsiFileStub<*>, sink: IndexSink) {
            if (stub !is JsTypeDefFileStub) {
                return
            }
    }


    override fun indexFunction(stub: JsTypeDefFunctionStub, sink:IndexSink) {
        sink.occurrence<JsTypeDefFunction, String>(JsTypeDefFunctionsByNameIndex.instance.key, stub.functionName)
        sink.occurrence<JsTypeDefFunction, String>(JsTypeDefFunctionsByNamespaceIndex.instance.key, stub.fullyNamespacedName)
    }

    override fun indexProperty(stub: JsTypeDefPropertyStub, sink:IndexSink) {
        sink.occurrence<JsTypeDefProperty, String>(JsTypeDefPropertiesByNameIndex.instance.key, stub.propertyName)
        sink.occurrence<JsTypeDefProperty, String>(JsTypeDefPropertiesByNamespaceIndex.instance.key, stub.fullyNamespacedName)
    }

    override fun indexModule(stub: JsTypeDefModuleStub, sink:IndexSink) {
        sink.occurrence<JsTypeDefModule, String>(JsTypeDefModulesByNameIndex.instance.key, stub.moduleName)
        sink.occurrence<JsTypeDefModule, String>(JsTypeDefModulesByNamespaceIndex.instance.key, stub.fullyNamespacedName)

    }

    override fun indexModuleName(stub: JsTypeDefModuleNameStub, sink:IndexSink) {
        sink.occurrence<JsTypeDefModuleName, String>(JsTypeDefModuleNamesByNameIndex.instance.key, stub.moduleName)
        sink.occurrence<JsTypeDefModuleName, String>(JsTypeDefModuleNamesByNamespaceIndex.instance.key, stub.fullyNamespacedName)
    }

    override fun indexInterface(stub:JsTypeDefInterfaceStub, sink:IndexSink) {
        sink.occurrence<JsTypeDefInterfaceElement, String>(JsTypeDefClassesByNameIndex.KEY, stub.className)
        sink.occurrence<JsTypeDefInterfaceElement, String>(JsTypeDefClassesByNamespaceIndex.KEY, stub.fullyNamespacedName)
        for (superType in stub.superTypes) {
            sink.occurrence<JsTypeDefInterfaceElement, String>(JsTypeDefClassesByNamespaceIndex.KEY, superType)
        }
    }

    companion object {
        const val SOURCE_STUB_VERSION = 0
    }
}