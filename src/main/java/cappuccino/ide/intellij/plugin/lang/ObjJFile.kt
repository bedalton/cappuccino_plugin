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
import cappuccino.ide.intellij.plugin.utils.ObjJFrameworkUtils
import cappuccino.ide.intellij.plugin.utils.ifEquals
import cappuccino.ide.intellij.plugin.utils.nullIfEquals
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import icons.ObjJIcons
import javax.swing.Icon

class ObjJFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ObjJLanguage.instance), ObjJCompositeElement, ObjJHasTreeStructureElement {

    private val fileCache: ObjJFileCache by lazy {
        ObjJFileCache(this)
    }

    val frameworkName: String get() {
        return fileCache.frameworkName.nullIfEquals(EMPTY_FRAMEWORK_NAME)
                ?: ObjJFrameworkUtils.getEnclosingFrameworkName(this)
    }

    val cachedImportFileList: List<ObjJFile>?
        get() = fileCache.importedFiles ?: getImportedFiles(recursive = false, cache = true)

    val asImportStruct : ObjJImportInfoStub get() = ObjJImportInfoStub(frameworkName, name)

    private val importedFilesInternal : List<ObjJImportInfoStub> by lazy {
        if (DumbService.isDumb(project))
            throw IndexNotReadyException.create()
        val thisFrameworkName = frameworkName
        stub?.imports?.map {
            if (it.framework == EMPTY_FRAMEWORK_NAME)
                it.copy(framework = thisFrameworkName, fileName = it.fileName)
            else
                it
        } ?: (collectImports(this).map {
            val frameworkName = it.frameworkNameString.ifEquals(EMPTY_FRAMEWORK_NAME) { thisFrameworkName }
            ObjJImportInfoStub(frameworkName, it.fileNameString)
        })
    }

    val importedFiles : List<ObjJImportInfoStub> get() {
        if (DumbService.isDumb(project)) {
            return stub?.imports ?: collectImports(this).map {
                ObjJImportInfoStub(it.frameworkNameString, it.fileNameString)
            }
        }
        return importedFilesInternal
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
        return PsiTreeUtil.collectElementsOfType(containingFile, aClass).toList()
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


