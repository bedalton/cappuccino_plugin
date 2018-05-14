package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink

import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.stubs.ObjJStubVersions
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJFileStubImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.*
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil
import java.util.logging.Level
import java.util.logging.Logger

open class StubIndexService internal constructor() {
    private val LOGGER = Logger.getLogger("#org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService")

    open fun indexMethod(methodHeaderStub: ObjJMethodHeaderStub, indexSink: IndexSink) {
        LOGGER.log(Level.WARNING, "Using empty stub indexes <indexMethod()> method")
    }

    open fun indexMethodCall(methodHeaderStub: ObjJMethodCallStub, indexSink: IndexSink) {
        LOGGER.log(Level.WARNING, "Using empty stub indexes <indexMethodCall()> method")
    }

    open fun indexClassDeclaration(stub: ObjJClassDeclarationStub<*>, indexSink: IndexSink) {
        LOGGER.log(Level.WARNING, "Using empty stub indexes <indexImplementationClassDeclaration()> method")
    }

    open fun indexFile(stub: ObjJFileStub, sink: IndexSink) {

    }

    open fun indexAccessorProperty(property: ObjJAccessorPropertyStub, indexSink: IndexSink) {
        LOGGER.log(Level.WARNING, "Using empty stub indexes <indexAccessorProperty()> method")
    }

    open fun indexInstanceVariable(variableDeclarationStub: ObjJInstanceVariableDeclarationStub, indexSink: IndexSink) {
        LOGGER.log(Level.WARNING, "Using empty stub indexes <indexInstanceVariable()> method")
    }

    open fun indexFunctionDeclaration(variableDeclarationStub: ObjJFunctionDeclarationElementStub<*>, indexSink: IndexSink) {
        LOGGER.log(Level.WARNING, "Using empty stub indexes <indexFunctionDeclaration()> method")
    }

    /**
     * Indexes selector literal method as possible inline method declaration
     * @param selectorLiteral selector literal
     * @param indexSink index sink
     */
    open fun indexSelectorLiteral(selectorLiteral: ObjJSelectorLiteralStub, indexSink: IndexSink) {

        LOGGER.log(Level.WARNING, "Using empty stub index method <indexSelectorLiteral()>")
    }

    /**
     * Index global declaration
     * @param globalVariableDeclaration selector literal
     * @param indexSink index sink
     */
    open fun indexGlobalVariableDeclaration(globalVariableDeclaration: ObjJGlobalVariableDeclarationStub, indexSink: IndexSink) {

        LOGGER.log(Level.WARNING, "Using empty stub index method <globalVariableDeclaration()>")
    }

    open fun indexImport(stub: ObjJImportStub<*>, indexSink: IndexSink) {
        LOGGER.log(Level.WARNING, "Using empty stub index method <indexImport()>")
    }

    open fun indexVarTypeId(stub: ObjJVarTypeIdStub, indexSink: IndexSink) {

    }

    open fun indexVariableName(stub: ObjJVariableNameStub, indexSink: IndexSink) {

    }


    // ============================== //
    // ========== File Stub ========= //
    // ============================== //

    fun createFileStub(file: ObjJFile): ObjJFileStub {
        val fileName = ObjJFileUtil.getContainingFileName(file)
        val fileImportStrings = file.importStrings
        return ObjJFileStubImpl(file, fileName ?: "{?}", fileImportStrings)
    }

    companion object {

        internal val INDEX_VERSION = ObjJStubVersions.SOURCE_STUB_VERSION
        /**
         * Emtpy service implementation
         */
        private val EMPTY_SERVICE = StubIndexService()

        val instance: StubIndexService
            get() {
                val service = ServiceManager.getService(ObjJIndexService::class.java)
                return service ?: EMPTY_SERVICE
            }
    }

}
