package cappuccino.ide.intellij.plugin.lang

import cappuccino.ide.intellij.plugin.caches.ObjJFileCache
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.structure.ObjJStructureViewElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFilePsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiFileUtil
import cappuccino.ide.intellij.plugin.psi.utils.getImportedFiles
import cappuccino.ide.intellij.plugin.utils.ObjJImportUtils
import com.intellij.ide.projectView.PresentationData
import icons.ObjJIcons

import javax.swing.*

class ObjJFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ObjJLanguage.instance), ObjJCompositeElement, ObjJHasTreeStructureElement {

    private val fileCache:ObjJFileCache by lazy {
        ObjJFileCache(this)
    }

    val cachedImportFileList:List<ObjJFile>?
        get() = fileCache.importedFiles ?: getImportedFiles(recursive = false, cache = false)

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

    fun <PsiT:PsiElement> getFileChildrenOfType(aClass: Class<PsiT>, recursive: Boolean): List<PsiT> {
        val children = getChildrenOfType(aClass).toMutableList()
        if (!recursive) {
            return children
        }
        val blockChildren = getChildrenOfType(ObjJBlock::class.java).flatMap {
            it.getBlockChildrenOfType(aClass, true)
        }
        children.addAll(blockChildren)
        return children
    }

    override fun <PsiT : PsiElement> getParentOfType(parentClass: Class<PsiT>): PsiT? =
            PsiTreeUtil.getParentOfType(this, parentClass)

    override fun createTreeStructureElement(): ObjJStructureViewElement {
        val fileName = ObjJPsiFileUtil.getFileNameSafe(this, "")
        return ObjJStructureViewElement(this, PresentationData(fileName, "", ObjJIcons.DOCUMENT_ICON, null), fileName)
    }
}


