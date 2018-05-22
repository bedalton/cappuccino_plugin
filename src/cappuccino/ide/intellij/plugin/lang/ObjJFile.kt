package cappuccino.ide.intellij.plugin.lang

import cappuccino.ide.intellij.plugin.structure.ObjJStructureViewElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasTreeStructureElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFilePsiUtil
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import com.intellij.ide.projectView.PresentationData

import javax.swing.*

class ObjJFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ObjJLanguage.instance), ObjJCompositeElement, ObjJHasTreeStructureElement {

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

    override fun <PsiT : PsiElement> getChildOfType(childClass: Class<PsiT>): PsiT? =
            PsiTreeUtil.getChildOfType(this, childClass)


    override fun <PsiT : PsiElement> getChildrenOfType(childClass: Class<PsiT>): List<PsiT> =
            PsiTreeUtil.getChildrenOfTypeAsList(this, childClass)

    override fun <PsiT : PsiElement> getParentOfType(parentClass: Class<PsiT>): PsiT? =
            PsiTreeUtil.getParentOfType(this, parentClass)

    override fun createTreeStructureElement(): ObjJStructureViewElement {
        val fileName = ObjJFileUtil.getFileNameSafe(this, "")
        return ObjJStructureViewElement(this, PresentationData(fileName, "", ObjJIcons.DOCUMENT_ICON, null), fileName)
    }

}
