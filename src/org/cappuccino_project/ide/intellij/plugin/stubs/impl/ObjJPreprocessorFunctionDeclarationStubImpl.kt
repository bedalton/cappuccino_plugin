package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJFunctionLiteralImpl
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJPreprocessorDefineFunctionImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes

class ObjJPreprocessorFunctionDeclarationStubImpl(parent: StubElement<*>,
                                                  fileName: String,
                                                  fqName: String,
                                                  paramNames: List<String>,
                                                  returnType: String?,
                                                  shouldResolve: Boolean
) : ObjJFunctionDeclarationElementStubImpl<ObjJPreprocessorDefineFunctionImpl>(parent, ObjJStubTypes.PREPROCESSOR_FUNCTION, fileName, fqName, paramNames, returnType, shouldResolve), ObjJFunctionDeclarationElementStub<ObjJPreprocessorDefineFunctionImpl>
