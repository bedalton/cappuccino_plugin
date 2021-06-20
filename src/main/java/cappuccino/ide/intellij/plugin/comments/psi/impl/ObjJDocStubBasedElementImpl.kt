package cappuccino.ide.intellij.plugin.comments.psi.impl

import cappuccino.ide.intellij.plugin.comments.psi.api.ObjJDocCommentElement
import cappuccino.ide.intellij.plugin.comments.psi.api.ObjJDocCommentStubBasedElement
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.PsiTreeUtil

open class ObjJDocCommentStubBasedElementImpl<StubT : StubElement<out PsiElement>> : StubBasedPsiElementBase<StubT>, ObjJDocCommentElement, ObjJDocCommentStubBasedElement<StubT> {

    constructor(stub: StubT, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    constructor(node: ASTNode) : super(node)

    override fun getLanguage(): Language {
        return ObjJLanguage.instance
    }

    override fun toString(): String {
        return elementType.toString()
    }

    override fun <T:PsiElement> getParentOfType(parentClass:Class<T>) : T? = PsiTreeUtil.getParentOfType(this, parentClass)
    override fun <T:PsiElement> getChildOfType(childClass:Class<T>) : T? = PsiTreeUtil.getChildOfType(this, childClass)
    override fun <T:PsiElement> getChildrenOfType(childClass:Class<T>) : List<T> = PsiTreeUtil.getChildrenOfTypeAsList(this, childClass)

    val tokenType:IStubElementType<*,*> get() = elementType
}