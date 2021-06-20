package cappuccino.ide.intellij.plugin.utils

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.io.File
import java.util.logging.Logger

private val LOGGER: Logger = Logger.getLogger("#" + ObjJFileUtil::class.java.canonicalName)

val VirtualFile.contents: String
    get() {
        return VfsUtilCore.loadText(this)
    }

fun VirtualFile.getModule(project: Project): Module? {
    return ModuleUtil.findModuleForFile(this, project)
}

object ObjJVirtualFileUtil {
    fun findFileByPath(path: String, refreshIfNecessary: Boolean = true): VirtualFile? {
        val file = File(path)
        if (!file.exists())
            return null
        return VfsUtil.findFileByIoFile(file, refreshIfNecessary)
    }
}

val Document.virtualFile
    get() = FileDocumentManager.getInstance().getFile(this)

val Editor.virtualFile
    get() = document.virtualFile

fun VirtualFile.getPsiFile(project: Project): PsiFile? = PsiManager.getInstance(project).findFile(this)


object ObjJFileUtil {
    private const val RESOURCES_FOLDER = "classes"

    private val PLUGIN_HOME_FILE: File?
        get() = PLUGIN?.path

    val PLUGIN_HOME_DIRECTORY: VirtualFile?
        get() {
            val file = PLUGIN_HOME_FILE
            if (file == null) {
                LOGGER.severe("Failed to locate plugin home path")
                return null
            }
            if (file.extension == "jar") {
                val jar = VfsUtil.findFileByIoFile(file, true)
                        ?: return null
                return JarFileSystem.getInstance().getJarRootForLocalFile(jar)
            }
            val libFolder = VfsUtil.findFileByIoFile(file, true)?.findChild("lib")
                    ?: VfsUtil.findFileByIoFile(file, true)?.findChild("Cappuccino Plugin")?.findChild("lib")
                    ?: return DEBUG_PLUGIN_HOME_DIRECTORY
            val jar = libFolder.findChild("Cappuccino Plugin.jar") ?: return DEBUG_PLUGIN_HOME_DIRECTORY
            return JarFileSystem.getInstance().getJarRootForLocalFile(jar) ?: DEBUG_PLUGIN_HOME_DIRECTORY
        }

    private val DEBUG_PLUGIN_HOME_DIRECTORY: VirtualFile?
        get() {
            val file = PLUGIN_HOME_FILE ?: return null
            return VfsUtil.findFileByIoFile(file, true)?.findChild("classes")
        }

    private val PLUGIN_RESOURCES_DIRECTORY: VirtualFile?
        get() = PLUGIN_HOME_DIRECTORY

    fun getPluginResourceFile(relativePath: String): VirtualFile? {
        return PLUGIN_RESOURCES_DIRECTORY?.findFileByRelativePath(relativePath)
    }

}
