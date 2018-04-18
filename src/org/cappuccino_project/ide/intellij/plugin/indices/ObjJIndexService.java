package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.stubs.IndexSink;

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJBlock;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodCallPsiUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJProtocolDeclarationPsiUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.*;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.EMPTY_SELECTOR;
import static org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.SELECTOR_SYMBOL;

public class ObjJIndexService extends StubIndexService {

    private static final Logger LOGGER = Logger.getLogger("#objj.ObjJIndexService");

    ObjJIndexService() {
     //   Logger.getGlobal().log(Level.INFO, "Creating ObjJIndexService");
    }


    /**
     * Index method header stubs
     * @param methodHeaderStub method header stub to index
     * @param indexSink index sink
     */
    @Override
    public void indexMethod(ObjJMethodHeaderStub methodHeaderStub, IndexSink indexSink) {
        final String selector = methodHeaderStub.getSelectorString();
        if (selector.equals(EMPTY_SELECTOR) || selector.equals(EMPTY_SELECTOR + SELECTOR_SYMBOL)) {
            LOGGER.log(Level.SEVERE, "Method header has no selector to index");
            return;
        }
        if (selector.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Method stub returned with an empty selector");
            return;
        }


        try {
            LOGGER.log(Level.INFO,"Indexing Method - ["+methodHeaderStub.getContainingClassName()+" "+selector + "]");
            indexSink.occurrence(ObjJUnifiedMethodIndex.KEY, selector);
            LOGGER.log(Level.INFO,"Indexed Method - ["+methodHeaderStub.getContainingClassName()+" "+selector + "]");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to index selector with error: "+e.getLocalizedMessage());
        }

        final String className = methodHeaderStub.getContainingClassName();
        if (!ObjJMethodCallPsiUtil.isUniversalMethodCaller(className)) {
            try {
                indexSink.occurrence(ObjJClassMethodIndex.getInstance().getKey(), className);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to index class&selector tuple with error: "+e.getLocalizedMessage());
            }
        }
    }

    /**
     * Indexes instance variable stubs
     * @param variableDeclarationStub instance variable stub to index
     * @param indexSink index sink
     */
    public void indexInstanceVariable(@NotNull
                                              ObjJInstanceVariableDeclarationStub variableDeclarationStub, IndexSink indexSink) {
        indexSink.occurrence(ObjJInstanceVariablesByClassIndex.getInstance().getKey(), variableDeclarationStub.getContainingClass());
        indexSink.occurrence(ObjJInstanceVariablesByNameIndex.getInstance().getKey(), variableDeclarationStub.getVariableName());
     //   LOGGER.log(Level.INFO, "Indexing instance variable <"+variableDeclarationStub.getVariableName()+"> for class <"+variableDeclarationStub.getContainingClass()+">");
        if (variableDeclarationStub.getGetter() != null && !variableDeclarationStub.getGetter().isEmpty()) {
            indexSink.occurrence(ObjJClassInstanceVariableAccessorMethodIndex.getInstance().getKey(), variableDeclarationStub.getGetter());
        }
        if (variableDeclarationStub.getSetter() != null && !variableDeclarationStub.getSetter().isEmpty()) {
            indexSink.occurrence(ObjJClassInstanceVariableAccessorMethodIndex.getInstance().getKey(), variableDeclarationStub.getSetter());
        }
    }

    /**
     * Indexes virtual methods from accessor methods
     * @param property accessor property potentially containing virtual methods
     * @param indexSink index sink
     */
    @Override
    public void indexAccessorProperty(@NotNull
                                              ObjJAccessorPropertyStub property, IndexSink indexSink) {
        LOGGER.log(Level.INFO, "Indexing Accessor Property for var <"+property.getContainingClassName()+":"+property.getVariableName()+">");
        if (property.getGetter() != null) {
            LOGGER.log(Level.INFO, "ObjJIndex: Indexing accessorProperty ["+property.getContainingClass()+" "+ property.getGetter() + "]");
            indexSink.occurrence(ObjJUnifiedMethodIndex.KEY, property.getGetter());
        }
        if (property.getSetter() != null) {
            LOGGER.log(Level.INFO, "ObjJIndex: Indexing accessorProperty ["+property.getContainingClass()+" "+ property.getSetter() + "]");
            indexSink.occurrence(ObjJUnifiedMethodIndex.KEY, property.getSetter());
        }
    }

    /**
     * Indexes class declarations
     * @param stub class declaration stub
     * @param indexSink index sink
     */
    @Override
    public void indexClassDeclaration(@NotNull
                                              ObjJClassDeclarationStub stub, @NotNull IndexSink indexSink) {
        if (stub.getClassName().isEmpty()) {
         //   LOGGER.log(Level.INFO, "ClassName is empty in class declaration for index");
            return;
        }
        indexSink.occurrence(ObjJClassDeclarationsIndex.getInstance().getKey(), stub.getClassName());
        if (stub instanceof ObjJImplementationStub) {
            indexImplementationClassDeclaration((ObjJImplementationStub)stub, indexSink);
        } else if (stub instanceof  ObjJProtocolDeclarationStub){
            indexSink.occurrence(ObjJProtocolDeclarationsIndex.getInstance().getKey(), stub.getClassName());
        }
        for (Object protocol : stub.getInheritedProtocols()) {
            if (protocol instanceof String) {
                indexSink.occurrence(ObjJClassInheritanceIndex.getInstance().getKey(), (String)protocol);
            }
        }
    }

    private void indexImplementationClassDeclaration(@NotNull ObjJImplementationStub implementationStub, @NotNull IndexSink indexSink) {
        if (implementationStub.isCategory()) {
            indexSink.occurrence(ObjJClassInheritanceIndex.getInstance().getKey(), implementationStub.getClassName());
            indexSink.occurrence(ObjJImplementationCategoryDeclarationsIndex.getInstance().getKey(),implementationStub.getClassName());
        } else if (implementationStub.getSuperClassName() != null && !implementationStub.getSuperClassName().equals(ObjJClassType.CPOBJECT)) {
         //   LOGGER.log(Level.INFO, "Setting super class to: " + implementationStub.getSuperClassName());
            indexSink.occurrence(ObjJClassInheritanceIndex.getInstance().getKey(), implementationStub.getSuperClassName());
        }
        indexSink.occurrence(ObjJImplementationDeclarationsIndex.getInstance().getKey(), implementationStub.getClassName());
    }

    /**
     * Indexes selector literal method as possible inline method declaration
     * @param selectorLiteral selector literal
     * @param indexSink index sink
     */
    @Override
    public void indexSelectorLiteral(@NotNull
                                              ObjJSelectorLiteralStub selectorLiteral, @NotNull IndexSink indexSink) {
        indexSink.occurrence(ObjJSelectorInferredMethodIndex.getInstance().getKey(), selectorLiteral.getSelectorString());
    }

    @Override
    public void indexFunctionDeclaration(@NotNull
                                                 ObjJFunctionDeclarationElementStub functionDeclaration, IndexSink indexSink) {
        if (functionDeclaration.getFunctionName() == null) {
            //noinspection unchecked
            LOGGER.log(Level.INFO, "function("+ ArrayUtils.join(functionDeclaration.getParamNames()) + ") has no function name");
            return;
        }
        LOGGER.log(Level.INFO, "Indexing function: <"+functionDeclaration.getFunctionName()+">");
        indexSink.occurrence(ObjJFunctionsIndex.getInstance().getKey(), functionDeclaration.getFunctionName());
        LOGGER.log(Level.INFO, "Did Index function: <"+functionDeclaration.getFunctionName()+">");
    }

    @Override
    public void indexMethodCall(ObjJMethodCallStub methodHeaderStub, IndexSink indexSink) {
        LOGGER.log(Level.INFO, "Indexing method call  <["+methodHeaderStub.getCallTarget()+" "+methodHeaderStub.getSelectorString()+"]>");
        indexSink.occurrence(ObjJMethodCallIndex.getInstance().getKey(), methodHeaderStub.getSelectorString());
        LOGGER.log(Level.INFO, "Indexed method call.");
    }


    /**
     * Index global declaration
     * @param globalVariableDeclaration selector literal
     * @param indexSink index sink
     */
    @Override
    public void indexGlobalVariableDeclaration(@NotNull
                                                       ObjJGlobalVariableDeclarationStub globalVariableDeclaration, @NotNull IndexSink indexSink) {
        if (globalVariableDeclaration.getFileName() != null) {
            indexSink.occurrence(ObjJGlobalVariablesByFileNameIndex.getInstance().getKey(), globalVariableDeclaration.getFileName());
        }
        indexSink.occurrence(ObjJGlobalVariableNamesIndex.getInstance().getKey(), globalVariableDeclaration.getVariableName());
    }

    public void indexImport(@NotNull ObjJImportStub stub, IndexSink indexSink) {

    }

    public void indexFile(@NotNull ObjJFileStub fileStub, @NotNull IndexSink indexSink) {
        LOGGER.log(Level.INFO, "Indexing file by name: "+fileStub.getFileName());
        indexSink.occurrence(ObjJFilesByNameIndex.getInstance().getKey(), fileStub.getFileName());
    }

    public void indexVarTypeId(@NotNull ObjJVarTypeIdStub stub, IndexSink indexSink) {

    }

    public void indexVariableName(@NotNull ObjJVariableNameStub stub, @NotNull IndexSink indexSink) {
        final String containingFileName = ObjJFileUtil.getContainingFileName(stub.getPsi().getContainingFile());
        if (containingFileName == null) {
            LOGGER.log(Level.SEVERE, "Cannot index variable name, containing file name is null");
            return;
        } else {
            LOGGER.log(Level.INFO, "Indexing variable: "+containingFileName+": "+stub.getVariableName());
        }
        indexSink.occurrence(ObjJVariableNameByScopeIndex.KEY, containingFileName+"-ALL");
        List<Pair<Integer,Integer>> blockRanges = stub.getContainingBlockRanges();
        if (blockRanges.isEmpty()) {
            indexSink.occurrence(ObjJVariableNameByScopeIndex.KEY, containingFileName+"-TOP");
        }

        for (Pair<Integer,Integer> blockRange : blockRanges) {
            indexSink.occurrence(ObjJVariableNameByScopeIndex.KEY, ObjJVariableNameByScopeIndex.getIndexKey(containingFileName, blockRange));
        }
    }

}
