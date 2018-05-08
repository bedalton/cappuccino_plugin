package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJGlobalVariableDeclarationImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJGlobalVariableDeclarationStub
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes
import org.codehaus.groovy.ast.ASTNode

class ObjJGlobalVariableDeclarationStubImpl(parent: StubElement<*>, override val fileName: String?, override val variableName: String, override val variableType: String?) : ObjJStubBaseImpl<ObjJGlobalVariableDeclarationImpl>(parent, ObjJStubTypes.GLOBAL_VARIABLE), ObjJGlobalVariableDeclarationStub
