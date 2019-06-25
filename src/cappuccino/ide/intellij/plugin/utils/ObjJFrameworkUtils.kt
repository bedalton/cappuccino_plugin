package cappuccino.ide.intellij.plugin.utils

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
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

val PsiElement.enclosingFrameworkName:String
    get() = (containingFile as? ObjJFile)?.frameworkName ?: ObjJFrameworkUtils.getEnclosingFrameworkName(this)

val PsiFile.frameworkName:String
    get() = (this as? ObjJFile)?.frameworkName ?: ObjJFrameworkUtils.getEnclosingFrameworkName(this)

fun findFrameworkNameInPlist(plist:PsiFile?) : String? {
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

fun findFrameworkNameInPlistText(plistText:String) : String? {
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

fun createFrameworkSearchRegex(frameworkName:String) = """<key>CPBundleName</key>\s*<string>\s*$frameworkName\s*</string>""".toRegex()


object ObjJFrameworkUtils {


    fun frameworkNames(project: Project):List<String> {
        return FilenameIndex.getFilesByName(project, INFO_PLIST_FILE_NAME, GlobalSearchScope.everythingScope(project)).mapNotNull { file ->
            findFrameworkNameInPlist(file)
        }
    }

    fun getEnclosingFrameworkName(file: PsiFile) : String {
        val parentDirectory = file.containingDirectory ?: return EMPTY_FRAMEWORK_NAME
        return getFrameworkNameInDirectory(file.project, parentDirectory).firstOrNull() ?: EMPTY_FRAMEWORK_NAME
    }

    fun getEnclosingFrameworkName(psiElement: PsiElement) : String {
        val parentDirectory = psiElement.containingFile.containingDirectory ?: return EMPTY_FRAMEWORK_NAME
        return getFrameworkNameInDirectory(psiElement.project, parentDirectory).firstOrNull() ?: EMPTY_FRAMEWORK_NAME
    }

    private fun getFrameworkNameInDirectory(project: Project, directory: PsiDirectory):List<String> {
        val searchScope = GlobalSearchScopes.directoryScope(directory, false)
        val frameworkNames = FilenameIndex.getFilesByName(project, INFO_PLIST_FILE_NAME, searchScope).mapNotNull { file ->
            findFrameworkNameInPlist(file)
        }
        if (frameworkNames.isNotEmpty())
            return frameworkNames
        val parent = directory.parent ?: return listOf("*")
        return getFrameworkNameInDirectory(project, parent)
    }

    fun getFrameworkFileNames(project: Project, frameworkName: String) : List<String> {
        return getFrameworkDirectory(project, frameworkName).flatMap { directory ->
            getFileNamesInDirectory(directory, true)
        }
    }

    private fun getFrameworkDirectory(project: Project, frameworkName:String) : List<PsiDirectory> {
        return FilenameIndex.getFilesByName(project, INFO_PLIST_FILE_NAME, GlobalSearchScope.everythingScope(project)).filter { file ->
            frameworkName == findFrameworkNameInPlist(file)
        }.map {
            it.containingDirectory
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