package cappuccino.ide.intellij.plugin.universal.psi

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.psi.impl.ObjJResolveUtil
import cappuccino.ide.intellij.plugin.psi.utils.ReferencedInScope
import cappuccino.ide.intellij.plugin.psi.utils.getScope
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.usageView.UsageViewUtil
import javax.swing.Icon

class ObjJUniversalPsiElementImpl(node: ASTNode) : ASTWrapperPsiElement(node),  ObjJUniversalPsiElement {

    override fun <T: PsiElement> getParentOfType(parentClass:Class<T>) : T? = PsiTreeUtil.getParentOfType(this, parentClass)
    override fun <T: PsiElement> getChildOfType(childClass:Class<T>) : T? = PsiTreeUtil.getChildOfType(this, childClass)
    override fun <T: PsiElement> getChildrenOfType(childClass:Class<T>) : List<T> = PsiTreeUtil.getChildrenOfTypeAsList(this, childClass)

    override fun toString(): String {
        return node.elementType.toString()
    }

    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        return processDeclarations(this, processor, state, lastParent, place)
    }

    private fun processDeclarations(
            element: PsiElement,
            processor: PsiScopeProcessor,
            state: ResolveState, lastParent: PsiElement?,
            place: PsiElement): Boolean {
        return processor.execute(element, state) && ObjJResolveUtil.processChildren(element, processor, state, lastParent, place)
    }

    override fun getPresentation(): ItemPresentation? {
        val text = UsageViewUtil.createNodeText(this)
        return object : ItemPresentation {
            override fun getPresentableText(): String {
                return text
            }

            override fun getLocationString(): String {
                return containingFile.name
            }

            override fun getIcon(b: Boolean): Icon? {
                return this@ObjJUniversalPsiElementImpl.getIcon(0)
            }
        }
    }
}


open class ObjJUniversalStubBasedElementImpl<StubT : StubElement<out PsiElement>> : StubBasedPsiElementBase<StubT>, ObjJUniversalPsiElement, ObjJUniversalStubBasedElement<StubT> {

    val psiOrParent: ObjJUniversalPsiElement
        get() = this

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

    override fun commonContext(otherElement:PsiElement) : PsiElement? {
        return PsiTreeUtil.findCommonContext(this, otherElement)
    }

    override fun commonScope(otherElement: PsiElement) : ReferencedInScope {
        return getScope(commonContext(otherElement))
    }
}