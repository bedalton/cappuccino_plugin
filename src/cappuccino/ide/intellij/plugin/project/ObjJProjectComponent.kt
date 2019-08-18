package cappuccino.ide.intellij.plugin.project

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFileType
import cappuccino.ide.intellij.plugin.lang.ObjJFileType
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.getModule
import cappuccino.ide.intellij.plugin.utils.virtualFile
import com.intellij.ProjectTopics
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.util.logging.Logger


class ObjJProjectComponent(project: Project) : ProjectComponent {

    init {
        registerFileOpenHandler(project)
        registerProjectRootChangeListener(project)
        notifyUpdate(project)
    }

    private fun registerFileOpenHandler(project: Project) {
        val bus = project.getMessageBus()
        bus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun fileOpened(editorManager: FileEditorManager, file: VirtualFile) {
                val extension = file.extension
                if (extension != ObjJFileType.FILE_EXTENSION && extension != JsTypeDefFileType.FILE_EXTENSION)
                    return
                val module = file.getModule(project)
                if (module != null) {
                    JsTypeDefBundledSourcesRegistrationUtil.register(module, project)
                    initFrameworkDefaults(editorManager.selectedTextEditor, file)
                }
            }
        })
    }

    private fun initFrameworkDefaults(editor: Editor?, file: VirtualFile) {
        val editorVirtualFile = editor?.virtualFile ?: return
        if (editorVirtualFile.path != file.path)
            return
    }

    private fun registerProjectRootChangeListener(project: Project) {
        project.messageBus.connect().subscribe(ProjectTopics.PROJECT_ROOTS, ObjJSdkProjectRootsChangeListener)
    }

    override fun projectOpened() {
        super.projectOpened()

    }

    private fun notifyUpdate(project: Project) {
        if (ObjJPluginSettings.pluginUpdated) {
            ObjJPluginSettings.pluginUpdated = false
            //ObjJNotify.showUpdate(project)
        }
        if (ObjJPluginSettings.stubVersionsUpdated) {
            ObjJPluginSettings.stubVersionsUpdated = false
            ObjJNotify.showUpdateIndexWarning(project)
        }
    }

    companion object {
        private val LOGGER = Logger.getLogger("#" + ObjJProjectComponent::class.java)
    }
}