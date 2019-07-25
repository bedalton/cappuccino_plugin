package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportIncludeStatement
import cappuccino.ide.intellij.plugin.utils.frameworkName
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

/**
 * Holder for psi file utils
 */
object ObjJPsiFileUtil {
    /**
     * Gets containing file name in a safe way
     * @todo see if file name does need to be safely retrieved
     */
    fun getContainingFileName(psiElement: PsiElement?): String {
        return getFileNameSafe(psiElement?.containingFile)
    }

    /**
     * Gets a file name while trying to avoid null pointer exceptions
     * Was more necessary before Kotlin implementation
     * might still be necessary though
     */
    @JvmOverloads
    fun getFileNameSafe(psiFile: PsiFile?, defaultValue: String? = null, includePath: Boolean = false): String {
        if (psiFile == null) {
            return defaultValue ?: ""
        }

        if (!includePath) {
            return psiFile.name
        }

        if (psiFile.virtualFile != null) {
            psiFile.virtualFile.path
        }
        val fileName = psiFile.name
        return if (fileName.isNotEmpty()) fileName else defaultValue ?: ""
    }

    /**
     * Checks if a given file is a definition element
     */
    fun isDefinitionElement(psiElement: PsiElement): Boolean {
        return getFileNameSafe(psiElement.containingFile)?.endsWith(".d.j") == true
    }

    fun getImportedClassNames(fileIn:ObjJFile) : List<String> {
        return fileIn.getImportedFiles(
                recursive = true,
                cache = true
        ).flatMap { file ->
            file.classDeclarations.map { it.classNameString }
        }
    }
}


/**
 * Helper infix operator to check if psi elements are NOT in the same file
 */
infix fun PsiElement?.notInSameFile(otherElement: PsiElement?): Boolean {
    return !sharesSameFile(this, otherElement)
}

/**
 * Helper infix operator to check if psi elements are in the same file
 */
infix fun PsiElement?.inSameFile(otherElement: PsiElement?): Boolean {
    return sharesSameFile(this, otherElement)
}

/**
 * Checks whether two files share the same containing file
 */
fun sharesSameFile(element1: PsiElement?, element2: PsiElement?): Boolean {
    val file1 = element1?.containingFile ?: return false
    val file2 = element2?.containingFile ?: return false
    return file1.isEquivalentTo(file2) && file1.virtualFile?.path == file2.virtualFile?.path
}

/**
 * Helper extension function to get containing file name from any element
 */
val PsiElement.containingFileName: String
    get() = ObjJPsiFileUtil.getContainingFileName(this)


/**
 * Gets all imports, potentially recursively
 * @param recursive whether to fetch imports recursively through imported files
 * @param cache whether to use the imports cache in the file element
 */
fun ObjJFile.getImportedFiles(recursive: Boolean, cache: Boolean? = null): List<ObjJFile> {
    val out = mutableListOf<ObjJFile>()
    getImportedFiles(
            fileIn = this,
            recursive = recursive,
            cache = cache,
            out = out
    )
    return out
}

/**
 * Finds all imports (potentially recursively)
 * and adds them to the mutable array parameter
 * @param fileIn file to find imports for
 * @param recursive whether to fetch imports recursively through imported files
 * @param cache whether to use the imports cache in the file element
 * @param out mutable list to add all imports to (necessary to pass in to prevent recursion loops)
 */
private fun getImportedFiles(fileIn: ObjJFile, recursive: Boolean, cache: Boolean? = null, out: MutableList<ObjJFile>) {
    if (cache.orFalse()) {
        fileIn.cachedImportFileList?.forEach { file ->
            ProgressIndicatorProvider.checkCanceled()
            addToImportsList(
                    file = file,
                    recursive = recursive,
                    cache = cache,
                    out = out
            )
        }
    } else {
        collectImports(fileIn).forEach { importStatement ->
            ProgressIndicatorProvider.checkCanceled()
            val files = importStatement.multiResolve()
            for (file in files) {
                addToImportsList(
                        file = file,
                        recursive = recursive,
                        cache = cache,
                        out = out
                )
            }
        }
    }
}

/**
 * Finds all imports (potentially recursively)
 * and adds them to the mutable array parameter
 * @param file file to add to imports array
 * @param recursive whether to fetch imports recursively through imported files
 * @param cache whether to use the imports cache in the file element
 * @param out mutable list to add all imports to (necessary to pass in to prevent recursion loops)
 */
private fun addToImportsList(file: ObjJFile, recursive: Boolean, cache: Boolean? = null, out: MutableList<ObjJFile>) {
    // Ensure loop does not cause lockups
    ProgressIndicatorProvider.checkCanceled()
    // Ensure file is not already in list
    if (out.contains(file))
        return
    out.add(file)
    // add imported files' imports
    if (recursive) {
        getImportedFiles(
                fileIn = file,
                recursive = recursive,
                cache = cache,
                out = out
        )
    }
}

/**
 * Collects all import elements from within a given psi file
 * @param psiFile file to search
 */
fun collectImports(psiFile: PsiFile): Collection<ObjJImportIncludeStatement> {
    val collectedImports = psiFile.collectElementsOfType(ObjJImportIncludeStatement::class.java)
    if (collectedImports.isNotEmpty()) {
        return collectedImports
    }
    return psiFile.getChildrenOfType(ObjJImportBlock::class.java).flatMap { block ->
        block.getChildrenOfType(ObjJImportIncludeStatement::class.java)
    }
    // todo handle nested import statements in preproc if statements
}

val PsiFile.fileNameAsImportString:String get(){
    val frameworkName:String = frameworkName
    val fileName = name
    return "<$frameworkName/$fileName>"
}

fun <PsiT:PsiElement>PsiFile.collectElementsOfType(classType:Class<PsiT>) : List<PsiT> {
    return PsiTreeUtil.collectElements(this) { element:PsiElement ->
        classType.isInstance(element)
    }.mapNotNull {
        classType.cast(it)
    }
}