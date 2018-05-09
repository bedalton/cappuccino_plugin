package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.psi.stubs.IndexSink
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration

import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.*
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil

import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.EMPTY_SELECTOR
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.SELECTOR_SYMBOL
import org.cappuccino_project.ide.intellij.plugin.psi.utils.isUniversalMethodCaller

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
                indexSink.occurrence<ObjJMethodHeader, String>(ObjJClassMethodIndex.instance.getKey(), className)
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
        indexSink.occurrence<ObjJInstanceVariableDeclaration, String>(ObjJInstanceVariablesByClassIndex.instance.getKey(), variableDeclarationStub.containingClass)
        indexSink.occurrence<ObjJInstanceVariableDeclaration, String>(ObjJInstanceVariablesByNameIndex.instance.getKey(), variableDeclarationStub.variableName)
        //   //LOGGER.log(Level.INFO, "Indexing instance variable <"+variableDeclarationStub.getVariableName()+"> for class <"+variableDeclarationStub.getContainingClass()+">");
        if (variableDeclarationStub.getter != null && !variableDeclarationStub.getter!!.isEmpty()) {
            indexSink.occurrence<ObjJInstanceVariableDeclaration, String>(ObjJClassInstanceVariableAccessorMethodIndex.instance.getKey(), variableDeclarationStub.getter!!)
        }
        if (variableDeclarationStub.setter != null && !variableDeclarationStub.setter!!.isEmpty()) {
            indexSink.occurrence<ObjJInstanceVariableDeclaration, String>(ObjJClassInstanceVariableAccessorMethodIndex.instance.getKey(), variableDeclarationStub.setter!!)
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
        indexSink.occurrence<ObjJClassDeclarationElement<*>, String>(ObjJClassDeclarationsIndex.instance.getKey(), stub.className)
        if (stub is ObjJImplementationStub) {
            indexImplementationClassDeclaration(stub, indexSink)
        } else if (stub is ObjJProtocolDeclarationStub) {
            indexSink.occurrence<ObjJProtocolDeclaration, String>(ObjJProtocolDeclarationsIndex.instance.getKey(), stub.className)
        }
        for (protocol in stub.inheritedProtocols) {
            indexSink.occurrence<ObjJClassDeclarationElement<*>, String>(ObjJClassInheritanceIndex.instance.getKey(), protocol)
        }
    }

    private fun indexImplementationClassDeclaration(implementationStub: ObjJImplementationStub, indexSink: IndexSink) {
        if (implementationStub.isCategory) {
            indexSink.occurrence<ObjJClassDeclarationElement<*>, String>(ObjJClassInheritanceIndex.instance.getKey(), implementationStub.className)
            indexSink.occurrence<ObjJImplementationDeclaration, String>(ObjJImplementationCategoryDeclarationsIndex.instance.getKey(), implementationStub.className)
        } else if (implementationStub.superClassName != null && implementationStub.superClassName != ObjJClassType.CPOBJECT) {
            //   //LOGGER.log(Level.INFO, "Setting super class to: " + implementationStub.getSuperClassName());
            indexSink.occurrence<ObjJClassDeclarationElement<*>, String>(ObjJClassInheritanceIndex.instance.getKey(), implementationStub.superClassName!!)
        }
        indexSink.occurrence<ObjJImplementationDeclaration, String>(ObjJImplementationDeclarationsIndex.instance.getKey(), implementationStub.className)
    }

    /**
     * Indexes selector literal method as possible inline method declaration
     * @param selectorLiteral selector literal
     * @param indexSink index sink
     */
    override fun indexSelectorLiteral(selectorLiteral: ObjJSelectorLiteralStub, indexSink: IndexSink) {
        indexSink.occurrence<ObjJSelectorLiteral, String>(ObjJSelectorInferredMethodIndex.instance.getKey(), selectorLiteral.selectorString)
        val stringBuilder = StringBuilder()
        for (selector in selectorLiteral.selectorStrings) {
            stringBuilder.append(selector).append(ObjJMethodPsiUtils.SELECTOR_SYMBOL)
            indexSink.occurrence<ObjJMethodHeaderDeclaration<*>, String>(ObjJMethodFragmentIndex.KEY, stringBuilder.toString())
        }
    }

    override fun indexFunctionDeclaration(variableDeclarationStub: ObjJFunctionDeclarationElementStub<*>, indexSink: IndexSink) {
        //LOGGER.log(Level.INFO, "Indexing function: <"+functionDeclaration.getFunctionName()+">");
        indexSink.occurrence<ObjJFunctionDeclarationElement<*>, String>(ObjJFunctionsIndex.instance.getKey(), variableDeclarationStub.functionName)
        //LOGGER.log(Level.INFO, "Did Index function: <"+functionDeclaration.getFunctionName()+">");
    }

    override fun indexMethodCall(methodHeaderStub: ObjJMethodCallStub, indexSink: IndexSink) {
        //LOGGER.log(Level.INFO, "Indexing method call  <["+methodHeaderStub.getCallTarget()+" "+methodHeaderStub.getSelectorString()+"]>");
        indexSink.occurrence<ObjJMethodCall, String>(ObjJMethodCallIndex.instance.getKey(), methodHeaderStub.selectorString)
        //LOGGER.log(Level.INFO, "Indexed method call.");
    }


    /**
     * Index global declaration
     * @param globalVariableDeclaration selector literal
     * @param indexSink index sink
     */
    override fun indexGlobalVariableDeclaration(globalVariableDeclaration: ObjJGlobalVariableDeclarationStub, indexSink: IndexSink) {
        if (globalVariableDeclaration.fileName != null) {
            indexSink.occurrence<ObjJGlobalVariableDeclaration, String>(ObjJGlobalVariablesByFileNameIndex.instance.getKey(), globalVariableDeclaration.fileName!!)
        }
        indexSink.occurrence<ObjJGlobalVariableDeclaration, String>(ObjJGlobalVariableNamesIndex.instance.getKey(), globalVariableDeclaration.variableName)
    }

    override fun indexImport(stub: ObjJImportStub<*>, indexSink: IndexSink) {

    }

    override fun indexFile(stub: ObjJFileStub, sink: IndexSink) {
        //LOGGER.log(Level.INFO, "Indexing file by name: "+fileStub.getFileName());
        sink.occurrence<ObjJFile, String>(ObjJFilesByNameIndex.instance.getKey(), stub.fileName)
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

    companion object {
        const val INDEX_VERSION = 1
    }
}
