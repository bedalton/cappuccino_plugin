package cappuccino.ide.intellij.plugin.stubs.types

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionLiteral
import cappuccino.ide.intellij.plugin.psi.impl.ObjJFunctionLiteralImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJFunctionLiteralStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub

class ObjJFunctionLiteralStubType internal constructor(
        debugName: String) : ObjJAbstractFunctionDeclarationStubType<ObjJFunctionLiteralImpl, ObjJFunctionLiteralStubImpl>(debugName, ObjJFunctionLiteralImpl::class.java, ObjJFunctionLiteralStubImpl::class.java) {

    override fun createPsi(
            stub: ObjJFunctionDeclarationElementStub<ObjJFunctionLiteralImpl>): ObjJFunctionLiteralImpl {
        return ObjJFunctionLiteralImpl(stub, this)
    }

    override fun createStub(parent: StubElement<*>,
                                     fileName: String,
                                     fqName: String,
                                     paramNames: List<String>,
                                     returnType: String?,
                                     shouldResolve: Boolean
    ): ObjJFunctionDeclarationElementStub<ObjJFunctionLiteralImpl> {
        return ObjJFunctionLiteralStubImpl(parent, fileName, fqName, paramNames, returnType, shouldResolve)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return false
    }
}
