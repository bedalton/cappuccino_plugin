@file:Suppress("unused")

package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportStatement
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import cappuccino.ide.intellij.plugin.utils.INFO_PLIST_FILE_NAME
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference
import javafx.scene.control.ProgressIndicator

import java.nio.file.FileSystems
import java.util.*
import java.util.logging.Logger
import java.util.regex.Pattern


/**
 * @todo implement class to resolve imports and framework names
 */
object ObjJFilePsiUtil {

    private val LOGGER by lazy {Logger.getLogger(ObjJFilePsiUtil::class.java.name)}
    private val SEPARATOR = FileSystems.getDefault().separator
    private val INFO_PLIST_DICT_PATTERN = Pattern.compile(".*<dict>(.*)</dict>.*")
    private val INFO_PLIST_PROPERTY_PATTERN = Pattern.compile("(<key>(.*)</key>\n<[^>]+>(.*)</[^>]+>)*")

    fun getContainingFrameworkName(file: ObjJFile): String? {
        val filePath = file.virtualFile.path
        val srcRoots = ProjectRootManager.getInstance(file.project).contentSourceRoots
        var basePath: String? = null
        for (srcRoot in srcRoots) {
            if (filePath.startsWith(srcRoot.path)) {
                basePath = srcRoot.path
                break
            }
        }
        if (basePath == null) {
            return null
        }
        val pathComponents = mutableListOf(*filePath.substring(basePath.length).split(SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        var plistFile: VirtualFile? = null
        while (pathComponents.size > 0) {
            val path = basePath + "/" + ArrayUtils.join(pathComponents, SEPARATOR, true) + INFO_PLIST_FILE_NAME
            //LOGGER.log(Level.INFO, "Checking for info.plist at location: <$path>")
            plistFile = LocalFileSystem.getInstance().findFileByPath(path)
            if (plistFile != null) {
                break
            }
            pathComponents.removeAt(pathComponents.size - 1)
        }
        return if (plistFile == null) {
            null
        } else null
    }


    private fun getInfoPlistProperties(virtualFile: VirtualFile?): Map<String, String>? {
        return if (virtualFile == null) {
            emptyMap()
        } else null
    }

    /**
     * Gets all import statements in an ObjJFile as strings
     * Useful when determining what files have been imported for class resolution
     */
    fun getImportsAsStrings(file: ObjJFile): List<String> {
        val importStatements = file.getChildrenOfType( ObjJImportStatement::class.java)
        if (importStatements.isEmpty()) {
            return ArrayUtils.EMPTY_STRING_ARRAY
        }
        val out = ArrayList<String>()
        for (importStatement in importStatements) {
            out.add(importStatement.importAsUnifiedString)
        }
        return out
    }


}

fun ObjJFile.getImportedFiles(recursive: Boolean, cache: Boolean? = null): List<ObjJFile> {
    val out = mutableListOf<ObjJFile>()
    getImportedFiles(this, recursive, cache, out)
    return out
}

private fun getImportedFiles(fileIn:ObjJFile, recursive:Boolean, cache:Boolean? = null, out:MutableList<ObjJFile>) {
    if (cache.orFalse()) {
        fileIn.cachedImportFileList?.forEach { file ->
            ProgressIndicatorProvider.checkCanceled()
            addToImportsList(file, recursive, cache, out)
        }
    } else {
        collectImports(fileIn).forEach { importStatement ->
            ProgressIndicatorProvider.checkCanceled()
            val reference = getReference(importStatement) ?: return@forEach
            val files = if (reference is PsiPolyVariantReference)
                reference.multiResolve(true).mapNotNull { it.element as? ObjJFile }
            else {
                listOfNotNull(reference.resolve() as? ObjJFile)
            }
            for (file in files) {
                addToImportsList(file, recursive, cache, out)
            }
        }
    }
}

private fun getReference(importStatement:ObjJImportStatement<*>) : PsiReference? {
    return when(importStatement) {
        is ObjJImportFile -> importStatement.fileNameAsImportString.reference
        is ObjJIncludeFile -> importStatement.fileNameAsImportString.reference
        is ObjJImportFramework -> importStatement.frameworkReference.frameworkFileName?.reference
        is ObjJIncludeFramework -> importStatement.frameworkReference.frameworkFileName?.reference
        else -> null
    }
}

private fun addToImportsList(file:ObjJFile, recursive:Boolean, cache:Boolean? = null, out:MutableList<ObjJFile>) {
    ProgressIndicatorProvider.checkCanceled()
    if (out.contains(file))
        return
    out.add(file)
    if (recursive.orFalse())
        getImportedFiles(file, recursive, cache, out)
}

private fun collectImports(psiFile: PsiFile) : List<ObjJImportStatement<*>> {

    return psiFile.getChildrenOfType(ObjJImportBlock::class.java).flatMap {block ->
        block.getChildrenOfType(ObjJImportStatementElement::class.java).mapNotNull {
            it.importFile ?: it.importFramework
        }
    } + psiFile.getChildrenOfType(ObjJIncludeBlock::class.java).flatMap { block ->
        block.getChildrenOfType(ObjJIncludeStatementElement::class.java).mapNotNull {
            it.includeFile ?: it.includeFramework
        }
    }
}