package cappuccino.ide.intellij.plugin.indices

import com.intellij.psi.stubs.IndexSink
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration

import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.stubs.interfaces.*
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil

import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.EMPTY_SELECTOR
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.SELECTOR_SYMBOL
import cappuccino.ide.intellij.plugin.psi.utils.isUniversalMethodCaller
import com.intellij.psi.stubs.PsiFileStub

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
            //LOGGER.log(Level.SEVERE, "Method header has no selector to index");
            return
        }
        if (selector.isEmpty()) {
            //LOGGER.log(Level.SEVERE, "Method stub returned with an empty selector");
            return
        }


        try {
            //LOGGER.log(Level.INFO,"Indexing Method - ["+methodHeaderStub.getContainingClassName()+" "+selector + "]");
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJUnifiedMethodIndex.KEY, selector)
            //LOGGER.log(Level.INFO,"Indexed Method - ["+methodHeaderStub.getContainingClassName()+" "+selector + "]");
        } catch (e: Exception) {
            //LOGGER.log(Level.SEVERE, "Failed to index selector with error: "+e.getLocalizedMessage());
        }

        val selectorBuilder = StringBuilder()
        for (subSelector in methodHeaderStub.selectorStrings) {
            selectorBuilder.append(subSelector).append(ObjJMethodPsiUtils.SELECTOR_SYMBOL)
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJMethodFragmentIndex.KEY, selectorBuilder.toString())
        }

        val className = methodHeaderStub.containingClassName
        if (!isUniversalMethodCaller(className)) {
            try {
                indexSink.occurrence<ObjJMethodHeader, String>(ObjJClassMethodIndex.instance.key, className)
            } catch (e: Exception) {
                //LOGGER.log(Level.SEVERE, "Failed to index class&selector tuple with error: "+e.getLocalizedMessage());
            }

        }
    }

    /**
     * Indexes instance variable stubs
     * @param variableDeclarationStub instance variable stub to index
     * @param indexSink index sink
     */
    override fun indexInstanceVariable(variableDeclarationStub: ObjJInstanceVariableDeclarationStub, indexSink: IndexSink) {
        indexSink.occurrence<ObjJInstanceVariableDeclaration, String>(ObjJInstanceVariablesByClassIndex.instance.key, variableDeclarationStub.containingClass)
        indexSink.occurrence<ObjJInstanceVariableDeclaration, String>(ObjJInstanceVariablesByNameIndex.instance.key, variableDeclarationStub.variableName)
        //   //LOGGER.log(Level.INFO, "Indexing instance variable <"+variableDeclarationStub.getVariableName()+"> for class <"+variableDeclarationStub.getContainingClass()+">");
        if (variableDeclarationStub.getter != null && !variableDeclarationStub.getter!!.isEmpty()) {
            indexSink.occurrence<ObjJInstanceVariableDeclaration, String>(ObjJClassInstanceVariableAccessorMethodIndex.instance.key, variableDeclarationStub.getter!!)
        }
        if (variableDeclarationStub.setter != null && !variableDeclarationStub.setter!!.isEmpty()) {
            indexSink.occurrence<ObjJInstanceVariableDeclaration, String>(ObjJClassInstanceVariableAccessorMethodIndex.instance.key, variableDeclarationStub.setter!!)
        }
    }

    /**
     * Indexes virtual methods from accessor methods
     * @param property accessor property potentially containing virtual methods
     * @param indexSink index sink
     */
    override fun indexAccessorProperty(property: ObjJAccessorPropertyStub, indexSink: IndexSink) {
        //LOGGER.log(Level.INFO, "Indexing Accessor Property for var <"+property.getContainingClassName()+":"+property.getVariableName()+">");
        if (property.getter != null) {
            //LOGGER.log(Level.INFO, "ObjJIndex: Indexing accessorProperty ["+property.getContainingClass()+" "+ property.getGetter() + "]");
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJUnifiedMethodIndex.KEY, property.getter!!)
        }
        if (property.setter != null) {
            //LOGGER.log(Level.INFO, "ObjJIndex: Indexing accessorProperty ["+property.getContainingClass()+" "+ property.getSetter() + "]");
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
            //   //LOGGER.log(Level.INFO, "ClassName is empty in class declaration for index");
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



    private fun indexImplementationClassDeclaration(implementationStub: ObjJImplementationStub, indexSink: IndexSink) {
        if (implementationStub.isCategory) {
            indexSink.occurrence<ObjJClassDeclarationElement<*>, String>(ObjJClassInheritanceIndex.instance.key, implementationStub.className)
            indexSink.occurrence<ObjJImplementationDeclaration, String>(ObjJImplementationCategoryDeclarationsIndex.instance.key, implementationStub.className)
        } else if (implementationStub.superClassName != null && implementationStub.superClassName != ObjJClassType.CPOBJECT) {
            //   //LOGGER.log(Level.INFO, "Setting super class to: " + implementationStub.getSuperClassName());
            indexSink.occurrence<ObjJClassDeclarationElement<*>, String>(ObjJClassInheritanceIndex.instance.key, implementationStub.superClassName!!)
        }
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
            stringBuilder.append(selector).append(ObjJMethodPsiUtils.SELECTOR_SYMBOL)
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJMethodFragmentIndex.KEY, stringBuilder.toString())
        }
    }

    override fun indexFunctionDeclaration(variableDeclarationStub: ObjJFunctionDeclarationElementStub<*>, indexSink: IndexSink) {
        //LOGGER.log(Level.INFO, "Indexing function: <"+functionDeclaration.getFunctionName()+">");
        indexSink.occurrence<ObjJFunctionDeclarationElement<*>, String>(ObjJFunctionsIndex.instance.key, variableDeclarationStub.functionName)
        //LOGGER.log(Level.INFO, "Did Index function: <"+functionDeclaration.getFunctionName()+">");
    }

    override fun indexMethodCall(methodHeaderStub: ObjJMethodCallStub, indexSink: IndexSink) {
        //LOGGER.log(Level.INFO, "Indexing method call  <["+methodHeaderStub.getCallTarget()+" "+methodHeaderStub.getSelectorString()+"]>");
        indexSink.occurrence<ObjJMethodCall, String>(ObjJMethodCallIndex.instance.key, methodHeaderStub.selectorString)
        //LOGGER.log(Level.INFO, "Indexed method call.");
    }


    /**
     * Index global declaration
     * @param globalVariableDeclaration selector literal
     * @param indexSink index sink
     */
    override fun indexGlobalVariableDeclaration(globalVariableDeclaration: ObjJGlobalVariableDeclarationStub, indexSink: IndexSink) {
        if (globalVariableDeclaration.fileName != null) {
            indexSink.occurrence<ObjJGlobalVariableDeclaration, String>(ObjJGlobalVariablesByFileNameIndex.instance.key, globalVariableDeclaration.fileName!!)
        }
        indexSink.occurrence<ObjJGlobalVariableDeclaration, String>(ObjJGlobalVariableNamesIndex.instance.key, globalVariableDeclaration.variableName)
    }

    override fun indexImport(stub: ObjJImportStub<*>, indexSink: IndexSink) {

    }

    override fun indexFile(stub: PsiFileStub<*>, sink: IndexSink) {
        //LOGGER.log(Level.INFO, "Indexing file by name: "+fileStub.getFileName());
        if (stub !is ObjJFileStub) {
            return
        }
        sink.occurrence<ObjJFile, String>(ObjJFilesByNameIndex.instance.key, stub.fileName)
    }

    override fun indexVarTypeId(stub: ObjJVarTypeIdStub, indexSink: IndexSink) {

    }

    override fun indexVariableName(stub: ObjJVariableNameStub, indexSink: IndexSink) {
        val containingFileName = ObjJFileUtil.getContainingFileName(stub.psi.containingFile)
                ?: //LOGGER.log(Level.SEVERE, "Cannot index variable name, containing file name is null");
                return
// else {
        //LOGGER.log(Level.INFO, "Indexing variable: "+containingFileName+": "+stub.getVariableName());
        //}
        indexSink.occurrence<ObjJVariableName, String>(ObjJVariableNameByScopeIndex.KEY, "$containingFileName-ALL")
        val blockRanges = stub.containingBlockRanges
        if (blockRanges.isEmpty()) {
            indexSink.occurrence<ObjJVariableName, String>(ObjJVariableNameByScopeIndex.KEY, "$containingFileName-TOP")
        }

        for (blockRange in blockRanges) {
            indexSink.occurrence<ObjJVariableName, String>(ObjJVariableNameByScopeIndex.KEY, ObjJVariableNameByScopeIndex.getIndexKey(containingFileName, blockRange))
        }
    }

    override fun indexTypeDef(stub:ObjJTypeDefStub, indexSink: IndexSink) {

    }

    companion object {
        const val INDEX_VERSION = 1
    }
}
