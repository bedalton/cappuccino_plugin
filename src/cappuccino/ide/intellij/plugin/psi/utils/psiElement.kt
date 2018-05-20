package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil

fun <PsiT : PsiElement> PsiElement?.getParentOfType(clazz:Class<PsiT>) : PsiT? = PsiTreeUtil.getParentOfType(this, clazz)
fun <PsiT : PsiElement> PsiElement?.getChildOfType(clazz:Class<PsiT>) : PsiT? = PsiTreeUtil.getChildOfType(this, clazz)
fun <PsiT : PsiElement> PsiElement?.getChildrenOfType(clazz:Class<PsiT>) : List<PsiT> = PsiTreeUtil.getChildrenOfTypeAsList(this, clazz)
fun PsiElement?.getElementType() : IElementType? = this?.node?.elementType
@Deprecated("Use getElementType instead", ReplaceWith("getElementType()"))
fun PsiElement?.getNodeType() : IElementType? = this?.node?.elementType


infix fun PsiElement?.equals (otherElement: PsiElement?): Boolean = this?.isEquivalentTo(otherElement) ?: false
operator fun PsiElement?.contains (parent:PsiElement?): Boolean = if (this != null) PsiTreeUtil.isAncestor(parent, this, false) else false
infix fun PsiElement?.isType (elementType:IElementType) : Boolean = if (this != null) this.node.elementType == elementType else false
fun PsiElement.tokenType() : IElementType = this.node.elementType
var el1:PsiElement? = null;
var el2:PsiElement? = null;

infix fun <T : PsiElement> PsiElement.hasParentOfType (parentClass:Class<T>) : Boolean =
    this.getParentOfType(parentClass) != null