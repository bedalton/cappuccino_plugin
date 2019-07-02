package cappuccino.ide.intellij.plugin.project

import cappuccino.ide.intellij.plugin.lang.ObjJFileType
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.ProjectTopics
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectBundle
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications

class ObjJSdkSetupNotification(val project: Project, notifications: EditorNotifications) : EditorNotifications.Provider<EditorNotificationPanel>() {

    init {
        project.messageBus.connect(project).subscribe(ProjectTopics.PROJECT_ROOTS, object:ModuleRootListener {
            override fun rootsChanged(event: ModuleRootEvent) {
                notifications.updateAllNotifications();
            }
        });
    }

    override fun getKey(): Key<EditorNotificationPanel> {
        return KEY
    }

    override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel? {
        if (file.fileType !is ObjJFileType)
            return null;

        val psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null || psiFile.language != ObjJLanguage.instance) return null;
        return createPanel(project, psiFile);
    }

    companion object {
        private fun createPanel(project: Project, file: PsiFile): EditorNotificationPanel {
            val panel: EditorNotificationPanel = EditorNotificationPanel()
            panel.setText(ProjectBundle.message("project.sdk.not.defined"));
            panel.createActionLabel(ProjectBundle.message("project.sdk.setup")) createLabel@{
                val projectSdk: Sdk = ProjectSettingsService.getInstance(project).chooseAndSetSdk()
                        ?: return@createLabel;
                ApplicationManager.getApplication().runWriteAction {
                    val module: Module? = ModuleUtilCore.findModuleForPsiElement(file);
                    if (module != null) {
                        ModuleRootModificationUtil.setSdkInherited(module);
                    }
                }
            }
            return panel;
        }

        private val KEY: Key<EditorNotificationPanel> = Key.create("Setup ObjJ SDK");
    }
}