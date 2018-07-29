package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.psi.ObjJPreprocessorDefineFunction
import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJPreprocessorDefineFunctionImpl
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJPreprocessorFunctionDeclarationStubImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import com.intellij.lang.ASTNode

class ObjJPreprocessorDefineFunctionStubType internal constructor(
        debugName: String) : ObjJAbstractFunctionDeclarationStubType<ObjJPreprocessorDefineFunctionImpl, ObjJPreprocessorFunctionDeclarationStubImpl>(debugName, ObjJPreprocessorDefineFunctionImpl::class.java, ObjJPreprocessorFunctionDeclarationStubImpl::class.java) {

    override fun createPsi(
            stub: ObjJFunctionDeclarationElementStub<ObjJPreprocessorDefineFunctionImpl>): ObjJPreprocessorDefineFunctionImpl {
        return ObjJPreprocessorDefineFunctionImpl(stub, ObjJStubTypes.PREPROCESSOR_FUNCTION)
    }

    override fun createStub(parent: StubElement<*>,
                                     fileName: String,
                                     fqName: String,
                                     paramNames: List<String>,
                                     returnType: String?,
                                     shouldResolve: Boolean): ObjJFunctionDeclarationElementStub<ObjJPreprocessorDefineFunctionImpl> {
        return ObjJPreprocessorFunctionDeclarationStubImpl(parent, fileName, fqName, paramNames, returnType, shouldResolve)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        val psi:ObjJPreprocessorDefineFunction = node?.psi  as? ObjJPreprocessorDefineFunction ?: return false
        return psi.functionName != null || psi.variableName != null
    }
}
