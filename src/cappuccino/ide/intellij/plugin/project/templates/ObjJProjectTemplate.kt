package cappuccino.ide.intellij.plugin.project.templates

import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.project.ObjJModuleBuilder
import com.intellij.ide.util.projectWizard.AbstractModuleBuilder
import com.intellij.ide.util.projectWizard.WebProjectTemplate
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ProjectTemplate
import icons.ObjJIcons
import javax.swing.Icon
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JPanel



class ObjJProjectTemplate : WebProjectTemplate<Object>() {
    override fun generateProject(project: Project, rootDirectory: VirtualFile, settings: Object, module: Module) {

    }

    override fun getName(): String {
        return ObjJ
    }

    override fun getDescription(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
} ProjectTemplate  {

}