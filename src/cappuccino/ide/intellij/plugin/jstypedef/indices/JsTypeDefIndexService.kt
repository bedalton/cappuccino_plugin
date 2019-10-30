package cappuccino.ide.intellij.plugin.jstypedef.indices

import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.*
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.PsiFileStub

class JsTypeDefIndexService : StubIndexService() {

    override fun indexFile(stub:PsiFileStub<*>, sink: IndexSink) {
            if (stub !is JsTypeDefFileStub) {
                return
            }
    }

    override fun indexFunction(stub: JsTypeDefFunctionStub, sink:IndexSink) {
        if (stub.functionName.isBlank())
            return
        sink.occurrence(JsTypeDefFunctionsByNameIndex.instance.key, stub.functionName)
        sink.occurrence(JsTypeDefFunctionsByNamespaceIndex.instance.key, stub.fullyNamespacedName)
        val enclosingClass = stub.enclosingClass
        if(enclosingClass.isNotNullOrBlank()) {
            sink.occurrence(JsTypeDefFunctionsByClassNamesIndex.instance.key, enclosingClass!!)
        }
    }

    override fun indexProperty(stub: JsTypeDefPropertyStub, sink:IndexSink) {
        if (stub.propertyName.isBlank())
            return
        sink.occurrence(JsTypeDefPropertiesByNameIndex.instance.key, stub.propertyName)
        sink.occurrence(JsTypeDefPropertiesByNamespaceIndex.instance.key, stub.fullyNamespacedName)
        val enclosingClass = stub.enclosingClass
        if(enclosingClass.isNotNullOrBlank()) {
            sink.occurrence(JsTypeDefPropertiesByClassNameIndex.instance.key, enclosingClass!!)
        }
    }

    override fun indexModule(stub: JsTypeDefModuleStub, sink:IndexSink) {
        if (stub.moduleName.isBlank())
            return
        sink.occurrence(JsTypeDefModulesByNameIndex.instance.key, stub.moduleName)
        sink.occurrence(JsTypeDefModulesByNamespaceIndex.instance.key, stub.fullyNamespacedName)

    }

    override fun indexModuleName(stub: JsTypeDefModuleNameStub, sink:IndexSink) {
        if (stub.moduleName.isBlank())
            return
        sink.occurrence(JsTypeDefModuleNamesByNameIndex.instance.key, stub.moduleName)
        sink.occurrence(JsTypeDefModuleNamesByNamespaceIndex.instance.key, stub.fullyNamespacedName)
    }

    override fun indexInterface(stub:JsTypeDefInterfaceStub, sink:IndexSink) {
        if(stub.className.isBlank())
            return
        sink.occurrence<JsTypeDefClassDeclaration<*,*>, String>(JsTypeDefClassesByNameIndex.KEY, stub.className)
        sink.occurrence<JsTypeDefClassDeclaration<*,*>, String>(JsTypeDefClassesByNamespaceIndex.KEY, stub.fullyNamespacedName)
        for (superType in stub.superTypes) {
            sink.occurrence<JsTypeDefClassDeclaration<*,*>, String>(JsTypeDefClassesBySuperClassIndex.KEY, superType.typeName)
        }
        val namespaceComponents = stub.namespaceComponents
        for (i in 1 .. namespaceComponents.size) {
            val namespace = namespaceComponents.subList(0, i).joinToString(".")
            sink.occurrence(JsTypeDefClassesByPartialNamespaceIndex.KEY, namespace)
        }
    }

    override fun indexClass(stub:JsTypeDefClassStub, sink:IndexSink) {
        if(stub.className.isBlank())
            return
        sink.occurrence<JsTypeDefClassDeclaration<*,*>, String>(JsTypeDefClassesByNameIndex.KEY, stub.className)
        sink.occurrence<JsTypeDefClassDeclaration<*,*>, String>(JsTypeDefClassesByNamespaceIndex.KEY, stub.fullyNamespacedName)
        for (superType in stub.superTypes) {
            sink.occurrence<JsTypeDefClassDeclaration<*,*>, String>(JsTypeDefClassesBySuperClassIndex.KEY, superType.typeName)
        }
        val namespaceComponents = stub.namespaceComponents
        for (i in 1 .. namespaceComponents.size) {
            val namespace = namespaceComponents.subList(0, i).joinToString(".")
            sink.occurrence(JsTypeDefClassesByPartialNamespaceIndex.KEY, namespace)
        }
    }

    override fun indexKeyList(stub:JsTypeDefKeysListStub, sink: IndexSink) {
        sink.occurrence(JsTypeDefKeyListsByNameIndex.instance.key, stub.listName)
    }

    override fun indexTypeAlias(stub: JsTypeDefTypeAliasStub, sink: IndexSink) {
        sink.occurrence(JsTypeDefTypeAliasIndex.KEY, stub.typeName)
    }

    override fun indexTypeMap(stub:JsTypeDefTypeMapStub, sink:IndexSink) {
        if(stub.mapName.isBlank()) {
            LOGGER.info("Map name is null or blank")
            return
        }
        sink.occurrence(JsTypeDefTypeMapByNameIndex.instance.key, stub.mapName)
    }
}