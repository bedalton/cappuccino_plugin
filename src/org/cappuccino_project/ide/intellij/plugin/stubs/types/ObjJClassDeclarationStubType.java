package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.stubs.IndexSink;
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub;
import org.jetbrains.annotations.NotNull;

public abstract class ObjJClassDeclarationStubType<StubT extends ObjJClassDeclarationStub, PsiT extends ObjJClassDeclarationElement<?>> extends ObjJStubElementType <StubT, PsiT> {
    ObjJClassDeclarationStubType(
            @NotNull
                    String debugName,
            @NotNull
                    Class<PsiT> psiClass,
            @NotNull
                    Class<StubT> stubClass) {
        super(debugName, psiClass, stubClass);
    }

    @Override
    public void indexStub(@NotNull ObjJClassDeclarationStub stub, @NotNull IndexSink indexSink) {
        ServiceManager.getService(StubIndexService.class).indexClassDeclaration(stub, indexSink);
    }

}
