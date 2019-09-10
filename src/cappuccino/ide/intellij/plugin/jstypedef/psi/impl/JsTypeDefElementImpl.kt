package cappuccino.ide.intellij.plugin.jstypedef.psi.impl

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefInterfaceElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefModule
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefElement
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.usageView.UsageViewUtil
import javax.swing.Icon

open class JsTypeDefElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), JsTypeDefElement {

    val elementType:IElementType get () = node.elementType

    override val containerName: String?
        get() {
            val parentClass = getParentOfType(JsTypeDefInterfaceElement::class.java)
            if (parentClass != null) {
                val typeName = parentClass.typeName?.id?.text
                if (typeName != null)
                    return typeName
            }
            val module = getParentOfType(JsTypeDefModule::class.java)
            if (module != null) {
                return module.namespacedName
            }
            return containingFile?.name ?: "???"
        }

    override val containingTypeDefFile: JsTypeDefFile?
        get() {
            return containingFile as? JsTypeDefFile
        }

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
        return processor.execute(element, state) && JsTypeDefResolveUtil.processChildren(element, processor, state, lastParent, place)
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
                return this@JsTypeDefElementImpl.getIcon(0)
            }
        }
    }

    override fun <T:PsiElement> getParentOfType(parentClass:Class<T>) : T? = PsiTreeUtil.getParentOfType(this, parentClass)
    override fun <T:PsiElement> getChildOfType(childClass:Class<T>) : T? = PsiTreeUtil.getChildOfType(this, childClass)
    override fun <T:PsiElement> getChildrenOfType(childClass:Class<T>) : List<T> = PsiTreeUtil.getChildrenOfTypeAsList(this, childClass)


}