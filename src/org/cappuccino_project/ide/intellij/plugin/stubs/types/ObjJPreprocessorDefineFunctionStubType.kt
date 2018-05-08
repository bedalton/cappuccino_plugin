package org.cappuccino_project.ide.intellij.plugin.stubs.types

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJPreprocessorDefineFunctionImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.impl.ObjJPreprocessorFunctionDeclarationStubImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub

class ObjJPreprocessorDefineFunctionStubType internal constructor(
        debugName: String) : ObjJAbstractFunctionDeclarationStubType<ObjJPreprocessorDefineFunctionImpl, ObjJPreprocessorFunctionDeclarationStubImpl>(debugName, ObjJPreprocessorDefineFunctionImpl::class.java, ObjJPreprocessorFunctionDeclarationStubImpl::class.java) {

    override fun createPsi(
            stub: ObjJFunctionDeclarationElementStub<ObjJPreprocessorDefineFunctionImpl>): ObjJPreprocessorDefineFunctionImpl {
        return ObjJPreprocessorDefineFunctionImpl(stub, ObjJStubTypes.PREPROCESSOR_FUNCTION)
    }

    internal override fun createStub(parent: StubElement<*>,
                                     fileName: String,
                                     fqName: String,
                                     paramNames: List<String>,
                                     returnType: String?,
                                     shouldResolve: Boolean): ObjJFunctionDeclarationElementStub<ObjJPreprocessorDefineFunctionImpl> {
        return ObjJPreprocessorFunctionDeclarationStubImpl(parent, fileName, fqName, paramNames, returnType, shouldResolve)
    }
}
