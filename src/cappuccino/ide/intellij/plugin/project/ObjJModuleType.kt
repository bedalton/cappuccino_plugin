package cappuccino.ide.intellij.plugin.project

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.ProjectJdkForModuleStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import icons.ObjJIcons
import javax.swing.Icon

/**
 * Based on ElixerModuleType by zyuyou.
 * Adapted by bedalton on 2019/08/21
 */
class ObjJModuleType : ModuleType<ObjJModuleBuilder>(MODULE_TYPE_ID) {

    override fun createModuleBuilder(): ObjJModuleBuilder {
        return ObjJModuleBuilder()
    }

    override fun getName(): String {
        return "Objective-J Module"
    }

    override fun getDescription(): String {
        return "Objective-J modules are used for developing applications with the <b>Cappuccino Framework</b>."
    }

    //  @Override
    fun getBigIcon(): Icon {
        return ObjJIcons.SDK_ICON
    }

    override fun getNodeIcon(isOpened: Boolean): Icon {
        return ObjJIcons.SDK_ICON
    }

    override fun createWizardSteps(wizardContext: WizardContext,
                          moduleBuilder: ObjJModuleBuilder,
                          modulesProvider: ModulesProvider): Array<ModuleWizardStep> {
        return arrayOf(object : ProjectJdkForModuleStep(wizardContext, ObjJSDKType.instance) {
            override fun updateDataModel() {
                super.updateDataModel()
                moduleBuilder.moduleJdk = jdk
            }
        })
    }

    companion object {
        internal const val MODULE_TYPE_ID = "ObjJ_MODULE"
        val instance:ObjJModuleType by lazy {
            ModuleTypeManager.getInstance().findByID(MODULE_TYPE_ID) as ObjJModuleType
        }
    }


}

