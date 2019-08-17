package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJFunctionDeclarationImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement

class ObjJFunctionDeclarationStubImpl(parent: StubElement<*>,
                                      fileName: String,
                                      fqName: String,
                                      paramNames: List<String>,
                                      returnType: String?,
                                      shouldResolve: Boolean,
                                      scope:ObjJFunctionScope) : ObjJFunctionDeclarationElementStubImpl<ObjJFunctionDeclarationImpl>(parent, ObjJStubTypes.FUNCTION_DECLARATION, fileName, fqName, paramNames, returnType, shouldResolve, scope), ObjJFunctionDeclarationElementStub<ObjJFunctionDeclarationImpl>
