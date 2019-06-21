package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*

@Suppress("UNUSED_PARAMETER")
object ObjJImportPsiUtils {

    fun getFileNameString(importStatement: ObjJImportFile): String {
        return if (importStatement.stub != null) {
            importStatement.stub.fileName
        } else importStatement.fileNameAsImportString.stringLiteral.stringValue
    }

    fun getFileNameString(includeFile: ObjJIncludeFile): String {
        return if (includeFile.stub != null) {
            includeFile.stub.fileName
        } else includeFile.fileNameAsImportString.stringLiteral.stringValue
    }

    fun getFileNameString(statement: ObjJImportFramework): String {
        return if (statement.stub != null) {
            statement.stub.fileName
        } else statement.frameworkReference.fileNameString
    }

    fun getFileNameString(statement: ObjJIncludeFramework): String {
        return if (statement.stub != null) {
            statement.stub.fileName
        } else statement.frameworkReference.fileNameString
    }

    fun getFileNameString(reference: ObjJFrameworkReference): String {
        return reference.frameworkFileName?.text ?: ""
    }


    fun getFrameworkNameString(ignored: ObjJIncludeFile): String? {
        return null
    }

    fun getFrameworkNameString(ignored: ObjJImportFile): String? {
        return null
    }

    fun getFrameworkNameString(framework: ObjJImportFramework): String? {
        return framework.stub?.framework ?: framework.frameworkReference.frameworkNameString
    }

    fun getFrameworkNameString(framework: ObjJIncludeFramework): String? {
        return framework.stub?.framework ?: framework.frameworkReference.frameworkNameString
    }

    fun getFrameworkNameString(reference: ObjJFrameworkReference): String? {
       return reference.frameworkName?.text
    }


}
