package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.fixes.ObjJImportFileForClassQuickFix
import cappuccino.ide.intellij.plugin.fixes.ObjJImportFileForFunctionOrVariableQuickFix
import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.impl.isNotCategory
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiFileUtil
import cappuccino.ide.intellij.plugin.psi.utils.functionDeclarationReference
import cappuccino.ide.intellij.plugin.psi.utils.getImportedFiles
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.enclosingFrameworkName
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class ObjJReferencedElementIsImportedInspection  : LocalInspectionTool() {

    override fun buildVisitor(problemsHolder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitCallTarget(callTarget: ObjJCallTarget) {
                val selector = callTarget.getParentOfType(ObjJMethodCall::class.java)?.selectorString
                annotateIfNecessary(problemsHolder, callTarget, withSelector = selector)
            }

            override fun visitFunctionCall(functionCall: ObjJFunctionCall) {
                annotateIfNecessary(problemsHolder, functionCall)
            }

            override fun visitVariableName(variableName: ObjJVariableName) {
                annotateIfNecessary(problemsHolder, variableName)
            }

            override fun visitClassName(className: ObjJClassName) {
                annotateIfNecessary(problemsHolder, className)
            }
        }
    }

    private fun annotateIfNecessary(problemsHolder: ProblemsHolder, functionCall:ObjJFunctionCall) {
        val containingFile = functionCall.containingFile as? ObjJFile ?: return
        val importedFiles = containingFile.getImportedFiles(true)
        val referenced = functionCall.functionDeclarationReference?.containingFile ?: return
        if (referenced in importedFiles)
            return

        val functionNameElement = functionCall.functionName ?: return

        problemsHolder.registerProblem(functionNameElement, ObjJBundle.message("objective-j.inspections.not-imported.message", "function", functionNameElement.text), ObjJImportFileForFunctionOrVariableQuickFix(functionCall.enclosingFrameworkName, "function", functionNameElement.text, includeTests(functionNameElement)))
    }

    private fun annotateIfNecessary(problemsHolder: ProblemsHolder, variableName:ObjJVariableName) {
        if (variableName.text in listOf( "super", "this", "self"))
            return
        val containingFile = variableName.containingFile as? ObjJFile ?: return
        val importedFiles = containingFile.getImportedFiles(true)
        val referenced = variableName.reference.resolve(true)?.containingFile ?: return
        if (referenced == containingFile)
            return
        if (referenced in importedFiles)
            return
        problemsHolder.registerProblem(variableName, ObjJBundle.message("objective-j.inspections.not-imported.message", "variable", variableName.text), ObjJImportFileForFunctionOrVariableQuickFix(variableName.enclosingFrameworkName, "variable", variableName.text, includeTests(variableName)))
    }

    private fun annotateIfNecessary(problemsHolder: ProblemsHolder, psiElement:PsiElement, withSelector:String? = null) {
        val parent = psiElement.parent
        if (parent is ObjJClassDependencyStatement || parent is ObjJProtocolDeclaration)
            return
        if ((psiElement.parent as? ObjJImplementationDeclaration)?.isNotCategory.orFalse())
            return
        val className = psiElement.text
        if (!ObjJClassDeclarationsIndex.instance.containsKey(className, psiElement.project) || className in ObjJPluginSettings.ignoredClassNames())
            return
        val containingFile = (psiElement.containingFile as? ObjJFile) ?: return
        val importedClassNames = ObjJPsiFileUtil.getImportedClassNames(containingFile) + containingFile.definedClassNames
        if (className in importedClassNames)
            return
        problemsHolder.registerProblem(psiElement, ObjJBundle.message("objective-j.inspections.not-imported.message", "class", className), ObjJImportFileForClassQuickFix(psiElement.enclosingFrameworkName, className, withSelector, includeTests(psiElement)))
    }

    private fun includeTests(@Suppress("UNUSED_PARAMETER") psiElement: PsiElement) : Boolean {
        return false
    }
}