package cappuccino.ide.intellij.plugin.actions

import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import com.intellij.ide.util.MemberChooser
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ScrollPaneFactory
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.tree.TreeModel

class ObjJMethodChooserDialog(methods:Array<ObjJMethodHeader>, project:Project) : DialogWrapper(project, true) {


    override fun createCenterPanel(): JComponent? {
        val panel = JPanel(BorderLayout())

        /*val scrollPane = ScrollPaneFactory.createScrollPane(this.myTree)
        scrollPane.preferredSize = JBUI.size(350, 450)
        panel.add(scrollPane, "Center")*/
        return panel
    }
}