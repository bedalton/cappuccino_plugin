package cappuccino.ide.intellij.plugin.utils

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.lang.ObjJFileType
import cappuccino.ide.intellij.plugin.psi.ObjJFrameworkReference
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportStatement
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.getSelfOrParentOfType
import cappuccino.ide.intellij.plugin.utils.ObjJImportUtils.getEnclosingFrameworkName
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopeUtil
import com.intellij.psi.search.GlobalSearchScopes
import java.util.regex.Pattern

object ObjJImportUtils {
    private val bundleRegex = Pattern.compile("<key>CPBundleName</key>\\s*<string>([^<]+?)</string>")

    val EMPTY_FRAMEWORK_NAME = "<*>"

    fun frameworkSearchRegex(frameworkName:String) = """<key>CPBundleName</key>\s*<string>\s*$frameworkName\s*</string>""".toRegex()

    fun frameworkNames(project: Project):List<String> {
        return FilenameIndex.getFilesByName(project, "Info.plist", GlobalSearchScope.everythingScope(project)).mapNotNull { file ->
            val matcher = bundleRegex.matcher(file.text)
            if (matcher.find()) {
                val match = matcher.group(1)
                match
            } else {
                null
            }
        }
    }

    fun getFrameworkNameInDirectory(project: Project, directory:PsiDirectory):List<String> {
        val searchScope = GlobalSearchScopes.directoryScope(directory, false)
        val frameworkNames = FilenameIndex.getFilesByName(project, "Info.plist", searchScope).mapNotNull { file ->
            val matcher = bundleRegex.matcher(file.text)
            if (matcher.find()) {
                val match = matcher.group(1)
                match
            } else
                null
        }
        if (frameworkNames.isNotEmpty())
            return frameworkNames
        val parent = directory.parent ?: return listOf("*")
        return getFrameworkNameInDirectory(project, parent)
    }

    fun getEnclosingFrameworkName(psiElement: PsiElement) : String {
        val parentDirectory = psiElement.containingFile.containingDirectory ?: return EMPTY_FRAMEWORK_NAME
        return getFrameworkNameInDirectory(psiElement.project, parentDirectory).firstOrNull() ?: EMPTY_FRAMEWORK_NAME
    }

    fun getImportedFiles(thisFile:ObjJFile) : List<ObjJFile> {
        val project = thisFile.project
        val files:List<ObjJFile> = thisFile.getFileChildrenOfType(ObjJImportStatement::class.java, true).flatMap {
            getFrameworkDirectory(project, it.frameworkNameString).flatMap {directory ->
                val searchScope = GlobalSearchScopes.directoryScope(directory, true)
                FilenameIndex.getFilesByName(project, it.fileNameString, searchScope).mapNotNull { it as? ObjJFile}
            }
        }
        return files + files.flatMap {
            it.getImportedFiles()
        }
    }

    fun getFrameworkDirectory(project: Project, frameworkName:String) : List<PsiDirectory> {
        val searchRegex = frameworkSearchRegex(frameworkName)
        return FilenameIndex.getFilesByName(project, "Info.plist", GlobalSearchScope.everythingScope(project)).filter { file ->
            searchRegex.containsMatchIn(file.text)
        }.map {
            it.containingDirectory
        }
    }

    fun getFrameworkFileNames(project: Project, frameworkName: String) : List<String> {
        LOGGER.info("Getting file names for framework: $frameworkName")
        return getFrameworkDirectory(project, frameworkName).flatMap { directory ->
            getFileNamesInDirectory(directory, true)
        }
    }

    fun getFileNamesInDirectory(directory: PsiDirectory?, recursive:Boolean = true) : List<String> {
        if (directory == null) {
            return emptyList()
        }
        val searchScope = GlobalSearchScopes.directoryScope(directory, recursive)
        return FilenameIndex.getAllFilesByExt(directory.project, "j", searchScope).mapNotNull {
            it.name
        }
    }

}

val PsiElement.enclosingFrameworkName:String
    get() = getEnclosingFrameworkName(this)