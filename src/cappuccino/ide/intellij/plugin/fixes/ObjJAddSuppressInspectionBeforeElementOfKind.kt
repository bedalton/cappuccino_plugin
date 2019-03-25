package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.inspections.ObjJInspectionProvider
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJElementFactory
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.util.IncorrectOperationException


/**
 * Abstract class to add a suppress inspection statement to a block scope
 */
abstract class ObjJAddSuppressInspectionBeforeElementOfKind (psiElement: PsiElement, protected val flag: ObjJSuppressInspectionFlags, private val parameter:String? = null) : BaseIntentionAction(), LocalQuickFix {

    protected val pointer:SmartPsiElementPointer<*> = SmartPointerManager.createPointer(psiElement)
    // val writeAbove, Should be overwritten by accessor method,
    // not by actually holding a reference to an element
    protected abstract val writeAbove:PsiElement?

    override fun isAvailable(project:Project, editor:Editor, file:PsiFile) : Boolean {
        return writeAbove != null
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        apply(project, file)
    }

    override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
        apply(project, problemDescriptor.psiElement.containingFile)
    }

    private fun apply(project: Project, file: PsiFile) {
        val writeAbove = this.writeAbove ?: return
        val suppressInspectionComment = ObjJElementFactory.createIgnoreComment(project, flag, parameter)
        writeAbove.parent.addBefore(suppressInspectionComment, writeAbove)
        DaemonCodeAnalyzer.getInstance(project).restart(file)
    }

    override fun getFamilyName(): String {
        return ObjJInspectionProvider.GROUP_DISPLAY_NAME
    }

}

/**
 * Concrete implementation to add a inspection suppression for a ignore flag within a given scope
 */
class ObjJAddSuppressInspectionForScope(psiElement: PsiElement, flag: ObjJSuppressInspectionFlags, private val scope:ObjJSuppressInspectionScope, private val parameter:String? = null) : ObjJAddSuppressInspectionBeforeElementOfKind(psiElement, flag, parameter) {
    // Holds pointer to the kind of element that should be annotated
    private var _writeAbove:SmartPsiElementPointer<PsiElement>? = null

    // Gets the element to write above.
    override val writeAbove:PsiElement? get () {
        var writeAbove = this._writeAbove?.element
        if (writeAbove != null) {
            return writeAbove
        }
        val element = pointer.element ?: return null
        writeAbove = when (scope) {
            ObjJSuppressInspectionScope.STATEMENT -> getOutermostParentInEnclosingBlock(element)
            ObjJSuppressInspectionScope.CLASS -> element.getParentOfType(ObjJClassDeclarationElement::class.java)
            ObjJSuppressInspectionScope.METHOD -> element.getParentOfType(ObjJMethodDeclaration::class.java)
            ObjJSuppressInspectionScope.FUNCTION -> element.getParentOfType(ObjJFunctionDeclarationElement::class.java)
            ObjJSuppressInspectionScope.FILE -> element.containingFile.firstChild
        } ?: return null
        _writeAbove = SmartPointerManager.createPointer(writeAbove)
        return writeAbove
    }

    override fun getText(): String {
        val forParameter = if (parameter != null && parameter.trim().isNotEmpty()) {
            ObjJBundle.message("objective-j.intentions.suppress-inspection.for-parameter", parameter.trim())
        } else ""
        return ObjJBundle.message("objective-j.intentions.suppress-inspection.text", flag.title, scope.scope, forParameter)
    }
}


enum class ObjJSuppressInspectionScope(val scope:String) {
    STATEMENT("for statement"),
    METHOD("for method"),
    FUNCTION("for function"),
    CLASS("for class"),
    FILE("in file")
}

fun getOutermostParentInEnclosingBlock(childElement:PsiElement) : PsiElement {
    var writeAbove:PsiElement? = null
    var currentParent:PsiElement? = childElement.parent
    while (currentParent != null) {
        if (currentParent.parent is ObjJBlock) {
            writeAbove = currentParent
            break
        }
        if (currentParent.parent is ObjJClassDeclarationElement<*>) {
            writeAbove = currentParent
            break
        }
        if (currentParent.parent is PsiFile) {
            writeAbove = currentParent
            break
        }
        currentParent = currentParent.parent
    }
    return writeAbove!!
}