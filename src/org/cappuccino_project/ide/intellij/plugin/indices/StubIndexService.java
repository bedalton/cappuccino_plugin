package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.stubs.IndexSink;

import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJBlock;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJBlockPsiUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.stubs.ObjJStubVersions;
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJFileStubImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.*;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StubIndexService {

    static final int INDEX_VERSION = ObjJStubVersions.SOURCE_STUB_VERSION;
    private final Logger LOGGER = Logger.getLogger("#org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService");
    /**
     * Emtpy service implementation
     */
    private static final StubIndexService EMPTY_SERVICE = new StubIndexService();


    StubIndexService() {}

    public static StubIndexService getInstance() {
        final ObjJIndexService service = ServiceManager.getService(ObjJIndexService.class);
        return service != null ? service : EMPTY_SERVICE;
    }

    public void indexMethod(ObjJMethodHeaderStub methodHeaderStub, IndexSink indexSink) {
        LOGGER.log(Level.WARNING, "Using empty stub indexes <indexMethod()> method");
    }

    public void indexMethodCall(ObjJMethodCallStub methodHeaderStub, IndexSink indexSink) {
        LOGGER.log(Level.WARNING, "Using empty stub indexes <indexMethodCall()> method");
    }

    public void indexClassDeclaration(ObjJClassDeclarationStub stub, IndexSink indexSink) {
        LOGGER.log(Level.WARNING, "Using empty stub indexes <indexImplementationClassDeclaration()> method");
    }

    public void indexFile(ObjJFileStub stub, IndexSink sink) {

    }

    public void indexAccessorProperty(@NotNull
                                              ObjJAccessorPropertyStub property, IndexSink indexSink) {
        LOGGER.log(Level.WARNING, "Using empty stub indexes <indexAccessorProperty()> method");
    }

    public void indexInstanceVariable(@NotNull
                                              ObjJInstanceVariableDeclarationStub variableDeclarationStub, IndexSink indexSink) {
        LOGGER.log(Level.WARNING, "Using empty stub indexes <indexInstanceVariable()> method");
    }

    public void indexFunctionDeclaration(@NotNull
                                              ObjJFunctionDeclarationElementStub variableDeclarationStub, IndexSink indexSink) {
        LOGGER.log(Level.WARNING, "Using empty stub indexes <indexFunctionDeclaration()> method");
    }

    /**
     * Indexes selector literal method as possible inline method declaration
     * @param selectorLiteral selector literal
     * @param indexSink index sink
     */
    public void indexSelectorLiteral(@NotNull
                                             ObjJSelectorLiteralStub selectorLiteral, @NotNull IndexSink indexSink) {

        LOGGER.log(Level.WARNING, "Using empty stub index method <indexSelectorLiteral()>");
    }

    /**
     * Index global declaration
     * @param globalVariableDeclaration selector literal
     * @param indexSink index sink
     */
    public void indexGlobalVariableDeclaration(@NotNull
                                                       ObjJGlobalVariableDeclarationStub globalVariableDeclaration, @NotNull IndexSink indexSink) {

        LOGGER.log(Level.WARNING, "Using empty stub index method <globalVariableDeclaration()>");
    }

    public void indexImport(@NotNull ObjJImportStub stub, IndexSink indexSink) {
        LOGGER.log(Level.WARNING, "Using empty stub index method <indexImport()>");
    }

    public void indexVarTypeId(@NotNull ObjJVarTypeIdStub stub, IndexSink indexSink) {

    }

    public void indexVariableName(@NotNull ObjJVariableNameStub stub, @NotNull IndexSink indexSink) {

    }


    // ============================== //
    // ========== File Stub ========= //
    // ============================== //

    @NotNull
    public ObjJFileStub createFileStub(@NotNull
                                               ObjJFile file) {
        final String fileName = ObjJFileUtil.getContainingFileName(file);
        final List<String> fileImportStrings = file.getImportStrings();
        return new ObjJFileStubImpl(file, fileName != null ? fileName : "{?}", fileImportStrings);
    }

}
