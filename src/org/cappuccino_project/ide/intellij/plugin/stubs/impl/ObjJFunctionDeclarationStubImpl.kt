package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJFunctionDeclarationImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes

class ObjJFunctionDeclarationStubImpl(parent: StubElement<*>,
                                      fileName: String,
                                      fqName: String,
                                      paramNames: List<String>,
                                      returnType: String?,
                                      shouldResolve: Boolean) : ObjJFunctionDeclarationElementStubImpl<ObjJFunctionDeclarationImpl>(parent, ObjJStubTypes.FUNCTION_DECLARATION, fileName, fqName, paramNames, returnType, shouldResolve), ObjJFunctionDeclarationElementStub<ObjJFunctionDeclarationImpl>
