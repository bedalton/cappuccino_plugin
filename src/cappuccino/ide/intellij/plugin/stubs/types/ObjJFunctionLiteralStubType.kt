package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJFunctionLiteralImpl
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFunctionDeclarationPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJFunctionLiteralStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope

class ObjJFunctionLiteralStubType internal constructor(
        debugName: String) : ObjJAbstractFunctionDeclarationStubType<ObjJFunctionLiteralImpl>(debugName, ObjJFunctionLiteralImpl::class.java) {

    override fun createPsi(
            stub: ObjJFunctionDeclarationElementStub<ObjJFunctionLiteralImpl>): ObjJFunctionLiteralImpl {
        return ObjJFunctionLiteralImpl(stub, this)
    }

    override fun createStub(
            parent: StubElement<*>,
            fileName: String,
            fqName: String,
            paramNames: List<String>,
            returnType: String?,
            shouldResolve: Boolean,
            scope:ObjJFunctionScope
    ): ObjJFunctionDeclarationElementStub<ObjJFunctionLiteralImpl> {
        return ObjJFunctionLiteralStubImpl(parent, fileName, fqName, paramNames, returnType, shouldResolve, scope)
    }
}
