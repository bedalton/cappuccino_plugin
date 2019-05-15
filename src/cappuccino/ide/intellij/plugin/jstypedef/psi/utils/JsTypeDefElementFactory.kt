package cappuccino.ide.intellij.plugin.jstypedef.psi.utils

import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefFile
import cappuccino.ide.intellij.plugin.jstypedef.lang.JsTypeDefLanguage
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import java.util.logging.Logger

object JsTypeDefElementFactory {

    private val LOGGER:Logger by lazy {
        Logger.getLogger(JsTypeDefElementFactory::class.java.name)
    }

    private const val PlaceholderClassName = "_XXX__"

    fun createFunctionName(project: Project, functionName: String): JsFunctionName? {
        val file = """
            declare function $functionName ();
        """.trimIndent().toFile(project)
        val functionDeclaration = file.getChildOfType(JsFunctionDeclaration::class.java)
        return functionDeclaration?.function?.functionName
    }

    fun createProperty(project: Project, propertyName:String, propertyTypes:String = "null"): JsProperty? {
        val file ="""
                interface $PlaceholderClassName {
                    $propertyName : $propertyTypes
                }
                """.trimIndent().toFile(project)
        return file.interfaces.getOrNull(0)?.interfaceBody?.propertyList?.getOrNull(0)
    }

    fun createTypeName(project:Project, typeName: String) : JsTypeName? {
        val file = """
            interface $typeName { }
        """.trimIndent().toFile(project)
        return file.interfaces.getOrNull(0)?.typeName
    }

    fun createModuleName(project:Project, moduleName:String) : JsModuleName? {
        val file = """
            module $moduleName {}
        """.trimIndent().toFile(project)
        return file.getChildOfType(JsModule::class.java)?.qualifiedModuleName?.moduleNameList?.first()
    }

    fun createSpace(project: Project): PsiElement {
        return " ".toFile(project).firstChild
    }

    fun createNewLine(project: Project): PsiElement {
        return "\n".toFile(project).firstChild
    }
}

private fun createFileFromText(project: Project, text: String): JsTypeDefFile {
    return PsiFileFactory.getInstance(project).createFileFromText("dummy.j", JsTypeDefLanguage.instance, text) as JsTypeDefFile
}

private fun String.toFile(project:Project) :JsTypeDefFile {
    return createFileFromText(project, this)
}