package cappuccino.ide.intellij.plugin.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFileFactory
import cappuccino.ide.intellij.plugin.annotator.IgnoreUtil
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement

import java.util.ArrayList
import java.util.logging.Logger

object ObjJElementFactory {
    private val LOGGER = Logger.getLogger(ObjJElementFactory::class.java.name)
    const val PlaceholderClassName = "_XXX__"

    fun createClassName(project:Project, className:String) : ObjJClassName? {
        val scriptText = "@implementation ${className} \n @end"
        return createFileFromText(project, scriptText).classDeclarations[0].getClassName()
    }

    fun createSelector(project: Project, selector: String): ObjJSelector {
        val scriptText = "@implementation $PlaceholderClassName \n - (void) $selector{} @end"
        val implementationDeclaration = createFileFromText(project, scriptText).classDeclarations[0] as ObjJImplementationDeclaration
        return implementationDeclaration.methodDeclarationList[0].methodHeader.selectorList[0]
    }

    fun createVariableName(project: Project, variableName: String): ObjJVariableName {
        val scriptText = "var $variableName;"
        val file = createFileFromText(project, scriptText)
        val variableAssignment = file.getChildOfType( ObjJBodyVariableAssignment::class.java)!!
        return variableAssignment.variableDeclarationList!!.variableNameList[0]
    }

    fun createFunctionName(project: Project, functionName: String): ObjJFunctionName {
        val scriptText = String.format("function %s(){}", functionName)
        //LOGGER.log(Level.INFO, "Script text: <$scriptText>")
        val file = createFileFromText(project, scriptText)
        val functionDeclaration = file.getChildOfType( ObjJFunctionDeclaration::class.java)
        return functionDeclaration!!.functionName!!
    }

    fun createSpace(project: Project): PsiElement {
        val scriptText = " "
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

    fun createIgnoreComment(project: Project, elementType: IgnoreUtil.ElementType): ObjJComment {
        val scriptText = "//ignore " + elementType.type
        val file = createFileFromText(project, scriptText)
        return file.getChildOfType(ObjJComment::class.java)!!
    }

    private fun createFileFromText(project: Project, text: String): ObjJFile {
        return PsiFileFactory.getInstance(project).createFileFromText("dummy.j", ObjJLanguage.instance, text) as ObjJFile
    }

    private fun createMethodDeclaration(project: Project, methodHeader: ObjJMethodHeader): ObjJMethodDeclaration {
        val script = "@implementation " + PlaceholderClassName + " \n " +
                methodHeader.text + "\n" +
                "{" + "\n" +
                "    " + getDefaultReturnValueString(methodHeader.methodHeaderReturnTypeElement) + "\n" +
                "}" + "\n" +
                "@end"
        val file = createFileFromText(project, script)
        val implementationDeclaration = file.getChildOfType(ObjJImplementationDeclaration::class.java)!!
        return implementationDeclaration.methodDeclarationList[0]!!
    }

    private fun getDefaultReturnValueString(returnTypeElement: ObjJMethodHeaderReturnTypeElement?): String {
        if (returnTypeElement == null || returnTypeElement.formalVariableType == null) {
            return ""
        }
        var defaultValue = "nil"
        val formalVariableType = returnTypeElement.formalVariableType
        if (formalVariableType!!.varTypeBool != null) {
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
            @implementation XX
            +($returnType) sel1 {
                return;
            }
            @end
        """.trimIndent()
        val file = createFileFromText(project, script)
        val returnTypeElement:ObjJMethodHeaderReturnTypeElement? = file.classDeclarations.getOrNull(0)?.getMethodHeaders()?.getOrNull(0)?.methodHeaderReturnTypeElement
        com.intellij.openapi.diagnostic.Logger.getInstance(ObjJElementFactory::class.java).assertTrue(returnTypeElement != null)
        return returnTypeElement!!
    }

}
