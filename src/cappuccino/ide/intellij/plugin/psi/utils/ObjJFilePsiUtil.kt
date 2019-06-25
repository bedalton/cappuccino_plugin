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
import com.intellij.psi.augment.PsiAugmentProvider
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

fun collectImports(psiFile: PsiFile) : List<ObjJImportStatement<*>> {
    val augmentCollected = PsiAugmentProvider.collectAugments(psiFile, ObjJImportStatementElement::class.java)
    val includeCollected = PsiAugmentProvider.collectAugments(psiFile, ObjJIncludeStatementElement::class.java)
    if (augmentCollected.size > 0 || includeCollected.size > 0) {
        LOGGER.info("Found elements through augmented collector")
        return augmentCollected.mapNotNull {
            it.importFile ?: it.importFramework
        } + includeCollected.mapNotNull { it.includeFile ?: it.includeFramework }
    }
    return psiFile.getChildrenOfType(ObjJImportBlock::class.java).flatMap {block ->
        block.getChildrenOfType(ObjJImportStatementElement::class.java).mapNotNull {
            it.importFile ?: it.importFramework
        }
    } + psiFile.getChildrenOfType(ObjJIncludeBlock::class.java).flatMap { block ->
        block.getChildrenOfType(ObjJIncludeStatementElement::class.java).mapNotNull {
            it.includeFile ?: it.includeFramework
        }
    }
    // todo handle nested import statements in preproc if statements
}