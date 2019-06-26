package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportIncludeStatement
import cappuccino.ide.intellij.plugin.utils.EMPTY_FRAMEWORK_NAME
import cappuccino.ide.intellij.plugin.utils.ObjJFrameworkUtils
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference

@Suppress("UNUSED_PARAMETER")
object ObjJImportPsiUtils {

    fun getFileNameString(importFileElement: ObjJImportFile): String {
        return if (importFileElement.stub != null) {
            importFileElement.stub.fileName
        } else importFileElement.fileNameAsImportString.fileNameString
    }

    fun getFileNameString(fileNameAsImportString: ObjJFileNameAsImportString) : String {
        return fileNameAsImportString.stringLiteral.stringValue.afterSlash()
    }

    fun getFileNameString(includeFile: ObjJIncludeFile): String {
        return if (includeFile.stub != null) {
            includeFile.stub.fileName
        } else includeFile.fileNameAsImportString.stringLiteral.stringValue
    }

    fun getFileNameString(importFrameworkElement: ObjJImportFramework): String {
        return if (importFrameworkElement.stub != null) {
            importFrameworkElement.stub.fileName
        } else importFrameworkElement.frameworkDescriptor.fileNameString
    }

    fun getFileNameString(includeFrameworkElement: ObjJIncludeFramework): String {
        return if (includeFrameworkElement.stub != null) {
            includeFrameworkElement.stub.fileName
        } else includeFrameworkElement.frameworkDescriptor.fileNameString
    }

    fun getFileNameString(frameworkDescriptor: ObjJFrameworkDescriptor): String {
        return frameworkDescriptor.frameworkFileName?.text ?: EMPTY_FRAMEWORK_NAME
    }

    fun getFrameworkNameString(include: ObjJIncludeFile): String? {
        return (include.containingFile as? ObjJFile)?.frameworkName ?: ObjJFrameworkUtils.getEnclosingFrameworkName(include.containingFile)
    }

    fun getFrameworkNameString(import: ObjJImportFile): String? {
        return (import.containingFile as? ObjJFile)?.frameworkName ?: ObjJFrameworkUtils.getEnclosingFrameworkName(import.containingFile)
    }

    fun getFrameworkNameString(framework: ObjJImportFramework): String? {
        return framework.stub?.framework ?: framework.frameworkDescriptor.frameworkNameString
    }

    fun getFrameworkNameString(framework: ObjJIncludeFramework): String? {
        return framework.stub?.framework ?: framework.frameworkDescriptor.frameworkNameString
    }

    fun getFrameworkNameString(descriptor: ObjJFrameworkDescriptor): String? {
       return descriptor.frameworkName?.text
    }

    fun getReference(importIncludeStatement:ObjJImportIncludeStatement) : PsiReference? {
        return when(val importIncludeElement = importIncludeStatement.importIncludeElement) {
            is ObjJImportFile -> importIncludeElement.fileNameAsImportString.reference
            is ObjJIncludeFile -> importIncludeElement.fileNameAsImportString.reference
            is ObjJImportFramework -> importIncludeElement.frameworkDescriptor.frameworkFileName?.reference
            is ObjJIncludeFramework -> importIncludeElement.frameworkDescriptor.frameworkFileName?.reference
            else -> null
        }
    }

    fun resolve(importIncludeElement: ObjJImportIncludeStatement) : ObjJFile? {
        val reference = importIncludeElement.reference
        return if (reference is PsiPolyVariantReference)
            reference.multiResolve(true).firstOrNull()?.element as? ObjJFile
        else {
            reference?.resolve() as? ObjJFile
        }
    }

    fun multiResolve(importIncludeElement: ObjJImportIncludeStatement) : List<ObjJFile> {
        val reference = importIncludeElement.reference
        return if (reference is PsiPolyVariantReference)
            reference.multiResolve(true).mapNotNull { it.element as? ObjJFile }
        else {
            listOfNotNull(reference?.resolve() as? ObjJFile)
        }
    }

    fun getImportIncludeElement(element: ObjJImportIncludeStatement) : ObjJImportElement<*>? {
        return when (element) {
            is ObjJImportStatementElement -> element.importFramework ?: element.importFile
            is ObjJIncludeStatementElement -> element.includeFramework ?: element.includeFile
            else -> null
        }
    }
}


private fun String.afterSlash():String {
    val lastPos = this.lastIndexOf("/")
    if (lastPos < 0)
        return this
    return this.substring(lastPos)
}