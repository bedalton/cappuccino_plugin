package cappuccino.ide.intellij.plugin.project

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.roots.ModifiableRootModel
import javax.swing.JComponent
import javax.swing.JLabel

class ObjJModuleBuilder  : ModuleBuilder() {
    @Throws(ConfigurationException::class)
    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        val root = doAddContentEntry(modifiableRootModel)?.file ?: return
        modifiableRootModel.inheritSdk()
        root.refresh(false, true)
    }

    override fun getModuleType(): ModuleType<*> {
        return ObjJModuleType.instance
    }

    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep? {
        return ObjJModuleWizardStep()
    }
}

class ObjJModuleWizardStep : ModuleWizardStep() {
    override fun getComponent(): JComponent {
        return JLabel("Provide some setting here")
    }

    override fun updateDataModel() {
        //todo update model according to UI
    }
}