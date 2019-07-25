package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import com.intellij.openapi.project.Project

class ObjJImportFileForFunctionQuickFix(thisFramework:String, private val functionName:String, private val includeTests:Boolean) : ObjJImportFileQuickFix(thisFramework) {

    override fun getName(): String {
        return ObjJBundle.message("objective-j.inspections.not-imported.fix.name", "function", functionName)
    }

    override fun getFileChooserTitle()
            = ObjJBundle.message("objective-j.inspections.not-imported.fix.file-chooser-title", "function", functionName)

    override fun getFileChooserDescription()
            = ObjJBundle.message("objective-j.inspections.not-imported.fix.file-chooser-description", "function", functionName)

    override fun getPossibleFiles(project: Project) : List<FrameworkFileNode> {
        val files= ObjJFunctionsIndex.instance[functionName, project]
        val withoutUnderscore = files.filterNot {
            it.containingFile.name.startsWith("_")
        }
        return (if (withoutUnderscore.isNotEmpty())
            withoutUnderscore
        else
            files).mapNotNull {
            val file = it.containingObjJFile ?: return@mapNotNull null
            val framework = file.frameworkName
            val text = "${it.description} in ${file.name}"
            FrameworkFileNode(framework, file.virtualFile, text, null)
        }
    }
}