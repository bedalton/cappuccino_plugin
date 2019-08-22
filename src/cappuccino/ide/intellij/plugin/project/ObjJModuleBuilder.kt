package cappuccino.ide.intellij.plugin.project

import cappuccino.ide.intellij.plugin.project.templates.ObjJProjectTemplate
import com.intellij.compiler.CompilerWorkspaceConfiguration
import com.intellij.ide.util.projectWizard.*
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ModifiableRootModel
import icons.ObjJIcons

import javax.swing.*
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.module.ModifiableModuleModel
import javax.swing.JComponent
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.openapi.options.ConfigurationException
import com.intellij.platform.ProjectGeneratorPeer
import kotlin.reflect.jvm.internal.impl.storage.NotNullLazyValue


/**
 * Based on ElixerModuleBuilder by zyuyou on 2019/08/21.
 * Adapted by bedalton
 */
class ObjJModuleBuilder<T>(private val myTemplate:ObjJProjectTemplate<T>? = null) : ModuleBuilder(), ModuleBuilderListener {

    protected val myGeneratorPeerLazyValue: NotNullLazyValue<ProjectGeneratorPeer<T>>? = myTemplate.createLazyPeer()

    override fun getBuilderId(): String? {
        return super.getBuilderId()
    }

    override fun getModuleType(): ModuleType<*> {
        return ObjJModuleType.instance
    }

    override fun isSuitableSdkType(sdkType: SdkTypeId?): Boolean {
        return sdkType === ObjJSDKType.instance
    }

    override fun getNodeIcon(): Icon {
        return ObjJIcons.SDK_ICON
    }

    override fun moduleCreated(module: Module) {
        CompilerWorkspaceConfiguration.getInstance(module.project).CLEAR_OUTPUT_DIRECTORY = false
    }

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        doAddContentEntry(modifiableRootModel)
    }


    override fun commitModule(project: Project, model: ModifiableModuleModel?): Module? {
        val module = super.commitModule(project, model)
        return module
    }

    protected fun createModuleStructure(project: Project, contentRoot: VirtualFile) {
        try {
        } catch (exception: Exception) {
        }
    }

    override fun modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep? {
        if (myTemplate == null) {
            return super.modifySettingsStep(settingsStep)
        }
        myGeneratorPeerLazyValue.getValue().buildUI(settingsStep)

        return object : ModuleWizardStep() {
            override fun getComponent(): JComponent? {
                return null
            }

            override fun updateDataModel() {}

            override fun validate(): Boolean {
                val info = myGeneratorPeerLazyValue.getValue().validate()
                if (info != null)
                    throw ConfigurationException(info!!.message)
                return true
            }
        }
    }
}