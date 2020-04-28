package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.FileContentUtilCore
import com.intellij.util.IncorrectOperationException


/**
 * Creates a suppress inspection comment targeting an undeclared variable by name
 */
class ObjJSuppressUndeclaredVariableInspectionOnVariable(variableName:ObjJVariableName, private val message:String? = null) : BaseIntentionAction(), LocalQuickFix {

    private val variableNamePointer:SmartPsiElementPointer<ObjJVariableName> = SmartPointerManager.createPointer(variableName)
    private val variableName:ObjJVariableName? get() {
        return variableNamePointer.element
    }

    override fun getText(): String {
        return message ?: ObjJBundle.message("objective-j.intentions.suppress-undeclared-variable-inspection.prompt", variableName?.text ?: "#")
    }

    override fun isAvailable(project:Project, editor:Editor, file:PsiFile) : Boolean {
        return variableName != null
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        apply(project, file)
    }

    override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
        apply(project, problemDescriptor.psiElement.containingFile)
    }

    private fun apply(project: Project, file: PsiFile) {
        val variableName = this.variableName ?: return
        val writeAbove = getOutermostParentInEnclosingBlock(variableName)
        val suppressInspectionComment = ObjJElementFactory.createIgnoreComment(project, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_VAR, variableName.text)
        //val newline = writeAbove.parent.addBefore(ObjJElementFactory.createCRLF(project), writeAbove)
        writeAbove.parent.addBefore(suppressInspectionComment, writeAbove)
        //writeAbove.parent.addBefore(suppressInspectionComment, writeAbove)

        val parentBlock = this.variableName?.getParentOfType(ObjJBlock::class.java)
        if (parentBlock != null) {
            CodeStyleManager.getInstance(file.project).reformatText(file, listOf(parentBlock.textRange))
        }
        DaemonCodeAnalyzer.getInstance(project).restart(file)
        FileContentUtilCore.reparseFiles(file.virtualFile)
    }

    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }

}