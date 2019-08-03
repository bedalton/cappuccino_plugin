package cappuccino.ide.intellij.plugin.stubs.interfaces


import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassTypeName
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJSelectorStruct
import com.intellij.psi.PsiElement

interface ObjJMethodHeaderDeclarationStub<PsiT : PsiElement> : StubElement<PsiT> {
    val paramTypes: List<String>

    val selectorStrings: List<String>

    val selectorString: String

    val containingClassName: String

    val isRequired: Boolean

    val explicitReturnType:String

    val isStatic: Boolean

    val ignored: Boolean

    val selectorStructs:List<ObjJSelectorStruct>

}
