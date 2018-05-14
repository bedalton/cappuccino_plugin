package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImplementationDeclarationImpl
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement

interface ObjJClassDeclarationStub<PsiT : ObjJClassDeclarationElement<ObjJClassDeclarationStub<PsiT>>> : StubElement<PsiT>, ObjJResolveableStub<PsiT> {

    val className: String
    val inheritedProtocols: List<String>

}