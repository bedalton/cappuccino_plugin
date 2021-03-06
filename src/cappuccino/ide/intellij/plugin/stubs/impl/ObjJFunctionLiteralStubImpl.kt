package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJFunctionLiteralImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement

class ObjJFunctionLiteralStubImpl(parent: StubElement<*>,
                                  fileName: String,
                                  fqName: String,
                                  parameterNames: List<String>,
                                  returnType: String?,
                                  shouldResolve: Boolean,
                                  scope:ObjJFunctionScope) : ObjJFunctionDeclarationElementStubImpl<ObjJFunctionLiteralImpl>(parent, ObjJStubTypes.FUNCTION_LITERAL, fileName, fqName, parameterNames, returnType, shouldResolve, scope), ObjJFunctionDeclarationElementStub<ObjJFunctionLiteralImpl>
