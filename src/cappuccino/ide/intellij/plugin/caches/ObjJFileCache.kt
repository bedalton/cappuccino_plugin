package cappuccino.ide.intellij.plugin.caches

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.utils.getImportedFiles
import cappuccino.ide.intellij.plugin.utils.INFO_PLIST_FILE_NAME
import cappuccino.ide.intellij.plugin.utils.INFO_PLIST_FILE_NAME_TO_LOWER_CASE
import cappuccino.ide.intellij.plugin.utils.findFrameworkNameInPlistText
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker

class ObjJFileCache(file:ObjJFile) {
    private val manager by lazy { CachedValuesManager.getManager(file.project) }
    private val modificationTracker by lazy { MyModificationTracker() }
    private val dependencies:List<Any> by lazy {listOf(modificationTracker, PsiModificationTracker.MODIFICATION_COUNT)}
    private var cachedFrameworkName:String? = null
    val frameworkName:String?
        get() = cachedFrameworkName ?: findFrameworkNameInPlistText(frameworkPlist.value?.text.orEmpty())
    /**
     * Gets the imported files
     */
    val importedFiles:List<ObjJFile>? get() = importedFilesCachedValue.value

    /**
     * Creates an imported files cache
     */
    private val importedFilesCachedValue:CachedValue<List<ObjJFile>> by lazy {
        val provider = CachedValueProvider<List<ObjJFile>> {
            val files = file.getImportedFiles(recursive = true, cache = false)
            CachedValueProvider.Result.create(files, dependencies)
        }
        manager.createCachedValue(provider)
    }

    private val frameworkPlist:CachedValue<PsiFile?> by lazy {
        val provider = CachedValueProvider<PsiFile> {
            var out:PsiFile? = null
            val directory:PsiDirectory? = file.parent
            while(directory != null) {
                val temp = directory.findFile(INFO_PLIST_FILE_NAME) ?: directory.findFile(INFO_PLIST_FILE_NAME_TO_LOWER_CASE)
                cachedFrameworkName = findFrameworkNameInPlistText(temp?.text.orEmpty())
                if (cachedFrameworkName != null) {
                    out = temp
                    break
                }
            }
            CachedValueProvider.Result.create(out, dependencies)
        }
        manager.createCachedValue(provider)
    }

}