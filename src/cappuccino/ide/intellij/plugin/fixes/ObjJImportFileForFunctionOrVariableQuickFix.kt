package cappuccino.ide.intellij.plugin.fixes

import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import com.intellij.openapi.project.Project

class ObjJImportFileForFunctionOrVariableQuickFix(thisFramework:String, private val elementKind:String, private val elementName:String, private val includeTests:Boolean) : ObjJImportFileQuickFix(thisFramework) {

    override fun getText(): String {
        return ObjJBundle.message("objective-j.inspections.not-imported.fix.name", elementKind, elementName)
    }

    override fun getName(): String {
        return ObjJBundle.message("objective-j.inspections.not-imported.fix.name", elementKind, elementName)
    }

    override fun getFileChooserTitle()
            = ObjJBundle.message("objective-j.inspections.not-imported.fix.file-chooser-title", elementKind, elementName)

    override fun getFileChooserDescription()
            = ObjJBundle.message("objective-j.inspections.not-imported.fix.file-chooser-description", elementKind, elementName)

    override fun getPossibleFiles(project: Project) : List<FrameworkFileNode> {
        val files:List<ObjJCompositeElement> = ObjJFunctionsIndex.instance[elementName, project] + ObjJGlobalVariableNamesIndex.instance[elementName, project]
        val withoutUnderscore = files.filterNot {
            it.containingFile.name.startsWith("_")
        }
        return (if (withoutUnderscore.isNotEmpty())
            withoutUnderscore
        else
            files).mapNotNull {
            val file = it.containingObjJFile ?: return@mapNotNull null
            val framework = file.frameworkName
            val text = if (it is ObjJFunctionDeclarationElement<*>)
                "${it.description} in ${file.name}"
            else
                "${it.text} in ${file.name}"
            FrameworkFileNodeImpl(framework, file.virtualFile, text, null)
        }
    }
}