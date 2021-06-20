package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.ObjJFileNameAsImportString
import cappuccino.ide.intellij.plugin.psi.ObjJFrameworkDescriptor
import cappuccino.ide.intellij.plugin.psi.ObjJVisitor
import cappuccino.ide.intellij.plugin.utils.EMPTY_FRAMEWORK_NAME
import cappuccino.ide.intellij.plugin.utils.ObjJFrameworkUtils
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScopes

class ObjJImportInspection : LocalInspectionTool() {

    override fun buildVisitor(problemsHolder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {

            override fun visitFileNameAsImportString(fileName: ObjJFileNameAsImportString) {
                super.visitFileNameAsImportString(fileName)
                annotateFileNameIfNecessary(problemsHolder, fileName, null)
            }

            override fun visitFrameworkDescriptor(frameworkDescriptor: ObjJFrameworkDescriptor) {
                super.visitFrameworkDescriptor(frameworkDescriptor)
                annotateFrameworkReference(problemsHolder, frameworkDescriptor)

            }
        }
    }

    private fun annotateFrameworkReference(problemsHolder: ProblemsHolder, descriptor: ObjJFrameworkDescriptor) {
        val frameworkName = descriptor.frameworkName ?: return
        if (frameworkName.text != EMPTY_FRAMEWORK_NAME && frameworkName.text !in ObjJFrameworkUtils.frameworkNames(descriptor.project)) {
            problemsHolder.registerProblem(frameworkName, ObjJBundle.message("objective-j.inspections.import-valid.framework.message", frameworkName.text))
            return
        }
        val fileName = descriptor.frameworkFileName ?: return
        annotateFileNameIfNecessary(problemsHolder, fileName, frameworkName.text)
    }

    private fun annotateFileNameIfNecessary(problemsHolder: ProblemsHolder, element: PsiElement, frameworkNameIn: String?) {
        // Ensure import name has length
        val fileNameFromString = (element as? ObjJFileNameAsImportString)?.stringLiteral?.stringValue
        val fileName = fileNameFromString ?: element.text

        // Get containing file and framework name
        val containingFile = element.containingFile as? ObjJFile ?: return
        val frameworkName = frameworkNameIn ?: containingFile.frameworkName

        // Establish if import file name is valid for framework
        val isValid = if (frameworkName == EMPTY_FRAMEWORK_NAME) {
            val parentDirectory = containingFile.containingDirectory ?: return
            val scope = GlobalSearchScopes.directoryScope(parentDirectory, true)
            FilenameIndex.getFilesByName(element.project, fileName, scope).any { file ->
                val otherFile = file as? ObjJFile ?: return@any false
                otherFile.frameworkName == frameworkName
            }
        } else {
            fileName in ObjJFrameworkUtils.getFrameworkFileNames(element.project, frameworkName)
        }
        if (isValid)
            return
        val rangeInElement = if (fileNameFromString != null && element.textLength > 2) {
            TextRange(1, fileName.length + 1)
        } else
            TextRange(0, fileName.length)
        problemsHolder.registerProblem(element, rangeInElement, ObjJBundle.message("objective-j.inspections.import-valid.file.message", fileName))
    }

}