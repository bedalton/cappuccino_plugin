package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces

import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJImplementationDeclarationImpl

interface ObjJImplementationStub : ObjJClassDeclarationStub<ObjJImplementationDeclarationImpl> {

    val superClassName: String?
    val categoryName: String?
    val isCategory: Boolean

}
