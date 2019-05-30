package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.IndexSink
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration

import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.stubs.interfaces.*
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil

import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.EMPTY_SELECTOR
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.SELECTOR_SYMBOL
import cappuccino.ide.intellij.plugin.psi.utils.isUniversalMethodCaller
import com.intellij.psi.stubs.PsiFileStub
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

        val selectorBuilder = StringBuilder()
        for (subSelector in methodHeaderStub.selectorStrings) {
            selectorBuilder.append(subSelector).append(SELECTOR_SYMBOL)
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJMethodFragmentIndex.KEY, selectorBuilder.toString())
        }

        val className = methodHeaderStub.containingClassName
        if (!isUniversalMethodCaller(className)) {
            try {
                indexSink.occurrence<ObjJMethodHeader, String>(ObjJClassMethodIndex.instance.key, className)
            } catch (e: Exception) {
                LOGGER.log(Level.SEVERE, "Failed to index class&selector tuple with error: ${e.localizedMessage}")
            }

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
        if (variableDeclarationStub.getter != null && variableDeclarationStub.getter!!.isNotEmpty()) {
            indexSink.occurrence<ObjJInstanceVariableDeclaration, String>(ObjJClassInstanceVariableAccessorMethodIndex.instance.key, variableDeclarationStub.getter!!)
        }
        // Index setters
        if (variableDeclarationStub.setter != null && variableDeclarationStub.setter!!.isNotEmpty()) {
            indexSink.occurrence<ObjJInstanceVariableDeclaration, String>(ObjJClassInstanceVariableAccessorMethodIndex.instance.key, variableDeclarationStub.setter!!)
        }
    }

    /**
     * Indexes virtual methods from accessor methods
     * @param property accessor property potentially containing virtual methods
     * @param indexSink index sink
     */
    override fun indexAccessorProperty(property: ObjJAccessorPropertyStub, indexSink: IndexSink) {
        if (property.getter != null) {
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJUnifiedMethodIndex.KEY, property.getter!!)
        }
        if (property.setter != null) {
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJUnifiedMethodIndex.KEY, property.setter!!)
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
     * Indexes a file
     */
    override fun indexFile(stub: PsiFileStub<*>, sink: IndexSink) {
        if (stub !is ObjJFileStub) {
            return
        }
        // Index file by name
        sink.occurrence<ObjJFile, String>(ObjJFilesByNameIndex.instance.key, stub.fileName)
    }

    /**
     * Index variable names by scope
     */
    override fun indexVariableName(stub: ObjJVariableNameStub, indexSink: IndexSink) {
        // Ensure has containing file
        val containingFileName = ObjJFileUtil.getContainingFileName(stub.psi.containingFile)
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

    /**
     * Index typedefs for completion and or validation
     */
    override fun indexTypeDef(stub:ObjJTypeDefStub, indexSink: IndexSink) {
        indexSink.occurrence<ObjJTypeDef, String>(ObjJTypeDefIndex.KEY, stub.className)
    }

    companion object {
        private const val MAJOR_VERSION = 6
        private const val MINOR_VERSION = 8
        val INDEX_VERSION:Int get() { return (MAJOR_VERSION + MINOR_VERSION) }
        val LOGGER:Logger by lazy {
            Logger.getLogger(ObjJIndexService::class.java.simpleName)
        }
    }
}
