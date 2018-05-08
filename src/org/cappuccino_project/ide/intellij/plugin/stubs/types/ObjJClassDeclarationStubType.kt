package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.stubs.IndexSink
import org.cappuccino_project.ide.intellij.plugin.indices.StubIndexService
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub

abstract class ObjJClassDeclarationStubType<StubT : ObjJClassDeclarationStub<*>, PsiT : ObjJClassDeclarationElement<*>> internal constructor(
        debugName: String,
        psiClass: Class<PsiT>,
        stubClass: Class<StubT>) : ObjJStubElementType<StubT, PsiT>(debugName, psiClass, stubClass) {

    override fun indexStub(stub: ObjJClassDeclarationStub<*>, indexSink: IndexSink) {
        ServiceManager.getService(StubIndexService::class.java).indexClassDeclaration(stub, indexSink)
    }

}
