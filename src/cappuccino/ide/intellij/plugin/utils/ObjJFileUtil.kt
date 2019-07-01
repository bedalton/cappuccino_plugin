package cappuccino.ide.intellij.plugin.utils

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.io.File


val VirtualFile.contents:String get() {
    return VfsUtilCore.loadText(this)
}

fun VirtualFile.getModule(project:Project) : Module? {
    return ModuleUtil.findModuleForFile(this, project)
}

object ObjJVirtualFileUtil {
    fun findFileByPath(path:String, refreshIfNecessary:Boolean = true) : VirtualFile? {
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

object ObjJFileUtil {

    private const val PLUGIN_ID = "cappuccino.intellij.plugin"

    private const val RESOURCES_FOLDER = "classes"

    private val PLUGIN_HOME_FILE:File?
        get() = PluginManager.getPlugin(PluginId.getId(PLUGIN_ID))?.path

    val PLUGIN_HOME_DIRECTORY:VirtualFile? get() {
        val file = PLUGIN_HOME_FILE ?: return null
        return VfsUtil.findFileByIoFile(file, true)
    }

    private val PLUGIN_RESOURCES_DIRECTORY: VirtualFile?
        get() = PLUGIN_HOME_DIRECTORY?.findChild(RESOURCES_FOLDER)

    fun getPluginResourceFile(relativePath:String): VirtualFile? {
        return PLUGIN_RESOURCES_DIRECTORY?.findFileByRelativePath(relativePath)
    }

}
