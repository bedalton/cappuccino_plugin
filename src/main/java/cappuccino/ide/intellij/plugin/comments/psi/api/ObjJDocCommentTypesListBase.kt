package cappuccino.ide.intellij.plugin.comments.psi.api

import com.intellij.psi.PsiElement

interface ObjJDocCommentTypesListBase: PsiElement {
    val qualifiedNameList: List<ObjJDocCommentQualifiedName>
}