package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.impl.isNotCategory
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiFileUtil
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class ObjJVariableAndFunctionAreImported  : LocalInspectionTool() {

    override fun buildVisitor(problemsHolder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitCallTarget(callTarget: ObjJCallTarget) {
                annotateIfNecessary(problemsHolder, callTarget)
            }

            override fun visitClassName(className: ObjJClassName) {
                annotateIfNecessary(problemsHolder, className)
            }
        }
    }

    private fun annotateIfNecessary(problemsHolder: ProblemsHolder, psiElement:PsiElement) {
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
        problemsHolder.registerProblem(psiElement, ObjJBundle.message("objective-j.inspections.not-imported.message", className))

    }

}