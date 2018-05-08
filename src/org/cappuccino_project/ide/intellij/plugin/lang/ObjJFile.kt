package org.cappuccino_project.ide.intellij.plugin.lang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJFrameworkReference
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImportFramework
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJImportStatement
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJFilePsiUtil
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil

import javax.swing.*
import java.util.ArrayList
import java.util.HashMap
import java.util.regex.Pattern

class ObjJFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ObjJLanguage.INSTANCE), ObjJCompositeElement {

    val classDeclarations: List<ObjJClassDeclarationElement<*>>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJClassDeclarationElement<*>::class.java)

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
        return ObjJTreeUtil.getChildOfType(this, childClass)
    }

    override fun <PsiT : PsiElement> getChildrenOfType(childClass: Class<PsiT>): List<PsiT> {
        return ObjJTreeUtil.getChildrenOfTypeAsList(this, childClass)
    }

    override fun <PsiT : PsiElement> getParentOfType(parentClass: Class<PsiT>): PsiT? {
        return ObjJTreeUtil.getParentOfType(this, parentClass)
    }


}
