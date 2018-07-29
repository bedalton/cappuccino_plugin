package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNamedElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.utils.Filter
import cappuccino.ide.intellij.plugin.utils.inSameFile
import com.intellij.openapi.project.DumbServiceImpl
import com.intellij.psi.util.PsiTreeUtil

import java.util.ArrayList
import java.util.logging.Level

object ObjJFunctionDeclarationPsiUtil {

    val LOGGER:java.util.logging.Logger = java.util.logging.Logger.getLogger(ObjJFunctionDeclarationPsiUtil::class.java.canonicalName)

    fun getName(functionDeclaration: ObjJFunctionDeclaration): String {
        return if (functionDeclaration.functionName != null) functionDeclaration.functionName!!.text else ""
    }

    /**
     * Renames function
     *
     * @param functionDeclaration function to rename
     * @param name                new function name
     * @return function name element
     * @throws IncorrectOperationException exception
     */
    @Throws(IncorrectOperationException::class)
    fun setName(
            functionDeclaration: ObjJFunctionDeclaration,
            name: String): ObjJFunctionName {
        val oldFunctionName = functionDeclaration.functionName
        val newFunctionName = ObjJElementFactory.createFunctionName(functionDeclaration.project, name)
        if (oldFunctionName == null) {
            if (functionDeclaration.openParen != null) {
                functionDeclaration.addBefore(functionDeclaration.openParen!!, newFunctionName)
            } else {
                functionDeclaration.addBefore(functionDeclaration.firstChild, newFunctionName)
            }
        } else {
            functionDeclaration.node.replaceChild(oldFunctionName.node, newFunctionName.node)
        }
        return newFunctionName
    }

    /**
     * Renames function literal node.
     *
     * @param functionLiteral the literal to rename
     * @param name            the new name
     * @return this function literal
     * @throws IncorrectOperationException exception
     */
    @Throws(IncorrectOperationException::class)
    fun setName(
            functionLiteral: ObjJFunctionLiteral,
            name: String): ObjJFunctionLiteral {
        //Get existing name node.
        val oldFunctionName = functionLiteral.functionNameNode
        //Create new name node
        val newFunctionName = ObjJElementFactory.createVariableName(functionLiteral.project, name)

        //Name node is not part of function literal, so name node may not be present.
        //If name node is not present, must exit early.
        Logger.getInstance(ObjJPsiImplUtil::class.java).assertTrue(oldFunctionName != null)
        //Replace node
        oldFunctionName!!.parent.node.replaceChild(oldFunctionName.node, newFunctionName.node)
        return functionLiteral
    }


    fun setName(defineFunction: ObjJPreprocessorDefineFunction, name: String): PsiElement {
        when {
            defineFunction.functionName != null -> {
                val functionName = ObjJElementFactory.createFunctionName(defineFunction.project, name)
                defineFunction.node.replaceChild(defineFunction.functionName!!.node, functionName.node)
            }
            defineFunction.openParen != null -> {
                val functionName = ObjJElementFactory.createFunctionName(defineFunction.project, name)
                defineFunction.addBefore(defineFunction.openParen!!, functionName)
            }
            defineFunction.variableName != null -> {
                val newVariableName = ObjJElementFactory.createVariableName(defineFunction.project, name)
                defineFunction.node.replaceChild(defineFunction.variableName!!.node, newVariableName.node)
            }
        }
        return defineFunction
    }

    fun getQualifiedNameText(functionCall: ObjJFunctionCall): String? {
        return functionCall.functionName?.text
    }

    fun getFunctionNameAsString(functionLiteral: ObjJFunctionLiteral): String {
        if (functionLiteral.stub != null) {
            return functionLiteral.stub.fqName
        }
        val globalVariableDeclaration = functionLiteral.getParentOfType(ObjJGlobalVariableDeclaration::class.java)
        if (globalVariableDeclaration != null) {
            return globalVariableDeclaration.variableNameString
        }
        val variableDeclaration = functionLiteral.getParentOfType( ObjJVariableDeclaration::class.java)
        return if (variableDeclaration == null || variableDeclaration.qualifiedReferenceList.isEmpty()) {
            ""
        } else ObjJPsiImplUtil.getPartsAsString(variableDeclaration.qualifiedReferenceList[0])
    }

    fun getFunctionNameAsString(functionDeclaration: ObjJPreprocessorDefineFunction): String {
        if (functionDeclaration.stub != null) {
            return functionDeclaration.stub.functionName
        }
        return if (functionDeclaration.functionName != null) functionDeclaration.functionName!!.text else if (functionDeclaration.variableName != null) functionDeclaration.variableName!!.text else "{UNDEF}"
    }

    fun getFunctionNamesAsString(functionLiteral: ObjJFunctionLiteral): List<String> {
        val out = ArrayList<String>()
        val variableDeclaration = functionLiteral.getParentOfType( ObjJVariableDeclaration::class.java)
        if (variableDeclaration == null || variableDeclaration.qualifiedReferenceList.isEmpty()) {
            return emptyList()
        }
        for (reference in variableDeclaration.qualifiedReferenceList) {
            val name = ObjJPsiImplUtil.getPartsAsString(reference)
            if (!name.isEmpty()) {
                out.add(name)
            }
        }
        return out
    }

    fun getFunctionNameAsString(functionDeclaration: ObjJFunctionDeclaration): String {
        if (functionDeclaration.stub != null) {
            return functionDeclaration.stub.functionName
        }
        return if (functionDeclaration.functionName != null) functionDeclaration.functionName!!.text else ""
    }

    fun getParamNameElements(
            functionDeclaration: ObjJFunctionDeclarationElement<*>): List<ObjJVariableName> {
        val out = ArrayList<ObjJVariableName>()
        for (parameterArg in functionDeclaration.formalParameterArgList) {
            out.add(parameterArg.variableName)
        }
        if (functionDeclaration.lastFormalParameterArg != null) {
            out.add(functionDeclaration.lastFormalParameterArg!!.variableName)
        }
        return out

    }

    fun getParamNames(
            functionDeclaration: ObjJFunctionDeclarationElement<*>): List<String> {
        if (functionDeclaration.stub != null) {

            return (functionDeclaration.stub as ObjJFunctionDeclarationElementStub<*>).paramNames
        }
        val out = ArrayList<String>()
        for (parameterArg in functionDeclaration.formalParameterArgList) {
            out.add(parameterArg.variableName.text)
        }
        if (functionDeclaration.lastFormalParameterArg != null) {
            out.add(functionDeclaration.lastFormalParameterArg!!.variableName.text)
        }
        return out
    }

    fun getReturnType(
            functionDeclaration: ObjJFunctionDeclaration): String {
        if (functionDeclaration.stub != null) {
            functionDeclaration.stub.returnType
        }
        return ObjJClassType.UNDETERMINED
    }

    fun getReturnType(
            functionLiteral: ObjJFunctionLiteral): String {
        if (functionLiteral.stub != null) {
            functionLiteral.stub.returnType
        }
        return ObjJClassType.UNDETERMINED
    }

    fun getReturnType(functionDefinition: ObjJPreprocessorDefineFunction): String? {
        return if (functionDefinition.stub != null) {
            functionDefinition.stub.returnType
        } else ObjJClassType.UNDETERMINED
        /*
        if (functionDefinition.getPreprocessorDefineBody() != null) {
            ObjJPreprocessorDefineBody defineBody = functionDefinition.getPreprocessorDefineBody();
            List<ObjJReturnStatement> returnStatements = new ArrayList<>();
            if (defineBody.getBlock() != null) {
                ObjJBlock block = defineBody.getBlock();
                returnStatements.addAll(ObjJBlockPsiUtil.getBlockChildrenOfType(block, ObjJReturnStatement.class, true));
            } else if (defineBody.getPreprocessorBodyStatementList() != null) {
                ObjJPreprocessorBodyStatementList statementList = defineBody.getPreprocessorBodyStatementList();
                if (statementList != null) {
                    returnStatements.addAll(statementList.getReturnStatementList());
                    for (ObjJBlock block : statementList.getBlockList()) {
                        java.util.logging.LOGGER.log(Level.INFO, "Looping preprocessor block in block");
                        returnStatements.addAll(ObjJBlockPsiUtil.getBlockChildrenOfType(block, ObjJReturnStatement.class, true));
                    }
                }
            }
            for (ObjJReturnStatement returnStatement : returnStatements) {
                java.util.logging.LOGGER.log(Level.INFO, "Looping return statement: <"+returnStatement.getText()+">");
                if (returnStatement.getExpr() != null) {
                    //todo Figure foldingDescriptors how to get the expression return types, when index is not ready.
                    List<String> types = Collections.emptyList();// ObjJVarTypeResolveUtil.getExpressionReturnTypes(returnStatement.getExpr(), true);
                    return !types.isEmpty() ? types.get(0) : ObjJClassType.UNDETERMINED;
                }
            }
        }
        */
    }

    fun getFunctionNameNode(
            functionLiteral: ObjJFunctionLiteral): ObjJNamedElement? {
        val variableDeclaration = functionLiteral.getParentOfType( ObjJVariableDeclaration::class.java)
                ?: return null
        return if (!variableDeclaration.qualifiedReferenceList.isEmpty()) variableDeclaration.qualifiedReferenceList[0].lastVar else null
    }


    fun resolveElementToFunctionDeclarationReference(elementToResolve:PsiElement) : PsiElement? {
        val text = elementToResolve.text

        val preprocessorFunctionNameElement = getPreprocessorFunctionDeclarationNameElement(elementToResolve)
        if (preprocessorFunctionNameElement != null) {
            return preprocessorFunctionNameElement
        }
        val project = elementToResolve.project
        if (DumbServiceImpl.isDumb(project)) {
            return null
        }
        val functionDeclarationElements: MutableList<ObjJFunctionDeclarationElement<*>> = ObjJFunctionsIndex.instance[text, project]
        if (functionDeclarationElements.isEmpty()) {
            return null
        }
        //Loop through all function declaration element in index
        for (declarationElement in functionDeclarationElements) {
            return getFunctionNameNodeIfMatching(declarationElement,elementToResolve) ?: continue
        }

        val globalFunctionDeclarationNameElement = getGlobalFunctionDeclarationNameElement(elementToResolve)
        if (globalFunctionDeclarationNameElement != null) {
            return globalFunctionDeclarationNameElement
        }
        return null
    }

    private fun getPreprocessorFunctionDeclarationNameElement(elementToResolve: PsiElement) : PsiElement? {
        val text:String = elementToResolve.text
        for (function in PsiTreeUtil.getChildrenOfTypeAsList(elementToResolve.containingFile, ObjJPreprocessorDefineFunction::class.java)) {
            val functionName = function.functionName?.text ?: continue
            if (functionName == text) {
                return function.functionName
            }
        }
        return null
    }

    private fun getFunctionNameNodeIfMatching(declarationElement:ObjJFunctionDeclarationElement<*>, elementToResolve: PsiElement):PsiElement? {
        val functionNameElement:ObjJNamedElement? = declarationElement.functionNameNode ?: return null
        if (functionNameElement?.isEquivalentTo(elementToResolve) != false) {
            return functionNameElement
        }
        //Source this variable name to first declaration.
        //Root needed to determine if function is local or file scoped
        val root: PsiElement = when (functionNameElement) {
            is ObjJVariableName -> functionNameElement.reference.resolve()
            is ObjJFunctionName -> functionNameElement.reference.resolve()
            else -> functionNameElement
        } ?: return null

        //Check if function name is file scoped
        val isBodyVariableAssignment:Boolean = root.getParentOfType(ObjJBodyVariableAssignment::class.java)?.varModifier != null ?: false
        //Check if function is in same file
        val inSameFile:Boolean = root inSameFile elementToResolve
        //Check that if function is a body variable assignment
        //That it is in the same file
        val logMessage:String = when {
            isBodyVariableAssignment && !inSameFile -> "Resolved function name: ${root.text} to file scoped function in file: ${root.containingFile.name}"
            isBodyVariableAssignment -> "Resolved function name: ${root.text} to a body variable assignment in same file: ${root.containingFile.name}"
            else -> "Resolved function name: ${root.text} to global function in file: ${root.containingFile.name}"
        }
        LOGGER.log(Level.INFO,logMessage)
        //Ensure that root element is not this element
        val isRootElement = functionNameElement?.isEquivalentTo(elementToResolve) ?: false
        if (isRootElement) {
            // Returns function name instead of root,
            // as the function name shows where
            // the function is actually declared
            return functionNameElement
        }
        return null
    }

    private fun getGlobalFunctionDeclarationNameElement(elementToResolve: PsiElement) : ObjJVariableName? {
        val filter:Filter<ObjJGlobalVariableDeclaration> = {
            it.expr.leftExpr?.functionLiteral != null
        }
        return when (elementToResolve) {
            is ObjJVariableName -> ObjJVariableNameResolveUtil.getGlobalElement(elementToResolve, filter)
            is ObjJFunctionName -> ObjJVariableNameResolveUtil.getGlobalElement(elementToResolve, filter)
            else -> return null
        }
    }

}
