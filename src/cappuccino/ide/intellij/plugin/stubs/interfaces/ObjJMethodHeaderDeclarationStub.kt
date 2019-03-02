package cappuccino.ide.intellij.plugin.stubs.interfaces


import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import com.intellij.psi.PsiElement

interface ObjJMethodHeaderDeclarationStub<PsiT : PsiElement> : StubElement<PsiT> {
    val paramTypes: List<String>

    val selectorStrings: List<String>

    val selectorString: String

    val containingClassName: String

    val isRequired: Boolean

    val returnType: ObjJClassType

    val returnTypeAsString: String

    val isStatic: Boolean

    val ignored: Boolean
}
