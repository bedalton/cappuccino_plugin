package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJPreprocessorDefineFunctionImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement

class ObjJPreprocessorFunctionDeclarationStubImpl(parent: StubElement<*>,
                                                  fileName: String,
                                                  fqName: String,
                                                  paramNames: List<String>,
                                                  returnType: String?,
                                                  shouldResolve: Boolean
) : ObjJFunctionDeclarationElementStubImpl<ObjJPreprocessorDefineFunctionImpl>(parent, ObjJStubTypes.PREPROCESSOR_FUNCTION, fileName, fqName, paramNames, returnType, shouldResolve, ObjJFunctionScope.GLOBAL_SCOPE), ObjJFunctionDeclarationElementStub<ObjJPreprocessorDefineFunctionImpl>
