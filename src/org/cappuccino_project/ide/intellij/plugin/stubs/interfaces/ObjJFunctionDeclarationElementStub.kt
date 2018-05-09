package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJFunctionLiteral
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJFunctionDeclarationImpl
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJResolveableElement

interface ObjJFunctionDeclarationElementStub<PsiT : ObjJFunctionDeclarationElement> : StubElement<PsiT>, ObjJResolveableStub<PsiT> {
    val fileName: String
    val fqName: String
    val functionName: String
    val numParams: Int
    val paramNames: List<String>
    val returnType: String?
}