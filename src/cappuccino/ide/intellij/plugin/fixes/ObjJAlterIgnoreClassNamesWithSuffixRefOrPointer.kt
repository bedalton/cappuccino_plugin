package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.FileContentUtilCore
import com.intellij.util.IncorrectOperationException

/**
 * Sets a project level setting to ignore all classes ending in Ref or Pointer if base class name exists
 */
class ObjJAlterIgnoreClassNamesWithSuffixRefOrPointer(private val shouldIgnore:Boolean) : BaseIntentionAction(), LocalQuickFix {

    override fun getText(): String {
        return if (shouldIgnore)
            ObjJBundle.message("objective-j.inspections.class-type-inspection.fix.ignore-ref-and-pointer-classes.prompt")
         else
            ObjJBundle.message("objective-j.inspections.class-type-inspection.fix.stop-ignoring-ref-and-pointer-classes.prompt")
    }

    override fun isAvailable(project:Project, editor:Editor, file:PsiFile) : Boolean {
        return true
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        invoke()
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        invoke()
    }

    private fun invoke() {
        ObjJPluginSettings.ignoreMissingClassesWhenSuffixedWithRefOrPointer = shouldIgnore
        ApplicationManager.getApplication().invokeLater {
            FileContentUtilCore.reparseFiles()
        }
    }

    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }

}