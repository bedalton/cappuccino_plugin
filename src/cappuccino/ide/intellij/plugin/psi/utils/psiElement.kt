package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil

/**
 * Gets first parent of type in psi tree
 */
fun <PsiT : PsiElement> PsiElement?.getParentOfType(clazz:Class<PsiT>) : PsiT? = PsiTreeUtil.getParentOfType(this, clazz)

/**
 * Gets parent element of type, or self cast as element if valid.
 * @param clazz Class of the psi element desired
 * @return self or parent of element type
 */
fun <PsiT : PsiElement> PsiElement?.getSelfOrParentOfType(clazz:Class<PsiT>) : PsiT? {
    return when {
        this == null -> null
        clazz.isInstance(this) -> clazz.cast(this)
        else -> PsiTreeUtil.getParentOfType(this, clazz)
    }
}

/**
 * Gets first psi element child of type
 * @returns first psi child of type
 */
fun <PsiT : PsiElement> PsiElement?.getChildOfType(clazz:Class<PsiT>) : PsiT? = PsiTreeUtil.getChildOfType(this, clazz)

/**
 * Gets psi children of type
 */
fun <PsiT : PsiElement> PsiElement?.getChildrenOfType(clazz:Class<PsiT>) : List<PsiT> = PsiTreeUtil.getChildrenOfTypeAsList(this, clazz)


val PsiElement?.elementType : IElementType? get() { return this?.node?.elementType }

infix fun PsiElement?.equals (otherElement: PsiElement?): Boolean = this?.isEquivalentTo(otherElement) ?: false

operator fun PsiElement?.contains (parent:PsiElement?): Boolean = if (this != null) PsiTreeUtil.isAncestor(parent, this, false) else false

infix fun PsiElement?.isType (elementType:IElementType) : Boolean = if (this != null) this.node.elementType == elementType else false


fun PsiElement.tokenType() : IElementType = this.node.elementType

fun PsiElement.getChildByType(elementType: IElementType) : PsiElement? {
    return this.node?.findChildByType(elementType)?.psi
}

infix fun <T : PsiElement> PsiElement?.isOrHasParentOfType (parentClass:Class<T>) : Boolean =
        this != null && (parentClass.isInstance(this) || this.getParentOfType(parentClass) != null)

infix fun <T : PsiElement> PsiElement?.hasParentOfType (parentClass:Class<T>) : Boolean =
        this != null && this.getParentOfType(parentClass) != null

infix fun <T : PsiElement> PsiElement?.doesNotHaveParentOfType (parentClass:Class<T>) : Boolean =
        this != null && this.getParentOfType(parentClass) == null