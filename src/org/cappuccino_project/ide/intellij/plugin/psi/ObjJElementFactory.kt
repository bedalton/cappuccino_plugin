package org.cappuccino_project.ide.intellij.plugin.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import org.cappuccino_project.ide.intellij.plugin.annotator.IgnoreUtil
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil

import java.util.ArrayList
import java.util.logging.Level
import java.util.logging.Logger

object ObjJElementFactory {
    private val LOGGER = Logger.getLogger(ObjJElementFactory::class.java.name)
    val PLACEHOLDER_CLASS_NAME = "_XXX__"

    fun createSelector(project: Project, selector: String?): ObjJSelector? {
        if (selector == null || selector.isEmpty()) {
            return null
        }
        val scriptText = "@implementation $PLACEHOLDER_CLASS_NAME \n - (void) $selector{} @end"
        val implementationDeclaration = createFileFromText(project, scriptText).classDeclarations[0] as ObjJImplementationDeclaration
        if (implementationDeclaration.methodDeclarationList.isEmpty()) {
            return null
        } else if (implementationDeclaration.methodDeclarationList[0].methodHeader.selectorList.isEmpty()) {
            return null
        }
        return implementationDeclaration.methodDeclarationList[0].methodHeader.selectorList[0]
    }

    fun createVariableName(project: Project, variableName: String): ObjJVariableName {
        val scriptText = "var $variableName;"
        val file = createFileFromText(project, scriptText)
        val variableAssignment = ObjJTreeUtil.getChildOfType(file, ObjJBodyVariableAssignment::class.java)!!
        return variableAssignment.qualifiedReferenceList[0].variableNameList[0]
    }

    fun createFunctionName(project: Project, functionName: String): ObjJFunctionName? {
        val scriptText = String.format("function %s(){}", functionName)
        LOGGER.log(Level.INFO, "Script text: <$scriptText>")
        val file = createFileFromText(project, scriptText)
        val functionDeclaration = ObjJTreeUtil.getChildOfType(file, ObjJFunctionDeclaration::class.java)
        return functionDeclaration?.functionName
    }

    fun createSpace(project: Project): PsiElement {
        val scriptText = " "
        val file = createFileFromText(project, scriptText)
        return file.firstChild
    }

    fun createSemiColonErrorElement(project: Project): PsiErrorElement? {
        val scriptText = "?*__ERR_SEMICOLON__*?"
        val file = createFileFromText(project, scriptText)
        val errorSequence = ObjJTreeUtil.getChildOfType(file, ObjJErrorSequence::class.java)
        val errorElement = ObjJTreeUtil.getChildOfType(errorSequence, PsiErrorElement::class.java)
        if (errorElement == null) {
            val childElementTypes = ArrayList<String>()
            for (child in file.children) {
                childElementTypes.add(child.node.elementType.toString())
            }
            LOGGER.log(Level.INFO, "createSemiColonErrorElement(Project project) Failed. No error element found. Found <" + ArrayUtils.join(childElementTypes) + "> instead")
        }
        return errorElement
    }

    fun createIgnoreComment(project: Project, elementType: IgnoreUtil.ElementType): ObjJComment? {
        val scriptText = "//ignore " + elementType.type
        val file = createFileFromText(project, scriptText)
        return PsiTreeUtil.getChildOfType(file, ObjJComment::class.java)
    }

    private fun createFileFromText(project: Project, text: String): ObjJFile {
        return PsiFileFactory.getInstance(project).createFileFromText("dummy.j", ObjJLanguage.INSTANCE, text) as ObjJFile
    }

    private fun createMethodDeclaration(project: Project, methodHeader: ObjJMethodHeader): ObjJMethodDeclaration {
        val script = "@implementation " + PLACEHOLDER_CLASS_NAME + " \n " +
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
        val functionDeclarationElement = file.getChildOfType(ObjJFunctionDeclarationElement<*>::class.java)!!
        return functionDeclarationElement.getChildOfType(ObjJReturnStatement::class.java)!!
    }

    fun createCRLF(project: Project): PsiElement {
        val file = createFileFromText(project, "\n")
        return file.firstChild
    }

}
