package cappuccino.ide.intellij.plugin.stubs.types

import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJFunctionDeclarationImpl
import cappuccino.ide.intellij.plugin.psi.utils.ReferencedInScope
import cappuccino.ide.intellij.plugin.psi.utils.getContainingScope
import cappuccino.ide.intellij.plugin.psi.utils.getScopeBlock
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJFunctionDeclarationStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiFile

class ObjJFunctionDeclarationStubType internal constructor(
        debugName: String) : ObjJAbstractFunctionDeclarationStubType<ObjJFunctionDeclarationImpl, ObjJFunctionDeclarationStubImpl>(debugName, ObjJFunctionDeclarationImpl::class.java, ObjJFunctionDeclarationStubImpl::class.java) {

    override fun createPsi(
            stub: ObjJFunctionDeclarationElementStub<ObjJFunctionDeclarationImpl>): ObjJFunctionDeclarationImpl {
        return ObjJFunctionDeclarationImpl(stub, this)
    }

    override fun createStub(parent: StubElement<*>,
                                     fileName: String,
                                     fqName: String,
                                     paramNames: List<String>,
                                     returnType: String?,
                                     shouldResolve: Boolean): ObjJFunctionDeclarationElementStub<ObjJFunctionDeclarationImpl> {
        return ObjJFunctionDeclarationStubImpl(parent, fileName, fqName, paramNames, returnType, shouldResolve)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return node?.psi?.parent as? PsiFile != null
    }
}
