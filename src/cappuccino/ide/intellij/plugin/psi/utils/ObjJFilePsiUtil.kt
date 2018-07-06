package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportStatement
import cappuccino.ide.intellij.plugin.utils.ArrayUtils

import java.nio.file.FileSystems
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern

object ObjJFilePsiUtil {

    private val LOGGER = Logger.getLogger(ObjJFilePsiUtil::class.java.name)
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
            //LOGGER.log(Level.INFO, "Failed to find base file path for file: <$filePath>")
            return null
        }
        val pathComponents = Arrays.asList(*filePath.substring(basePath.length).split(SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        var plistFile: VirtualFile? = null
        while (pathComponents.size > 0) {
            val path = basePath + "/" + ArrayUtils.join(pathComponents, SEPARATOR, true) + "info.plist"
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
