@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package cappuccino.ide.intellij.plugin.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFileFactory
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import com.intellij.openapi.diagnostic.Logger

import java.util.ArrayList

object ObjJElementFactory {
    private val LOGGER = Logger.getInstance(ObjJElementFactory::class.java)
    const val PlaceholderClassName = "_XXX__"

    fun createClassName(project:Project, className:String) : ObjJClassName? {
        val scriptText = "@implementation $className \n @end"
        return createFileFromText(project, scriptText).classDeclarations[0].getClassName()
    }

    fun createSelector(project: Project, selectorIn: String): ObjJSelector?
    {   val selector = selectorIn.replace("[^a-zA-Z_]".toRegex(), "").trim()
        val scriptText = "@implementation $PlaceholderClassName \n - (void) $selector{} @end"
        val implementationDeclaration = createFileFromText(project, scriptText).classDeclarations[0] as ObjJImplementationDeclaration
        return implementationDeclaration.methodDeclarationList[0]?.methodHeader?.selectorList?.get(0)
    }

    fun createVariableName(project: Project, variableName: String): ObjJVariableName {
        val scriptText = "var $variableName;"
        val file = createFileFromText(project, scriptText)
        val variableAssignment = file.getChildOfType( ObjJBodyVariableAssignment::class.java)!!
        return variableAssignment.variableDeclarationList!!.variableNameList[0]
    }

    fun createFunctionName(project: Project, functionName: String): ObjJFunctionName? {
        val scriptText = String.format("function %s(){}", functionName)
        //LOGGER.log(Level.INFO, "Script text: <$scriptText>")
        val file = createFileFromText(project, scriptText)
        val functionDeclaration = file.getChildOfType( ObjJFunctionDeclaration::class.java)
        return functionDeclaration!!.functionName
    }

    fun createSpace(project: Project): PsiElement {
        val scriptText = " "
        val file = createFileFromText(project, scriptText)
        return file.firstChild
    }

    fun createNewLine(project: Project): PsiElement {
        val scriptText = "\n"
        val file = createFileFromText(project, scriptText)
        return file.firstChild
    }

    fun createSemiColonErrorElement(project: Project): PsiErrorElement? {
        val scriptText = "?*__ERR_SEMICOLON__*?"
        val file = createFileFromText(project, scriptText)
        val errorSequence = file.getChildOfType( ObjJErrorSequence::class.java)
        val errorElement = errorSequence!!.getChildOfType( PsiErrorElement::class.java)
        if (errorElement == null) {
            val childElementTypes = ArrayList<String>()
            for (child in file.children) {
                childElementTypes.add(child.node.elementType.toString())
            }
            //LOGGER.log(Level.INFO, "createSemiColonErrorElement(Project project) Failed. No error element found. Found <" + ArrayUtils.join(childElementTypes) + "> instead")
        }
        return errorElement
    }

    fun createIgnoreComment(project: Project, ignoreFlags: ObjJSuppressInspectionFlags, paramIn:String? = null): PsiElement {
        val param = if (paramIn == null || paramIn.trim().isEmpty()) {
            ""
        } else {
            " $paramIn"
        }
        val scriptText = "// @ignore " + ignoreFlags.flag + param
        val file = createFileFromText(project, scriptText)
        return file.firstChild
    }

    private fun createFileFromText(project: Project, text: String): ObjJFile {
        return PsiFileFactory.getInstance(project).createFileFromText("dummy.j", ObjJLanguage.instance, text) as ObjJFile
    }

    fun createMethodDeclaration(project: Project, methodHeader: ObjJMethodHeader): ObjJMethodDeclaration {
        val script ="""
                @implementation $PlaceholderClassName
                ${methodHeader.text}
                {
                    //@todo provide implementation
                    ${getDefaultReturnValueString(methodHeader.methodHeaderReturnTypeElement)};
                }
                @end""".trimIndent()
        val file = createFileFromText(project, script)
        val implementationDeclaration = file.getChildOfType(ObjJImplementationDeclaration::class.java)!!
        return implementationDeclaration.methodDeclarationList[0]!!
    }


    fun createMethodDeclarations(project: Project, methodHeaders: List<ObjJMethodHeader>): List<ObjJMethodDeclaration> {
        val script ="@implementation $PlaceholderClassName\n"+
                createMethodDeclarationsText(methodHeaders) +
                "@end"
        val file = createFileFromText(project, script)
        val implementationDeclaration = file.getChildOfType(ObjJImplementationDeclaration::class.java)!!
        return implementationDeclaration.methodDeclarationList
    }

    fun createMethodDeclarationsText(methodHeaders:List<ObjJMethodHeader>) : String {
        var out = ""
        methodHeaders.forEach {
            out += "\n\n"+
            createMethodDeclarationText(it)
        }
        return out.trim()
    }

    fun createMethodDeclarationText(methodHeader: ObjJMethodHeader) : String {
        var returnStatementString = getDefaultReturnValueString(methodHeader.methodHeaderReturnTypeElement).trim()
        returnStatementString = if (returnStatementString.isNotEmpty()) {
            "   $returnStatementString\n"
        } else {
            ""
        }
        return  "${methodHeader.text}\n" +
                "{\n" +
                "   //@todo provide implementation\n" +
                returnStatementString +
                "}"
    }

    private fun getDefaultReturnValueString(returnTypeElement: ObjJMethodHeaderReturnTypeElement?): String {
        if (returnTypeElement == null || returnTypeElement.formalVariableType.text == "" || returnTypeElement.formalVariableType.text.toLowerCase() == "void") {
            return ""
        }
        var defaultValue = "nil"
        val formalVariableType = returnTypeElement.formalVariableType
        if (formalVariableType.varTypeBool != null) {
            defaultValue = "NO"
        } else if (formalVariableType.varTypeId != null || formalVariableType.className != null) {
            defaultValue = "Nil"
        }
        return "return $defaultValue;"
    }

    fun createReturnStatement(project: Project, returnValue: String): ObjJReturnStatement {
        val scriptString = "function x() {\n" +
                "return " + returnValue + ";" + "\n" +
                "}"
        val file = createFileFromText(project, scriptString)
        val functionDeclarationElement = file.getChildOfType(ObjJFunctionDeclarationElement::class.java)!!
        return functionDeclarationElement.getChildOfType(ObjJReturnStatement::class.java)!!
    }

    fun createCRLF(project: Project): PsiElement {
        val file = createFileFromText(project, "\n")
        return file.firstChild
    }

    fun createFormalVariableType(project:Project, returnType: String) : ObjJFormalVariableType {
        return createMethodReturnTypeElement(project, returnType).formalVariableType
    }

    fun createMethodReturnTypeElement(project: Project, returnType:String) : ObjJMethodHeaderReturnTypeElement {
        val script = """
            @protocol XX
            +($returnType) sel1;
            @end
        """.trimIndent()
        val file = createFileFromText(project, script)
        val returnTypeElement:ObjJMethodHeaderReturnTypeElement? = file.getChildOfType(ObjJProtocolDeclaration::class.java)?.getChildOfType(ObjJMethodHeader::class.java)?.methodHeaderReturnTypeElement
        com.intellij.openapi.diagnostic.Logger.getInstance(ObjJElementFactory::class.java).assertTrue(returnTypeElement != null)
        return returnTypeElement!!
    }

    fun createImportFileElement(project:Project, fileName:String) : PsiElement {
        if (fileName.isEmpty())
            throw Exception("Cannot create import element with empty filename")
        val script = "@import \"$fileName\""
        val file = createFileFromText(project, script)
        return file.firstChild.firstChild ?: throw Exception("Import filename element should not be null")
    }

    fun createImportFrameworkFileElement(project:Project, frameworkName:String, fileName:String) : PsiElement {
        if (frameworkName.isEmpty())
            throw Exception("Cannot create import element with empty framework name")
        if (fileName.isEmpty())
            throw Exception("Cannot create import element with empty file name")
        val script = "@import <$frameworkName/$fileName>".trimIndent()
        val file = createFileFromText(project, script)
        return file.firstChild.firstChild ?: throw Exception("Import framework element should not be null")
    }

}
