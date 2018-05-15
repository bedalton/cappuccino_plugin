package cappuccino.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJFunctionDeclarationImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes

class ObjJFunctionDeclarationStubImpl(parent: StubElement<*>,
                                      fileName: String,
                                      fqName: String,
                                      paramNames: List<String>,
                                      returnType: String?,
                                      shouldResolve: Boolean) : ObjJFunctionDeclarationElementStubImpl<ObjJFunctionDeclarationImpl>(parent, ObjJStubTypes.FUNCTION_DECLARATION, fileName, fqName, paramNames, returnType, shouldResolve), ObjJFunctionDeclarationElementStub<ObjJFunctionDeclarationImpl>
