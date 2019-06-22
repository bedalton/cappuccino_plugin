package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.ObjJImportUtils
import cappuccino.ide.intellij.plugin.utils.enclosingFrameworkName
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class ObjJImportInspection  : LocalInspectionTool() {

    override fun buildVisitor(problemsHolder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {

            override fun visitFileNameAsImportString(fileName: ObjJFileNameAsImportString) {
                super.visitFileNameAsImportString(fileName)
                annotateFileNameIfNecessary(problemsHolder, fileName)
            }

            override fun visitFrameworkReference(frameworkReference: ObjJFrameworkReference) {
                super.visitFrameworkReference(frameworkReference)
                annotateFrameworkReference(problemsHolder, frameworkReference);

            }
        }
    }

    private fun annotateFrameworkReference(problemsHolder: ProblemsHolder, reference:ObjJFrameworkReference) {
        val frameworkName = reference.frameworkName ?: return
        if (frameworkName.text !in ObjJImportUtils.frameworkNames(reference.project)) {
            problemsHolder.registerProblem(frameworkName, ObjJBundle.message("objective-j.inspections.import-valid.framework.message", frameworkName.text))
            return
        }
        val fileName = reference.frameworkFileName ?: return
        annotateFileNameIfNecessary(problemsHolder, fileName, frameworkName.text)
    }

    private fun annotateFileNameIfNecessary(problemsHolder: ProblemsHolder, element:PsiElement, frameworkNameIn:String? = null) {
        val frameworkName = frameworkNameIn  ?: element.enclosingFrameworkName
        if (frameworkName == ObjJImportUtils.EMPTY_FRAMEWORK_NAME)
            return
        val fileName = (element as? ObjJFileNameAsImportString)?.stringLiteral?.stringValue ?: element.text
        if (fileName.length < 3)
            return
        val isValid = fileName in ObjJImportUtils.getFrameworkFileNames(element.project, frameworkName)
        if (isValid)
            return
        LOGGER.info("Framework <$frameworkName> does not contain file: <$fileName>")
        val rangeInElement = if (element is  ObjJFileNameAsImportString) {
            TextRange(1, fileName.length)
        } else
            TextRange(0, fileName.length)
        problemsHolder.registerProblem(element, rangeInElement, ObjJBundle.message("objective-j.inspections.import-valid.file.message", fileName))
    }

}