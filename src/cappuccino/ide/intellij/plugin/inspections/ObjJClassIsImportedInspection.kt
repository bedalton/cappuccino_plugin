package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.utils.getImportedFiles
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class ObjJClassIsImportedInspection  : LocalInspectionTool() {

    override fun buildVisitor(problemsHolder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitCallTarget(callTarget: ObjJCallTarget) {
                annotateIfNeccessary(problemsHolder, callTarget)
            }

            override fun visitClassName(className: ObjJClassName) {
                annotateIfNeccessary(problemsHolder, className)
            }
        }
    }

    private fun annotateIfNeccessary(problemsHolder: ProblemsHolder, psiElement:PsiElement) {
        val className = psiElement.text
        if (className !in ObjJClassDeclarationsIndex.instance.getAllKeys(psiElement.project) && className !in ObjJPluginSettings.ignoredClassNames())
            return
        val containingFiles = ObjJClassDeclarationsIndex.instance[className, psiElement.project].mapNotNull {
            it.containingFile as? ObjJFile
        }
        val containingFile = (psiElement.containingFile as? ObjJFile) ?: return
        val imports = containingFile.getImportedFiles(cache = true, recursive = true) + containingFile
        val matches = imports.intersect(containingFiles).isNotEmpty()
        if (matches)
            return
        problemsHolder.registerProblem(psiElement, ObjJBundle.message("objective-j.inspections.class-not-imported.message", className))

    }

}