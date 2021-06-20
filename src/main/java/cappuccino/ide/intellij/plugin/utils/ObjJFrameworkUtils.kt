package cappuccino.ide.intellij.plugin.utils

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopes
import java.util.regex.Pattern

private val bundleRegex = Pattern.compile("<key>CPBundleName</key>\\s*<string>([^<]+?)</string>")
private val isFrameworkPlistRegex = "<key>CPBundlePackageType</key>\\s*<string>FMWK</string>".toRegex()

const val EMPTY_FRAMEWORK_NAME = "%@"

const val INFO_PLIST_FILE_NAME = "Info.plist"
val INFO_PLIST_FILE_NAME_TO_LOWER_CASE = INFO_PLIST_FILE_NAME.toLowerCase()

val FRAMEWORK_NAME_KEY = Key<String>("objj.userdata.keys.FRAMEWORK_NAME")

val PsiElement.enclosingFrameworkName: String
    get() = (containingFile as? ObjJFile)?.frameworkName ?: ObjJFrameworkUtils.getEnclosingFrameworkName(this)

fun findFrameworkNameInPlist(plist: PsiFile?): String? {
    if (plist == null)
        return null
    var frameworkName = plist.getUserData(FRAMEWORK_NAME_KEY)
    if (frameworkName != null)
        return frameworkName
    frameworkName = findFrameworkNameInPlistText(plist.text)
    if (frameworkName != null)
        plist.putUserData(FRAMEWORK_NAME_KEY, frameworkName)
    return frameworkName
}

fun findFrameworkNameInPlistText(plistText: String): String? {
    if (!isFrameworkPlistRegex.containsMatchIn(plistText))
        return null
    val matcher = bundleRegex.matcher(plistText)
    return if (matcher.find()) {
        val match = matcher.group(1)
        match
    } else {
        null
    }
}

fun createFrameworkSearchRegex(frameworkName: String) = """<key>CPBundleName</key>\s*<string>\s*$frameworkName\s*</string>""".toRegex()


object ObjJFrameworkUtils {


    fun frameworkNames(project: Project): List<String> {
        return FilenameIndex.getFilesByName(project, INFO_PLIST_FILE_NAME, GlobalSearchScope.everythingScope(project)).mapNotNull { file ->
            findFrameworkNameInPlist(file)
        }
    }

    fun getEnclosingFrameworkName(file: PsiFile): String {
        val parentDirectory = (file.containingDirectory ?: file.originalFile.containingDirectory)
        var frameworkName: String?
        if (parentDirectory != null) {
            frameworkName = if (DumbService.isDumb(file.project)) {
                getFrameworkNameInDirectoryDumb(file.project, parentDirectory)
            } else {
                getFrameworkNameInDirectory(file.project, parentDirectory).firstOrNull()
            }
            if (frameworkName != null)
                return frameworkName
        }
        val virtualParent = file.virtualFile?.parent ?: file.originalFile.virtualFile?.parent
        if (virtualParent != null) {
            frameworkName = getEnclosingFrameworkNameFromVirtualFile(file.project, virtualParent)
            if (frameworkName != null)
                return frameworkName
        }
        return EMPTY_FRAMEWORK_NAME
    }

    fun getEnclosingFrameworkName(psiElement: PsiElement): String {
        val parentDirectory = psiElement.containingFile.containingDirectory ?: return EMPTY_FRAMEWORK_NAME
        return getFrameworkNameInDirectory(psiElement.project, parentDirectory).firstOrNull() ?: EMPTY_FRAMEWORK_NAME
    }

    private fun getFrameworkNameInDirectory(project: Project, directory: PsiDirectory): List<String> {
        if (DumbService.isDumb(project))
            return listOfNotNull(getFrameworkNameInDirectoryDumb(project, directory))
        val searchScope = GlobalSearchScopes.directoryScope(directory, false)
        val frameworkNames = FilenameIndex.getFilesByName(project, INFO_PLIST_FILE_NAME, searchScope).mapNotNull { file ->
            findFrameworkNameInPlist(file)
        }
        if (frameworkNames.isNotEmpty())
            return frameworkNames
        val parent = directory.parent ?: return listOf(EMPTY_FRAMEWORK_NAME)
        return getFrameworkNameInDirectory(project, parent)
    }

    private fun getFrameworkNameInDirectoryDumb(project: Project, directory: PsiDirectory): String? {
        var currentDirectory: PsiDirectory? = directory
        var plist: PsiFile?
        while (currentDirectory != null && currentDirectory != project.guessProjectDir()) {
            plist = currentDirectory.findFile(INFO_PLIST_FILE_NAME)
                    ?: directory.findFile(INFO_PLIST_FILE_NAME_TO_LOWER_CASE)
            if (plist != null) {
                val frameworkName = findFrameworkNameInPlist(plist)
                if (frameworkName != null) {
                    return frameworkName
                }
            }
            currentDirectory = currentDirectory.parentDirectory ?: break
        }
        return null
    }

    private fun getEnclosingFrameworkNameFromVirtualFile(project: Project, virtualFile: VirtualFile?): String? {
        if (virtualFile == null) {
            return null
        }
        var currentDirectory: VirtualFile? = if (virtualFile.isDirectory) virtualFile else virtualFile.parent
        var plist: PsiFile? = null
        while (currentDirectory != null && currentDirectory != project.guessProjectDir() && plist == null) {
            plist = (currentDirectory.findChild(INFO_PLIST_FILE_NAME)
                    ?: currentDirectory.findChild(INFO_PLIST_FILE_NAME_TO_LOWER_CASE))?.getPsiFile(project)
            if (plist != null) {
                val frameworkName = findFrameworkNameInPlist(plist)
                if (frameworkName != null)
                    return frameworkName
            }
            currentDirectory = currentDirectory.parent ?: break
        }
        return null
    }

    fun getFrameworkFileNames(project: Project, frameworkName: String): List<String> {
        return getFrameworkDirectory(project, frameworkName).flatMap { directory ->
            getFileNamesInDirectory(directory, true)
        }
    }

    private fun getFrameworkDirectory(project: Project, frameworkName: String): List<PsiDirectory> {
        return FilenameIndex.getFilesByName(project, INFO_PLIST_FILE_NAME, GlobalSearchScope.everythingScope(project)).filter { file ->
            frameworkName == findFrameworkNameInPlist(file)
        }.map {
            it.containingDirectory
        }
    }

    fun getFileNamesInEnclosingDirectory(file: PsiFile, recursive: Boolean = true): List<String> {
        var directory: PsiDirectory? = file.originalFile.containingDirectory ?: return emptyList()
        val files = mutableListOf<String>()
        val projectDirectory = file.project.guessProjectDir()
        while (directory != null) {
            files.addAll(getFileNamesInDirectory(directory, false))
            if (!recursive)
                break
            if (directory.findSubdirectory("Framework") != null || directory == projectDirectory)
                break
            directory = directory.parent ?: break
        }
        return files
    }

    fun getFileNamesInDirectory(directory: PsiDirectory?, recursive: Boolean = true): List<String> {
        if (directory == null) {
            return emptyList()
        }
        val searchScope = GlobalSearchScopes.directoryScope(directory, recursive)
        return FilenameIndex.getAllFilesByExt(directory.project, "j", searchScope).mapNotNull {
            it.name
        }
    }

}