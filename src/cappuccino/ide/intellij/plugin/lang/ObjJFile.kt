package cappuccino.ide.intellij.plugin.lang

import cappuccino.ide.intellij.plugin.caches.ObjJFileCache
import cappuccino.ide.intellij.plugin.psi.ObjJClassDependencyStatement
import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasTreeStructureElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiFileUtil
import cappuccino.ide.intellij.plugin.psi.utils.collectImports
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.psi.utils.getImportedFiles
import cappuccino.ide.intellij.plugin.structure.ObjJStructureViewElement
import cappuccino.ide.intellij.plugin.stubs.impl.ObjJImportInfoStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFileStub
import cappuccino.ide.intellij.plugin.utils.EMPTY_FRAMEWORK_NAME
import cappuccino.ide.intellij.plugin.utils.enclosingFrameworkName
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import icons.ObjJIcons
import javax.swing.Icon

class ObjJFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ObjJLanguage.instance), ObjJCompositeElement, ObjJHasTreeStructureElement {

    private val fileCache: ObjJFileCache by lazy {
        ObjJFileCache(this)
    }

    val frameworkName: String by lazy {
        stub?.framework ?: fileCache.frameworkName ?: enclosingFrameworkName
    }

    val cachedImportFileList: List<ObjJFile>?
        get() = fileCache.importedFiles ?: getImportedFiles(recursive = false, cache = true)

    val asImportStruct : ObjJImportInfoStub by lazy {
        ObjJImportInfoStub(frameworkName, name)
    }

    val getImportedFiles : List<ObjJImportInfoStub> by lazy {
        stub?.imports ?: collectImports(this).map {
            ObjJImportInfoStub(it.frameworkNameString, it.fileNameString)
        }
    }

    val classDeclarations: List<ObjJClassDeclarationElement<*>>
        get() = fileCache.classDeclarations

    val definedClassNames:List<String>
        get() = insideClasses.mapNotNull { it.text }

    val classDependencyStatements: List<ObjJClassDependencyStatement>
        get() = fileCache.classDependencyStatements

    private val insideClasses:List<ObjJClassName>
        get() = fileCache.insideClasses

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

    fun <PsiT : PsiElement> getFileChildrenOfType(aClass: Class<PsiT>, recursive: Boolean): List<PsiT> {
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

    override fun getStub(): ObjJFileStub? {
        return super.getStub() as? ObjJFileStub
    }

    override fun <PsiT : PsiElement> getParentOfType(parentClass: Class<PsiT>): PsiT? =
            PsiTreeUtil.getParentOfType(this, parentClass)

    override fun createTreeStructureElement(): ObjJStructureViewElement {
        val fileName = ObjJPsiFileUtil.getFileNameSafe(this, "")
        return ObjJStructureViewElement(this, PresentationData(fileName, "", ObjJIcons.DOCUMENT_ICON, null), fileName)
    }
}


