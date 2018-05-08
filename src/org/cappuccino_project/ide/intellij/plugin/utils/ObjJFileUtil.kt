package org.cappuccino_project.ide.intellij.plugin.utils

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJImportStatement
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil

import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern

class ObjJFileUtil {

    /**
     * Gets a list of PSI files for the names of strings given
     * Todo: actually get imported files.
     * @param importedFileNames list of file names specified in import statements
     * @param project project to get files from
     * @return files for import file names
     */
    fun getImportedFiles(importedFileNames: List<String>, project: Project): List<ObjJFile> {
        return EMPTY_FILE_LIST
    }

    fun getImportsAsMap(file: PsiFile): Map<String, List<String>> {
        val out = HashMap<String, List<String>>()
        getImportsAsMap(file, out)
        return out
    }

    private fun getImportsAsMap(file: PsiFile, imports: MutableMap<String, List<String>>) {
        val importStatements = ObjJTreeUtil.getChildrenOfTypeAsList(file, ObjJImportStatement<*>::class.java)
        val checked = ArrayList<String>()
        val project = file.project
        val searchScope = GlobalSearchScope.everythingScope(project)
        for (importStatement in importStatements) {
            val framework = importStatement.frameworkName
            val fileName = importStatement.fileName
            if (!addImport(imports, framework, fileName)) {
                continue
            }
            val possibleFiles = FilenameIndex.getFilesByName(project, fileName, searchScope)
            for (possibleImportedFile in possibleFiles) {
                if (framework != null && !framework.isEmpty()) {
                    var directory: PsiDirectory? = possibleImportedFile.containingDirectory
                    while (directory != null) {
                        val directoryName = directory.name
                        if (directoryName == framework) {
                            getImportsAsMap(possibleImportedFile, imports)
                            directory = null
                        } else {
                            directory = directory.parentDirectory
                        }
                    }
                    break
                }
                if (possibleImportedFile.containingDirectory.isEquivalentTo(file.containingDirectory)) {
                    getImportsAsMap(possibleImportedFile, imports)
                    break
                }
            }
        }
    }

    fun addImport(imports: MutableMap<String, List<String>>,
                  framework: String?,
                  fileName: String
    ): Boolean {
        var framework = framework
        if (framework == null || framework.isEmpty()) {
            framework = FILE_PATH_KEY
        }
        val files = if (imports.containsKey(framework)) imports[framework] else ArrayList()
        if (files.contains(fileName)) {
            return false
        }
        files.add(fileName)
        imports[framework] = files
        return true
    }

    fun inList(file: ObjJFile, filePaths: List<Pattern>): Boolean {
        val thisPath = file.virtualFile.path
        for (pattern in filePaths) {
            if (pattern.matcher(thisPath).matches()) {
                return true
            }
        }
        return false
    }

    companion object {

        private val EMPTY_FILE_LIST = emptyList<ObjJFile>()
        val FILE_PATH_KEY = "__FILE__"

        fun getContainingFileName(psiElement: PsiElement?): String? {
            return getFileNameSafe(psiElement?.containingFile)
        }

        @JvmOverloads
        fun getFileNameSafe(psiFile: PsiFile?, defaultValue: String? = null, includePath: Boolean = false): String? {
            if (psiFile == null) {
                return defaultValue
            }
            if (psiFile.virtualFile != null) {
                return if (includePath) {
                    psiFile.virtualFile.path
                } else psiFile.virtualFile.name
            }
            val fileName = psiFile.originalFile.name
            return if (!fileName.isEmpty()) fileName else defaultValue
        }

        fun getFilePath(psiFile: PsiFile?, defaultValue: String?): String? {
            if (psiFile == null) {
                return defaultValue
            }
            if (psiFile.virtualFile != null) {
                return psiFile.virtualFile.path
            }
            try {
                return psiFile.originalFile.virtualFile.path
            } catch (ignored: Exception) {
            }

            return defaultValue
        }
    }

}
