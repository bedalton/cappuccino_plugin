package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import com.intellij.psi.PsiElement

interface ObjJHasFunctionName {
    val functionNameAsString: String

    val functionNameNode: ObjJNamedElement?
}
