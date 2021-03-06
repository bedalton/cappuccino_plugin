package cappuccino.ide.intellij.plugin.indices

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiFileUtil
import cappuccino.ide.intellij.plugin.psi.utils.collectImports
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJFileStubImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJImportInfoStub
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJPropertyNameStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.*
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import java.util.logging.Logger

open class StubIndexService internal constructor() {

    open fun indexMethod(methodHeaderStub: ObjJMethodHeaderStub, indexSink: IndexSink) {
       //LOGGER.warning("Using empty stub indexes <indexMethod()> method")
    }

    open fun indexMethodCall(methodCallStub: ObjJMethodCallStub, indexSink: IndexSink) {
       //LOGGER.warning("Using empty stub indexes <indexMethodCall()> method")
    }

    open fun indexClassDeclaration(stub: ObjJClassDeclarationStub<*>, indexSink: IndexSink) {
       //LOGGER.warning("Using empty stub indexes <indexImplementationClassDeclaration()> method")
    }

    open fun indexAccessorProperty(property: ObjJAccessorPropertyStub, indexSink: IndexSink) {
       //LOGGER.warning("Using empty stub indexes <indexAccessorProperty()> method")
    }

    open fun indexInstanceVariable(variableDeclarationStub: ObjJInstanceVariableDeclarationStub, indexSink: IndexSink) {
       //LOGGER.warning("Using empty stub indexes <indexInstanceVariable()> method")
    }

    open fun indexFunctionDeclaration(functionDeclarationStub: ObjJFunctionDeclarationElementStub<*>, indexSink: IndexSink) {
       //LOGGER.warning("Using empty stub indexes <indexFunctionDeclaration()> method")
    }

    /**
     * Indexes selector literal method as possible inline method declaration
     * @param selectorLiteral selector literal
     * @param indexSink index sink
     */
    open fun indexSelectorLiteral(selectorLiteral: ObjJSelectorLiteralStub, indexSink: IndexSink) {

       //LOGGER.warning("Using empty stub index method <indexSelectorLiteral()>")
    }

    /**
     * Index global declaration
     * @param globalVariableDeclaration selector literal
     * @param indexSink index sink
     */
    open fun indexGlobalVariableDeclaration(globalVariableDeclaration: ObjJGlobalVariableDeclarationStub, indexSink: IndexSink) {

       //LOGGER.warning("Using empty stub index method <globalVariableDeclaration()>")
    }

    open fun indexImport(stub: ObjJImportStub<*>, indexSink: IndexSink) {
       //LOGGER.warning("Using empty stub index method <indexImport()>")
    }

    open fun indexVariableName(stub: ObjJVariableNameStub, indexSink: IndexSink) {

    }

    open fun indexTypeDef(stub:ObjJTypeDefStub, indexSink: IndexSink) {

    }

    open fun indexPropertyName(propertyName: ObjJPropertyNameStub, indexSink: IndexSink) {
        throw NotImplementedError("indexPropertyName should have been overridden")
    }


    open fun indexVariableDeclaration(stub:ObjJVariableDeclarationStub, indexSink: IndexSink) {
        throw NotImplementedError("indexVariableDeclaration should have been overridden")
    }

    // ============================== //
    // ========== File Stub ========= //
    // ============================== //

    fun createFileStub(file: ObjJFile): ObjJFileStub {
        val fileName = ObjJPsiFileUtil.getContainingFileName(file)
        val  fileImportStrings = collectImports(file).map {
            ObjJImportInfoStub(it.frameworkNameString, it.fileNameString)
        }
        return ObjJFileStubImpl(file, fileName, fileImportStrings)
    }

   open fun indexFile(stub:ObjJFileStub?, indexSink: IndexSink) {
        throw NotImplementedError("Index file must be overridden")
    }

    companion object {
        private val LOGGER by lazy {
            Logger.getLogger("#cappuccino.ide.intellij.plugin.indices.StubIndexService")
        }
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
