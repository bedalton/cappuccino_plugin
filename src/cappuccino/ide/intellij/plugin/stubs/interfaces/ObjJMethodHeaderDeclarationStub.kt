package cappuccino.ide.intellij.plugin.stubs.interfaces


import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJSelectorStruct
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement

interface ObjJMethodHeaderDeclarationStub<PsiT : PsiElement> : StubElement<PsiT> {
    val parameterTypes: List<String>

    val selectorStrings: List<String>

    val selectorString: String

    val containingClassName: String

    val isRequired: Boolean

    val explicitReturnType:String

    val isStatic: Boolean

    val ignored: Boolean

    val selectorStructs:List<ObjJSelectorStruct>

    val isPrivate:Boolean

}
