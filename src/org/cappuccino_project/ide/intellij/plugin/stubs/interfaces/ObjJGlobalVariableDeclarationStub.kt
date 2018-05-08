package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJGlobalVariableDeclarationImpl

interface ObjJGlobalVariableDeclarationStub : StubElement<ObjJGlobalVariableDeclarationImpl> {

    val fileName: String?
    val variableName: String
    val variableType: String?

}
