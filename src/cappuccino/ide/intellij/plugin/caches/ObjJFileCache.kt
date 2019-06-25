package cappuccino.ide.intellij.plugin.caches

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.ObjJClassDependencyStatement
import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.collectElementsOfType
import cappuccino.ide.intellij.plugin.psi.utils.getImportedFiles
import cappuccino.ide.intellij.plugin.utils.*
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.*

class ObjJFileCache(file:ObjJFile) {
    private val manager by lazy { CachedValuesManager.getManager(file.project) }
    private val modificationTracker by lazy { MyModificationTracker() }
    private val dependencies:List<Any> by lazy {listOf(modificationTracker, PsiModificationTracker.MODIFICATION_COUNT)}
    private var cachedFrameworkName:String? = null
    val frameworkName:String?
        get() = cachedFrameworkName ?: findFrameworkNameInPlist(frameworkPlist.value)
    val classDeclarations: List<ObjJClassDeclarationElement<*>>
        get() = classDeclarationsCachedValue.value
    val classDependencyStatements:List<ObjJClassDependencyStatement>
        get() = classDependenciesCachedValue.value

    val insideClasses:List<ObjJClassName>
        get() = insideClassesCachedValue.value
    /**
     * Gets the imported files
     */
    val importedFiles:List<ObjJFile>?
        get() = importedFilesCachedValue.value

    /**
     * Creates an imported files cache
     */
    private val importedFilesCachedValue:CachedValue<List<ObjJFile>> by lazy {
        val provider = CachedValueProvider<List<ObjJFile>> {
            val files = file.getImportedFiles(recursive = false, cache = false)
            CachedValueProvider.Result.create(files, dependencies)
        }
        manager.createCachedValue(provider)
    }

    private val frameworkPlist:CachedValue<PsiFile?> by lazy {
        val provider = CachedValueProvider<PsiFile> {
            var out:PsiFile? = null
            var directory:PsiDirectory? = file.parent
            while(directory != null) {
                val temp = directory.findFile(INFO_PLIST_FILE_NAME) ?: directory.findFile(INFO_PLIST_FILE_NAME_TO_LOWER_CASE)
                cachedFrameworkName = findFrameworkNameInPlist(temp)
                if (cachedFrameworkName != null) {
                    out = temp
                    break
                }
                directory = directory.parent
            }
            if (out == null) {
                LOGGER.info("Failed to find framework plist for file: <${file.name}>")
            }
            CachedValueProvider.Result.create(out, dependencies)
        }
        manager.createCachedValue(provider)
    }

    private val classDeclarationsCachedValue:CachedValue<List<ObjJClassDeclarationElement<*>>> by lazy {
        val provider = CachedValueProvider {
            val declarations = file.collectElementsOfType(ObjJClassDeclarationElement::class.java)
            CachedValueProvider.Result.create(declarations, dependencies)
        }
        manager.createCachedValue(provider)
    }

    private val classDependenciesCachedValue:CachedValue<List<ObjJClassDependencyStatement>> by lazy {
        val provider = CachedValueProvider {
            val classNameReferences = file.collectElementsOfType(ObjJClassDependencyStatement::class.java)
            CachedValueProvider.Result.create(classNameReferences, dependencies)
        }
        manager.createCachedValue(provider)
    }

    /**
     * Collects all
     */
    private val insideClassesCachedValue:CachedValue<List<ObjJClassName>> by lazy {
        val provider = CachedValueProvider {
            val classDeclarationNames = file.classDeclarations
                .mapNotNull { it.getClassName() }
            val atClassReferences = file.classDependencyStatements
                    .mapNotNull { it.className }
            val classNames = classDeclarationNames + atClassReferences
            CachedValueProvider.Result.create(classNames, dependencies)
        }
        manager.createCachedValue(provider)
    }
}