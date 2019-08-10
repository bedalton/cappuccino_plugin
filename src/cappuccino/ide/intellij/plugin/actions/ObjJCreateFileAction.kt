package cappuccino.ide.intellij.plugin.actions

import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import icons.ObjJIcons
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.actions.AttributesDefaults
import com.intellij.ide.fileTemplates.ui.CreateFromTemplateDialog
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.PsiDirectory
import java.util.*

/**
 * Creates a file
 * @todo implement multiple file types (ie. implementations or protocols)
 */
class ObjJCreateFileAction: CreateFileFromTemplateAction(
        ObjJBundle.message("objective-j.actions.new-file.title"),
        ObjJBundle.message("objective-j.actions.new-file.description"),
        ObjJIcons.DOCUMENT_ICON), DumbAware {

    /**
     * Gets the menu name
     */
    override fun getActionName(p0: PsiDirectory?, p1: String, p2: String?): String =
            ObjJBundle.message("objective-j.actions.new-file.title")

    /**
     * Builds the dialog object
     */
    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle(ObjJBundle.message("objective-j.actions.new-file.title"))
                .setValidator(object : InputValidatorEx {
                    override fun canClose(inputString: String?) = checkInput(inputString)
                    override fun getErrorText(inputString: String?) = ObjJBundle.message("objective-j.actions.new-file.invalid", inputString.orEmpty())
                    override fun checkInput(inputString: String?) = inputString?.run {
                        this.indexOf(".") < 0 || this.endsWith(".j")
                    }?.or(false) ?: false
                })
                .addKind("File", ObjJIcons.DOCUMENT_ICON, "file")
    }

    /**
     * Creates the file given a filename and template name
     * @todo implement more than one file type
     */
    override fun createFileFromTemplate(fileName: String, template: FileTemplate, dir: PsiDirectory) = try {
        val className = FileUtilRt.getNameWithoutExtension(fileName)
        val type = when(template.name) {
            "implementation" -> "Class"
            "protocol" -> "Protocol"
            "Category" -> "Category"
            else -> "File"
        }
        val project = dir.project
        val properties = createProperties(project, fileName, className, type)
        CreateFromTemplateDialog(project, dir, template, AttributesDefaults(className).withFixedName(true), properties)
                .create()
                .containingFile
    } catch (e: Exception) {
        LOG.error("Error while creating new objective-j file", e)
        null
    }

    /**
     * Creates a properties object containing properties passed to the template.
     */
    companion object {
        fun createProperties(project: Project, fileName:String, className: String, type:String): Properties {
            val properties = FileTemplateManager.getInstance(project).defaultProperties
            properties += "NAME" to className
            properties += "FILE_NAME" to fileName
            properties += "TYPE" to type
            return properties
        }
    }
}