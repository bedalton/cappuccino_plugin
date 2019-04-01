package cappuccino.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJFunctionLiteralImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes

class ObjJFunctionLiteralStubImpl(parent: StubElement<*>,
                                  fileName: String,
                                  fqName: String,
                                  paramNames: List<String>,
                                  returnType: String?,
                                  shouldResolve: Boolean,
                                  scope:ObjJFunctionScope) : ObjJFunctionDeclarationElementStubImpl<ObjJFunctionLiteralImpl>(parent, ObjJStubTypes.FUNCTION_LITERAL, fileName, fqName, paramNames, returnType, shouldResolve, scope), ObjJFunctionDeclarationElementStub<ObjJFunctionLiteralImpl>
