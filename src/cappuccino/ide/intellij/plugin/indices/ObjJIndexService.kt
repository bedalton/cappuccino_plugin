package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.IndexSink
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration

import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.stubs.interfaces.*
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiFileUtil

import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.EMPTY_SELECTOR
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.SELECTOR_SYMBOL
import cappuccino.ide.intellij.plugin.stubs.ObjJStubVersions
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJPropertyNameStub
import java.util.logging.Level
import java.util.logging.Logger

class ObjJIndexService
//private static final Logger LOGGER = Logger.getLogger("#objj.ObjJIndexService");

internal constructor()//   Logger.getGlobal().log(Level.INFO, "Creating ObjJIndexService");
    : StubIndexService() {

    /**
     * Index method header stubs
     * @param methodHeaderStub method header stub to index
     * @param indexSink index sink
     */
    override fun indexMethod(methodHeaderStub: ObjJMethodHeaderStub, indexSink: IndexSink) {
        val selector = methodHeaderStub.selectorString
        if (selector == EMPTY_SELECTOR || selector == EMPTY_SELECTOR + SELECTOR_SYMBOL) {
            return
        }
        if (selector.isEmpty()) {
            return
        }

        try {
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJUnifiedMethodIndex.KEY, selector)
        } catch (e: Exception) {
            LOGGER.log(Level.SEVERE, "Failed to index selector with error: ${e.localizedMessage}")
        }
        val className = methodHeaderStub.containingClassName
        indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJClassAndSelectorMethodIndex.KEY, ObjJClassAndSelectorMethodIndex.getClassMethodKey(className, selector))

        val selectorBuilder = StringBuilder()
        for (subSelector in methodHeaderStub.selectorStrings) {
            selectorBuilder.append(subSelector).append(SELECTOR_SYMBOL)
            val currentSelector = selectorBuilder.toString()
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJMethodFragmentIndex.KEY, currentSelector)
        }

        try {
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJClassMethodIndex.KEY, className)
        } catch (e: Exception) {
            LOGGER.log(Level.SEVERE, "Failed to index class&selector tuple with error: ${e.localizedMessage}")
        }

    }

    /**
     * Indexes instance variable stubs
     * @param variableDeclarationStub instance variable stub to index
     * @param indexSink index sink
     */
    override fun indexInstanceVariable(variableDeclarationStub: ObjJInstanceVariableDeclarationStub, indexSink: IndexSink) {

        // Index Instance variables to class name
        indexSink.occurrence<ObjJInstanceVariableDeclaration, String>(ObjJInstanceVariablesByClassIndex.instance.key, variableDeclarationStub.containingClass)

        // Index variables by variable name
        indexSink.occurrence<ObjJInstanceVariableDeclaration, String>(ObjJInstanceVariablesByNameIndex.instance.key, variableDeclarationStub.variableName)

        // Index Getter accessors
        val getter = variableDeclarationStub.getter
        if (getter != null && getter.isNotBlank()) {
            indexSink.occurrence<ObjJInstanceVariableDeclaration, String>(ObjJClassInstanceVariableAccessorMethodIndex.instance.key, getter)
        }
        // Index setters
        val setter = variableDeclarationStub.setter
        if (setter != null && setter.isNotBlank()) {
            indexSink.occurrence<ObjJInstanceVariableDeclaration, String>(ObjJClassInstanceVariableAccessorMethodIndex.instance.key, setter)
        }

    }

    /**
     * Indexes virtual getMethods from accessor getMethods
     * @param property accessor property potentially containing virtual getMethods
     * @param indexSink index sink
     */
    override fun indexAccessorProperty(property: ObjJAccessorPropertyStub, indexSink: IndexSink) {
        val className = property.containingClassName
        indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJClassMethodIndex.KEY, className)
        val getter = property.getter
        if (getter != null) {
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJClassAndSelectorMethodIndex.KEY, ObjJClassAndSelectorMethodIndex.getClassMethodKey(className, getter))
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJUnifiedMethodIndex.KEY, getter)
        }
        val setter = property.setter
        if (setter != null) {
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJClassAndSelectorMethodIndex.KEY, ObjJClassAndSelectorMethodIndex.getClassMethodKey(className, setter))
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJUnifiedMethodIndex.KEY, setter)
        }
    }

    /**
     * Indexes class declarations
     * @param stub class declaration stub
     * @param indexSink index sink
     */
    override fun indexClassDeclaration(stub: ObjJClassDeclarationStub<*>, indexSink: IndexSink) {
        if (stub.className.isEmpty()) {
            return
        }
        indexSink.occurrence<ObjJClassDeclarationElement<*>, String>(ObjJClassDeclarationsIndex.instance.key, stub.className)
        if (stub is ObjJImplementationStub) {
            indexImplementationClassDeclaration(stub, indexSink)
        } else if (stub is ObjJProtocolDeclarationStub) {
            indexSink.occurrence<ObjJProtocolDeclaration, String>(ObjJProtocolDeclarationsIndex.instance.key, stub.className)
        }
        for (protocol in stub.inheritedProtocols) {
            indexSink.occurrence<ObjJClassDeclarationElement<*>, String>(ObjJClassInheritanceIndex.instance.key, protocol)
        }
    }


    /**
     * Indexes an @implementation class including its supertype and category status
     */
    private fun indexImplementationClassDeclaration(implementationStub: ObjJImplementationStub, indexSink: IndexSink) {

        // Index class as category
        if (implementationStub.isCategory) {
            indexSink.occurrence<ObjJClassDeclarationElement<*>, String>(ObjJClassInheritanceIndex.instance.key, implementationStub.className)
            indexSink.occurrence<ObjJImplementationDeclaration, String>(ObjJImplementationCategoryDeclarationsIndex.instance.key, implementationStub.className)

        // Index superclass
        } else if (implementationStub.superClassName != null && implementationStub.superClassName != ObjJClassType.CPOBJECT) {
            indexSink.occurrence<ObjJClassDeclarationElement<*>, String>(ObjJClassInheritanceIndex.instance.key, implementationStub.superClassName!!)
        }
        // Index in implementations index
        indexSink.occurrence<ObjJImplementationDeclaration, String>(ObjJImplementationDeclarationsIndex.instance.key, implementationStub.className)
    }

    /**
     * Indexes selector literal method as possible inline method declaration
     * @param selectorLiteral selector literal
     * @param indexSink index sink
     */
    override fun indexSelectorLiteral(selectorLiteral: ObjJSelectorLiteralStub, indexSink: IndexSink) {
        indexSink.occurrence<ObjJSelectorLiteral, String>(ObjJSelectorInferredMethodIndex.instance.key, selectorLiteral.selectorString)
        val stringBuilder = StringBuilder()
        for (selector in selectorLiteral.selectorStrings) {
            stringBuilder.append(selector).append(SELECTOR_SYMBOL)
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJMethodFragmentIndex.KEY, stringBuilder.toString())
        }
    }

    /**
     * Indexes a function declaration
     */
    override fun indexFunctionDeclaration(functionDeclarationStub: ObjJFunctionDeclarationElementStub<*>, indexSink: IndexSink) {
        indexSink.occurrence<ObjJFunctionDeclarationElement<*>, String>(ObjJFunctionsIndex.instance.key, functionDeclarationStub.functionName)
    }

    /**
     * Indexes a method call
     */
    override fun indexMethodCall(methodCallStub: ObjJMethodCallStub, indexSink: IndexSink) {
        indexSink.occurrence<ObjJMethodCall, String>(ObjJMethodCallIndex.instance.key, methodCallStub.selectorString)
    }


    /**
     * Index global declaration
     * @param globalVariableDeclaration selector literal
     * @param indexSink index sink
     */
    override fun indexGlobalVariableDeclaration(globalVariableDeclaration: ObjJGlobalVariableDeclarationStub, indexSink: IndexSink) {
        // Index by file name
        if (globalVariableDeclaration.fileName != null) {
            indexSink.occurrence<ObjJGlobalVariableDeclaration, String>(ObjJGlobalVariablesByFileNameIndex.instance.key, globalVariableDeclaration.fileName!!)
        }
        // Index by variable name
        indexSink.occurrence<ObjJGlobalVariableDeclaration, String>(ObjJGlobalVariableNamesIndex.instance.key, globalVariableDeclaration.variableName)
    }

    /**
     * Indexes import calls for referencing
     */
    override fun indexImport(stub: ObjJImportStub<*>, indexSink: IndexSink) {
        // @todo implement the actual indexing
    }

    /**
     * Index variable names by scope
     */
    override fun indexVariableName(stub: ObjJVariableNameStub, indexSink: IndexSink) {
        // Ensure has containing file
        val containingFileName = ObjJPsiFileUtil.getContainingFileName(stub.psi.containingFile)
                ?: return
        // Sink in file
        indexSink.occurrence<ObjJVariableName, String>(ObjJVariableNameByScopeIndex.KEY, "$containingFileName-ALL")
        val blockRanges = stub.containingBlockRanges
        if (blockRanges.isEmpty()) {
            indexSink.occurrence<ObjJVariableName, String>(ObjJVariableNameByScopeIndex.KEY, "$containingFileName-TOP")
        }

        // Index for each containing block
        for (blockRange in blockRanges) {
            indexSink.occurrence<ObjJVariableName, String>(ObjJVariableNameByScopeIndex.KEY, ObjJVariableNameByScopeIndex.getIndexKey(containingFileName, blockRange))
        }
    }

    override fun indexPropertyName(propertyName:ObjJPropertyNameStub, indexSink: IndexSink) {
        val namespaceComponents = propertyName.namespaceComponents
        val lastIndex = namespaceComponents.size - 1
        for (i in 0 .. lastIndex) {
            val namespace = namespaceComponents.subList(i, lastIndex).joinToString(".")
            indexSink.occurrence<ObjJPropertyName, String>(ObjJPropertyNamesIndex.KEY, namespace)
        }
    }

    /**
     * Index typedefs for completion and or validation
     */
    override fun indexTypeDef(stub:ObjJTypeDefStub, indexSink: IndexSink) {
        indexSink.occurrence<ObjJTypeDef, String>(ObjJTypeDefIndex.KEY, stub.className)
    }

    override fun indexVariableDeclaration(stub:ObjJVariableDeclarationStub, indexSink: IndexSink) {
        stub.qualifiedNamesList.forEach { qualifiedName ->
            if (qualifiedName.isEmpty())
                return@forEach
            val lastIndex = qualifiedName.size - 1
            for (i in 0 .. lastIndex) {
                val namespace = qualifiedName.subList(i, lastIndex).joinToString(".") { it.name ?: "{?}" }
                indexSink.occurrence<ObjJVariableDeclaration, String>(ObjJVariableDeclarationsByNameIndex.KEY, namespace)
            }
            val last = qualifiedName.last().name ?: return@forEach
            indexSink.occurrence<ObjJVariableDeclaration, String>(ObjJVariableDeclarationsByNameIndex.KEY, last)
        }
    }

    companion object {
        private const val MAJOR_VERSION = 8
        private const val MINOR_VERSION = 0
        const val INDEX_VERSION:Int = ObjJStubVersions.SOURCE_STUB_VERSION + MAJOR_VERSION + MINOR_VERSION
        val LOGGER:Logger by lazy {
            Logger.getLogger(ObjJIndexService::class.java.simpleName)
        }
    }
}
