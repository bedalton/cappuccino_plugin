package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces


import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType

interface ObjJMethodHeaderDeclarationStub<PsiT : ObjJMethodHeaderDeclaration<ObjJMethodHeaderDeclarationStub<PsiT>>> : StubElement<PsiT> {
    val paramTypes: List<String>

    val selectorStrings: List<String>

    val selectorString: String

    val containingClassName: String

    val isRequired: Boolean

    val returnType: ObjJClassType

    val returnTypeAsString: String

    val isStatic: Boolean

}
