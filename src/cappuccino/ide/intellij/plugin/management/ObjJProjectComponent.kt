package cappuccino.ide.intellij.plugin.management

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFileType
import cappuccino.ide.intellij.plugin.lang.ObjJFileType
import cappuccino.ide.intellij.plugin.utils.getModule
import cappuccino.ide.intellij.plugin.utils.virtualFile
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import java.util.logging.Logger


class ObjJProjectComponent(project:Project) : ProjectComponent {

    init {
        registerFileOpenHandler(project)
    }

    private fun registerFileOpenHandler(project: Project) {
        val bus = project.getMessageBus()
        bus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun fileOpened(editorManager: FileEditorManager, file: VirtualFile) {
                val extension = file.extension
                if (extension != ObjJFileType.FILE_EXTENSION && extension != JsTypeDefFileType.FILE_EXTENSION)
                    return
                val module =  file.getModule(project)
                if (module != null) {
                    JsTypeDefBundledSourcesRegistrationUtil.register(module)
                    initFrameworkDefaults(project, module, editorManager.selectedTextEditor, file)
                }
            }
        })
    }

    private fun initFrameworkDefaults(project:Project, module:Module, editor:Editor?, file:VirtualFile) {
        val editorVirtualFile = editor?.virtualFile ?: return
        if (editorVirtualFile.path != file.path)
            return
        LOGGER.info("File opened is current file in current editor")

    }

    override fun projectOpened() {
        super.projectOpened()

    }

    companion object {
        private val LOGGER = Logger.getLogger("#"+ObjJProjectComponent::class.java)
    }
}