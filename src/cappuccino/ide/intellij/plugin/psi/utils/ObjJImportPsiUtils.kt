package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImportStub
import java.util.regex.MatchResult
import java.util.regex.Pattern

@Suppress("UNUSED_PARAMETER")
object ObjJImportPsiUtils {
    private val FRAMEWORK_REGEX = Pattern.compile("<(.*)/(.*)>")

    fun getFileName(importStatement: ObjJImportFile): String {
        return if (importStatement.stub != null) {
            importStatement.stub.fileName
        } else importStatement.stringLiteral.stringValue
    }

    fun getFileName(includeFile: ObjJIncludeFile): String {
        return if (includeFile.stub != null) {
            includeFile.stub.fileName
        } else includeFile.stringLiteral.stringValue
    }

    fun getFileName(statement: ObjJImportFramework): String {
        return if (statement.stub != null) {
            statement.stub.fileName
        } else statement.frameworkReference.fileName
    }

    fun getFileName(statement: ObjJIncludeFramework): String {
        return if (statement.stub != null) {
            statement.stub.fileName
        } else statement.frameworkReference.fileName
    }

    fun getFileName(reference: ObjJFrameworkReference): String {
        val matchResult = FRAMEWORK_REGEX.matcher(reference.importFrameworkLiteral.text)
        return if (matchResult.groupCount() < 3) {
            ""
        } else matchResult.group(2)
    }


    fun getFrameworkName(ignored: ObjJIncludeFile): String? {
        return null
    }

    fun getFrameworkName(ignored: ObjJImportFile): String? {
        return null
    }

    fun getFrameworkName(framework: ObjJImportFramework): String? {
        return if (framework.stub != null) {
            framework.stub.framework
        } else getFrameworkName(framework.frameworkReference)
    }

    fun getFrameworkName(framework: ObjJIncludeFramework): String? {
        return if (framework.stub != null) {
            framework.stub.framework
        } else getFrameworkName(framework.frameworkReference)
    }

    fun getFrameworkName(reference: ObjJFrameworkReference): String? {
        val matchResult = FRAMEWORK_REGEX.matcher(reference.importFrameworkLiteral.text)
        return if (matchResult.groupCount() < 3) {
            null
        } else matchResult.group(1)
    }


}
