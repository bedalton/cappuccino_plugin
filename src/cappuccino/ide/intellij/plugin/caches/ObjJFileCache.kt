package cappuccino.ide.intellij.plugin.caches

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.utils.getImportedFiles
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker

class ObjJFileCache(file:ObjJFile) {
    private val manager by lazy { CachedValuesManager.getManager(file.project) }
    private val modificationTracker by lazy { MyModificationTracker() }
    private val dependencies:List<Any> by lazy {listOf(modificationTracker, PsiModificationTracker.MODIFICATION_COUNT)}

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

    /**
     * Gets the imported files
     */
    val importedFiles:List<ObjJFile>? get() = importedFilesCachedValue.value


}