package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.ObjJVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.EMPTY_SELECTOR
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.SELECTOR_SYMBOL
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiFileUtil
import cappuccino.ide.intellij.plugin.psi.utils.fileNameAsImportString
import cappuccino.ide.intellij.plugin.stubs.ObjJStubVersions
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJPropertyNameStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.*
import com.intellij.psi.stubs.IndexSink
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
            indexSink.occurrence(ObjJUnifiedMethodIndex.KEY, selector)
        } catch (e: Exception) {
           //LOGGER.severe("Failed to index selector with error: ${e.localizedMessage}")
        }
        val className = methodHeaderStub.containingClassName
        indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJClassAndSelectorMethodIndex.KEY, ObjJClassAndSelectorMethodIndex.getClassMethodKey(className, selector))

        val selectorBuilder = StringBuilder()
        for (subSelector in methodHeaderStub.selectorStrings) {
            selectorBuilder.append(subSelector).append(SELECTOR_SYMBOL)
            val currentSelector = selectorBuilder.toString()
            indexSink.occurrence(ObjJMethodFragmentIndex.KEY, currentSelector)
        }

        try {
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJClassMethodIndex.KEY, className)
        } catch (e: Exception) {
           //LOGGER.severe("Failed to index class&selector tuple with error: ${e.localizedMessage}")
        }

    }

    /**
     * Indexes instance variable stubs
     * @param variableDeclarationStub instance variable stub to index
     * @param indexSink index sink
     */
    override fun indexInstanceVariable(variableDeclarationStub: ObjJInstanceVariableDeclarationStub, indexSink: IndexSink) {

        // Index Instance variables to class name
        indexSink.occurrence(ObjJInstanceVariablesByClassIndex.instance.key, variableDeclarationStub.containingClass)

        // Index variables by variable name
        indexSink.occurrence(ObjJInstanceVariablesByNameIndex.instance.key, variableDeclarationStub.variableName)

        // Index Getter accessors
        val getter = variableDeclarationStub.getter
        if (getter != null && getter.isNotBlank()) {
            indexSink.occurrence(ObjJClassInstanceVariableAccessorMethodIndex.instance.key, getter)
        }
        // Index setters
        val setter = variableDeclarationStub.setter
        if (setter != null && setter.isNotBlank()) {
            indexSink.occurrence(ObjJClassInstanceVariableAccessorMethodIndex.instance.key, setter)
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
            indexSink.occurrence(ObjJUnifiedMethodIndex.KEY, getter)
        }
        val setter = property.setter
        if (setter != null) {
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJClassAndSelectorMethodIndex.KEY, ObjJClassAndSelectorMethodIndex.getClassMethodKey(className, setter))
            indexSink.occurrence(ObjJUnifiedMethodIndex.KEY, setter)
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
        indexSink.occurrence(ObjJClassDeclarationsIndex.instance.key, stub.className)
        val fileImportString = (stub.psi.containingFile as? ObjJFile)?.fileNameAsImportString
        if (fileImportString != null)
            indexSink.occurrence(ObjJClassDeclarationsByFileImportStringIndex.instance.key, fileImportString)
        if (stub is ObjJImplementationStub) {
            indexImplementationClassDeclaration(stub, indexSink)
        } else if (stub is ObjJProtocolDeclarationStub) {
            indexSink.occurrence(ObjJProtocolDeclarationsIndex.instance.key, stub.className)
        }
        for (protocol in stub.inheritedProtocols) {
            indexSink.occurrence(ObjJClassInheritanceIndex.instance.key, protocol)
        }
    }


    /**
     * Indexes an @implementation class including its supertype and category status
     */
    private fun indexImplementationClassDeclaration(implementationStub: ObjJImplementationStub, indexSink: IndexSink) {

        // Index class as category
        if (implementationStub.isCategory) {
            indexSink.occurrence(ObjJClassInheritanceIndex.instance.key, implementationStub.className)
            indexSink.occurrence(ObjJImplementationCategoryDeclarationsIndex.instance.key, implementationStub.className)

        // Index superclass
        } else if (implementationStub.superClassName != null && implementationStub.superClassName != ObjJClassType.CPOBJECT) {
            indexSink.occurrence(ObjJClassInheritanceIndex.instance.key, implementationStub.superClassName!!)
        }
        // Index in implementations index
        indexSink.occurrence(ObjJImplementationDeclarationsIndex.instance.key, implementationStub.className)
    }

    /**
     * Indexes selector literal method as possible inline method declaration
     * @param selectorLiteral selector literal
     * @param indexSink index sink
     */
    override fun indexSelectorLiteral(selectorLiteral: ObjJSelectorLiteralStub, indexSink: IndexSink) {
        indexSink.occurrence(ObjJSelectorInferredMethodIndex.instance.key, selectorLiteral.selectorString)
        val stringBuilder = StringBuilder()
        for (selector in selectorLiteral.selectorStrings) {
            stringBuilder.append(selector).append(SELECTOR_SYMBOL)
            indexSink.occurrence(ObjJMethodFragmentIndex.KEY, stringBuilder.toString())
        }
    }

    /**
     * Indexes a function declaration
     */
    override fun indexFunctionDeclaration(functionDeclarationStub: ObjJFunctionDeclarationElementStub<*>, indexSink: IndexSink) {
        indexSink.occurrence(ObjJFunctionsIndex.instance.key, functionDeclarationStub.functionName)
    }

    /**
     * Indexes a method call
     */
    override fun indexMethodCall(methodCallStub: ObjJMethodCallStub, indexSink: IndexSink) {
        indexSink.occurrence(ObjJMethodCallIndex.instance.key, methodCallStub.selectorString)
    }


    /**
     * Index global declaration
     * @param globalVariableDeclaration selector literal
     * @param indexSink index sink
     */
    override fun indexGlobalVariableDeclaration(globalVariableDeclaration: ObjJGlobalVariableDeclarationStub, indexSink: IndexSink) {
        // Index by file name
        if (globalVariableDeclaration.fileName != null) {
            indexSink.occurrence(ObjJGlobalVariablesByFileNameIndex.instance.key, globalVariableDeclaration.fileName!!)
        }
        // Index by variable name
        indexSink.occurrence(ObjJGlobalVariableNamesIndex.instance.key, globalVariableDeclaration.variableName)
    }

    /**
     * Indexes import calls for referencing
     */
    override fun indexImport(stub: ObjJImportStub<*>, indexSink: IndexSink) {
        indexSink.occurrence(ObjJImportsIndex.KEY, stub.fileName)
        indexSink.occurrence(ObjJImportInstancesIndex.KEY, stub.framework+"/"+stub.fileName)
    }

    /**
     * Index variable names by scope
     */
    override fun indexVariableName(stub: ObjJVariableNameStub, indexSink: IndexSink) {
        // Ensure has containing file
        val containingFile = stub.psi.containingFile
        val variableName = stub.variableName
        val blockRanges = stub.containingBlockRanges
        if (blockRanges.isEmpty()) {
            val range = containingFile.textRange
            indexSink.occurrence<ObjJVariableName, ObjJRangeKey>(ObjJVariableNameByScopeIndex.KEY, ObjJRangeKey(variableName, range.startOffset, range.endOffset))
        }
        // Index for each containing block
        for (blockRange in blockRanges) {
            indexSink.occurrence<ObjJVariableName, ObjJRangeKey>(ObjJVariableNameByScopeIndex.KEY, ObjJRangeKey(variableName, blockRange.first, blockRange.second))
        }
    }

    override fun indexPropertyName(propertyName:ObjJPropertyNameStub, indexSink: IndexSink) {
        val namespaceComponents = propertyName.namespaceComponents
        val lastIndex = namespaceComponents.size - 1
        for (i in 0 .. lastIndex) {
            val namespace = namespaceComponents.subList(i, lastIndex).joinToString(".")
            indexSink.occurrence(ObjJPropertyNamesIndex.KEY, namespace)
        }
    }

    /**
     * Index typedefs for completion and or validation
     */
    override fun indexTypeDef(stub:ObjJTypeDefStub, indexSink: IndexSink) {
        indexSink.occurrence(ObjJTypeDefIndex.KEY, stub.className)
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

    override fun indexFile(stub:ObjJFileStub?, indexSink: IndexSink) {
        if (stub == null)
            return
        //val framework = stub.framework ?: return
        //LOGGER.info("Indexing File: <$framework/${stub.fileName}>")
        //indexSink.occurrence(ObjJFilesByFrameworkIndex.KEY, framework);
        //indexSink.occurrence(ObjJFilesByFrameworkAndFileNameIndex.KEY, ObjJFilesByFrameworkAndFileNameIndex.getIndexKey(framework, stub.fileName))
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
