package org.cappuccino_project.ide.intellij.plugin.lang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJFilePsiUtil

import javax.swing.*

class ObjJFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ObjJLanguage.INSTANCE), ObjJCompositeElement {

    val classDeclarations: List<ObjJClassDeclarationElement<*>>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJClassDeclarationElement::class.java)

    val importStrings: List<String>
        get() = ObjJFilePsiUtil.getImportsAsStrings(this)

    override fun toString(): String {
        return "ObjectiveJ Language file"
    }

    override fun getIcon(flags: Int): Icon? {
        return ObjJIcons.DOCUMENT_ICON
    }

    override fun getFileType(): FileType {
        return ObjJFileType.INSTANCE
    }

    override fun <PsiT : PsiElement> getChildOfType(childClass: Class<PsiT>): PsiT? {
        return this.getChildOfType( childClass)
    }

    override fun <PsiT : PsiElement> getChildrenOfType(childClass: Class<PsiT>): List<PsiT> {
        return this.getChildrenOfType( childClass)
    }

    override fun <PsiT : PsiElement> getParentOfType(parentClass: Class<PsiT>): PsiT? {
        return this.getParentOfType( parentClass)
    }


}
