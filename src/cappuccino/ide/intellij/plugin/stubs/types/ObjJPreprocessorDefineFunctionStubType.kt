package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.psi.impl.ObjJPreprocessorDefineFunctionImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJPreprocessorFunctionDeclarationStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.StubElement

class ObjJPreprocessorDefineFunctionStubType internal constructor(
        debugName: String) : ObjJAbstractFunctionDeclarationStubType<ObjJPreprocessorDefineFunctionImpl>(debugName, ObjJPreprocessorDefineFunctionImpl::class.java) {

    override fun createPsi(
            stub: ObjJFunctionDeclarationElementStub<ObjJPreprocessorDefineFunctionImpl>): ObjJPreprocessorDefineFunctionImpl {
        return ObjJPreprocessorDefineFunctionImpl(stub, ObjJStubTypes.PREPROCESSOR_FUNCTION)
    }

    override fun createStub(parent: StubElement<*>,
                                     fileName: String,
                                     fqName: String,
                                     parameterNames: List<String>,
                                     returnType: String?,
                                     shouldResolve: Boolean,
                                     scope:ObjJFunctionScope): ObjJFunctionDeclarationElementStub<ObjJPreprocessorDefineFunctionImpl> {
        return ObjJPreprocessorFunctionDeclarationStubImpl(parent, fileName, fqName, parameterNames, returnType, shouldResolve)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        return true
    }
}
