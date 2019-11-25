package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.indices.ObjJInstanceVariablesByClassIndex
import cappuccino.ide.intellij.plugin.indices.ObjJVariableNameByScopeIndex
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import cappuccino.ide.intellij.plugin.utils.Filter
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import cappuccino.ide.intellij.plugin.utils.getFirstMatchOrNull
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import java.util.*
import java.util.logging.Logger

@Suppress("UNUSED_PARAMETER")
object ObjJVariableNameAggregatorUtil {

    private val LOGGER = Logger.getLogger("ObjJVariableNameUtil")
    private val EMPTY_VARIABLE_NAME_LIST = emptyList<ObjJVariableName>()

    /**
     * Gets ALL variable names EVEN if NOT ASSIGNMENTS and filters them
     */
    internal fun getAndFilterSiblingVariableNameElements(element: PsiElement, qualifiedNameIndex: Int, filter: Filter<ObjJVariableName>): List<ObjJVariableName> {
        val rawVariableNameElements = getSiblingVariableNameElements(element, qualifiedNameIndex)
        return ArrayUtils.filter(rawVariableNameElements, filter)
    }


    /**
     * Gets every preceding variable name element, even if it is NOT an assignment
     * @param element element to find siblings for
     * @param qualifiedNameIndex variable name index in chain, should be 0 for right now
     * @return list of variable name elements
     *
     * todo Allow checking of non 0 qualified name index
     */
    private fun getSiblingVariableNameElements(element: PsiElement, qualifiedNameIndex: Int): List<ObjJVariableName> {
        val result = ArrayList(getAllVariableNamesInContainingBlocks(element, qualifiedNameIndex))
        if (qualifiedNameIndex == 0) {
            result.addAll(getAllContainingClassInstanceVariables(element))
        }

        result.addAll(getAllAtGlobalFileVariables(element.containingFile))
        result.addAll(getAllGlobalScopedFileVariables(element.containingFile))
        result.addAll(getAllMethodDeclarationSelectorVars(element))
        result.addAll(getAllIterationVariables(element.getParentOfType( ObjJIterationStatement::class.java)))
        result.addAll(getAllFileScopedVariables(element.containingFile, qualifiedNameIndex))
        result.addAll(getAllFunctionScopeVariables(element.getParentOfType(ObjJFunctionDeclarationElement::class.java)))
        result.addAll(getCatchProductionVariables(element.getParentOfType( ObjJCatchProduction::class.java)))
        result.addAll(getPreprocessorDefineFunctionVariables(element.getParentOfType( ObjJPreprocessorDefineFunction::class.java)))
        return result
    }

    /**
     * Gets ALL assignment variable names and then filters them according to the filter
     */
    private fun getAndFilterSiblingVariableAssignmentNameElements(element: PsiElement, qualifiedNameIndex: Int, filter: Filter<ObjJVariableName>): List<ObjJVariableName> {
        val rawVariableNameElements = getSiblingVariableAssignmentNameElements(element, qualifiedNameIndex)
        return ArrayUtils.filter(rawVariableNameElements, filter)
    }


    /**
     * Gets all variable names inside a containing block, regardless of whether or not they are an assignment
     */
    private fun getAllVariableNamesInContainingBlocks(element: PsiElement, qualifiedNameIndex: Int): List<ObjJVariableName> {

        var containingBlock = element.getParentOfType( ObjJBlock::class.java)
        var tempBlock = containingBlock
        while (tempBlock != null) {
            tempBlock = containingBlock!!.getParentOfType(ObjJBlock::class.java)
            if (tempBlock == null) {
                break
            }
            containingBlock = tempBlock
        }
        if (containingBlock == null) {
            return EMPTY_VARIABLE_NAME_LIST
        }
        val containingFile = element.containingFile
        return ObjJVariableNameByScopeIndex.instance.getInRangeStrict(containingFile, containingBlock.textRange, element.project)
    }


    /**
     * Gets variable names used in variable assignments
     */
    fun getPrecedingVariableAssignmentNameElements(variableName: PsiElement, qualifiedIndex: Int): List<ObjJVariableName> {
        val startOffset = variableName.textRange.startOffset
        val file = variableName.containingFile
        return getAndFilterSiblingVariableAssignmentNameElements(variableName, qualifiedIndex) {
            itVar -> itVar !== variableName && (itVar.containingFile.isEquivalentTo(file) || itVar.textRange.startOffset < startOffset) }
    }


    /**
     * Gets preceding variable name element IF is assignment
     * @param element element to find siblings for
     * @param qualifiedNameIndex variable name index in chain, should be 0 for right now
     * @return list of variable name elements
     *
     * todo Allow checking of non 0 qualified name index
     */
    fun getSiblingVariableAssignmentNameElements(element: PsiElement, qualifiedNameIndex: Int): List<ObjJVariableName> {
        val result = getAllVariableNamesInAssignmentsInContainingBlocks(element, qualifiedNameIndex)
        if (qualifiedNameIndex <= 1) {
            result.addAll(getAllContainingClassInstanceVariables(element))
        }

        result.addAll(getAllAtGlobalFileVariables(element.containingFile))
        result.addAll(getAllGlobalScopedFileVariables(element.containingFile))
        result.addAll(getAllMethodDeclarationSelectorVars(element))
        result.addAll(getAllIterationVariables(element.getParentOfType( ObjJIterationStatement::class.java)))
        result.addAll(getAllFileScopedVariables(element.containingFile, qualifiedNameIndex))
        result.addAll(getAllFunctionScopeVariables(element.getParentOfType(ObjJFunctionDeclarationElement::class.java)))
        result.addAll(getCatchProductionVariables(element.getParentOfType( ObjJCatchProduction::class.java)))
        result.addAll(getPreprocessorDefineFunctionVariables(element.getParentOfType( ObjJPreprocessorDefineFunction::class.java)))
        return result
    }


    /**
     * Gets the sibling variable assignment elements with a filter
     */
    fun getSiblingVariableAssignmentNameElement(element: PsiElement, qualifiedNameIndex: Int, filter: Filter<ObjJVariableName>): ObjJVariableName? {

        // Get variable name in declaration
        var variableName: ObjJVariableName? = getVariableNameDeclarationInContainingBlocks(element, qualifiedNameIndex, filter)

        // Ensure that variable name is not same as reference
        if (variableName != null && variableName inSameFile element && !variableName.isEquivalentTo(element)) {
            return variableName
        }

        // If qualified index is less than one, do additional checks
        if (qualifiedNameIndex <= 1) {
            variableName = getFirstMatchOrNull(getAllMethodDeclarationSelectorVars(element), filter)
                        ?: getFirstMatchOrNull(getAllContainingClassInstanceVariables(element), filter)
                        ?: getFirstMatchOrNull(getAllIterationVariables(element.getParentOfType(ObjJIterationStatement::class.java)), filter)
                        ?: getFirstMatchOrNull(getAllFunctionScopeVariables(element.getParentOfType(ObjJFunctionDeclarationElement::class.java)), filter)
                        ?: getFirstMatchOrNull(getAllGlobalScopedFileVariables(element.containingFile), filter)
                        ?: getFirstMatchOrNull(getAllAtGlobalFileVariables(element.containingFile), filter)
                        ?: getFirstMatchOrNull(getAllContainingClassInstanceVariables(element), filter)
            // If variable has been found, return it
            if (variableName != null) {
                return variableName
            }
        }

        variableName = getFirstMatchOrNull(getAllFileScopedVariables(element.containingFile, qualifiedNameIndex), filter)
                    ?: getFirstMatchOrNull(getCatchProductionVariables(element.getParentOfType( ObjJCatchProduction::class.java)), filter)
                    ?: getFirstMatchOrNull(getPreprocessorDefineFunctionVariables(element.getParentOfType( ObjJPreprocessorDefineFunction::class.java)), filter)
        // If variable has been found, return it
        if (variableName != null) {
            return variableName
        }

        // Check that index is ready before searching one
        if (DumbService.isDumb(element.project)) {
            return null
        }
        // Get global variables from index and return match if any
        val globalVariableDeclarations = ObjJGlobalVariableNamesIndex.instance[element.text, element.project]
        if (globalVariableDeclarations.isNotEmpty()) {
            return globalVariableDeclarations[0].variableName
        }
        return null
    }


    /**
     * Gets variable name declaration in containing block
     * @param element element who's parent we are searching for variable name declarations
     * @param qualifiedNameIndex qualified name index of the variable name to search
     * @param filter how to filter the results to return a single variable name
     */
    private fun getVariableNameDeclarationInContainingBlocks(element: PsiElement, qualifiedNameIndex: Int, filter: Filter<ObjJVariableName>): ObjJVariableName? {
        val block = element as? ObjJBlock ?: PsiTreeUtil.getParentOfType(element, ObjJBlock::class.java) ?: return null
        return getVariableNameDeclarationInContainingBlocks(block, element, qualifiedNameIndex, filter)
    }

    /**
     * Gets variable name declaration inside this block
     * @param block to search
     * @param qualifiedNameIndex qualified name index of the variable name to search
     * @param filter how to filter the results to return a single variable name
     */
    private fun getVariableNameDeclarationInContainingBlocks(block:ObjJBlock, element: PsiElement, qualifiedNameIndex: Int, filter: Filter<ObjJVariableName>): ObjJVariableName? {
        val bodyVariableAssignments = block.getBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true) as MutableList
        var out: ObjJVariableName?
        for (bodyVariableAssignment in bodyVariableAssignments) {
            if (bodyVariableAssignment notInSameFile element) {
               //LOGGER.severe("BodyVariableAssignment is not in same parent as element")
                continue
            }
            ProgressIndicatorProvider.checkCanceled()
            out = getVariableFromBodyVariableAssignment(bodyVariableAssignment, qualifiedNameIndex, filter)
            if (out != null && !out.isEquivalentTo(element) && out inSameFile element) {
                return out
            }
        }
        val parentBlock = block.getParentOfType(ObjJBlock::class.java)
        return if (parentBlock != null) return getVariableNameDeclarationInContainingBlocks(parentBlock, element, qualifiedNameIndex, filter) else null
    }

    /**
     * Gets all variable name assignments witin a child elements parent block
     */
    private fun getAllVariableNamesInAssignmentsInContainingBlocks(element: PsiElement, qualifiedNameIndex: Int): MutableList<ObjJVariableName> {
        val result = ArrayList<ObjJVariableName>()
        val block = element as? ObjJBlock ?: PsiTreeUtil.getParentOfType(element, ObjJBlock::class.java) ?: return result
        val bodyVariableAssignments = block.getBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true) as MutableList
        bodyVariableAssignments.addAll(block.getParentBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true))
        for (bodyVariableAssignment in bodyVariableAssignments) {
            ProgressIndicatorProvider.checkCanceled()
            result.addAll(getAllVariablesFromBodyVariableAssignment(bodyVariableAssignment, qualifiedNameIndex))
        }
        return result
    }

    private fun getAllContainingClassInstanceVariables(element: PsiElement): List<ObjJVariableName> {
        if (element is ObjJHasContainingClass)
            return getAllContainingClassInstanceVariables(element.containingClassName, element.project)
        val containingClass = element.getParentOfType(ObjJClassDeclarationElement::class.java)
                ?: return emptyList()
        return getAllContainingClassInstanceVariables(containingClass.classNameString, element.project)
    }

    fun getAllContainingClassInstanceVariables(containingClassName:String?, project:Project): List<ObjJVariableName> {
        val result = ArrayList<ObjJVariableName>()
        ////LOGGER.info("Getting all containing class instance variables: $containingClassName")
        if (DumbService.getInstance(project).isDumb) {
            ////LOGGER.info("Cannot get instance variable as project is in dumb mode");
            return EMPTY_VARIABLE_NAME_LIST
        }
        if (containingClassName == null || isUniversalMethodCaller(containingClassName)) {
            return EMPTY_VARIABLE_NAME_LIST
        }
        for (declaration in ObjJInstanceVariablesByClassIndex.instance[containingClassName, project]) {
            ProgressIndicatorProvider.checkCanceled()
            if (declaration.variableName != null) {
                result.add(declaration.variableName!!)
            }
        }

        for (variableHoldingClassName in ObjJInheritanceUtil.getAllInheritedClasses(containingClassName, project)) {
            // Item was already pulled outside of list
            if (variableHoldingClassName == containingClassName)
                continue
            ProgressIndicatorProvider.checkCanceled()
            for (declaration in ObjJInstanceVariablesByClassIndex.instance[variableHoldingClassName, project]) {
                ProgressIndicatorProvider.checkCanceled()
                if (declaration.variableName != null) {
                    result.add(declaration.variableName!!)
                }
            }
        }
        return result
    }

    private fun getAllMethodDeclarationSelectorVars(element: PsiElement): List<ObjJVariableName> {
        val result = ArrayList<ObjJVariableName>()
        val declaration = element.getParentOfType( ObjJMethodDeclaration::class.java)
        if (declaration != null) {
            for (methodDeclarationSelector in declaration.methodHeader.methodDeclarationSelectorList) {
                ProgressIndicatorProvider.checkCanceled()
                if (methodDeclarationSelector.variableName == null || methodDeclarationSelector.variableName!!.text.isEmpty()) {
                    ////LOGGER.info("Selector variable name is null");
                    continue
                }
                ////LOGGER.info("Adding method header selector: "+methodDeclarationSelector.getVariableName().getText());
                result.add(methodDeclarationSelector.variableName!!)
            }
        } else {
            ////LOGGER.info("Psi element is not within a variable declaration");
        }
        return result
    }

    fun getAllFileScopedVariables(file: PsiFile?, qualifiedNameIndex: Int): List<ObjJVariableName> {
        if (file == null) {
            ////LOGGER.info("Cannot get all file scoped variables. File is null");
            return EMPTY_VARIABLE_NAME_LIST
        }
        val result = ArrayList<ObjJVariableName>()
        val bodyVariableAssignments = file.getChildrenOfType( ObjJBodyVariableAssignment::class.java).filter {
            it.varModifier != null
        }
        result.addAll(getAllVariablesFromBodyVariableAssignmentsList(bodyVariableAssignments, qualifiedNameIndex))
        result.addAll(getAllFileScopeGlobalVariables(file))
        result.addAll(getAllPreProcDefinedVariables(file))
        return result
    }

    private fun getAllPreProcDefinedVariables(file: PsiFile): List<ObjJVariableName> {
        val definedFunctions: List<ObjJPreprocessorDefineFunction> = if (file is ObjJFile) {
            file.getChildrenOfType(ObjJPreprocessorDefineFunction::class.java)
        } else {
            file.getChildrenOfType( ObjJPreprocessorDefineFunction::class.java)
        }
        val out = ArrayList<ObjJVariableName>()
        for (function in definedFunctions) {
            if (function.variableName != null) {
                out.add(function.variableName!!)
            }
        }
        return out
    }

    private fun getAllFileScopeGlobalVariables(
            file: PsiFile?): List<ObjJVariableName> {
        if (file == null) {
            return EMPTY_VARIABLE_NAME_LIST
        }
        val result = ArrayList<ObjJVariableName>()
        val expressions = file.getChildrenOfType( ObjJExpr::class.java)
        for (expr in expressions) {
            ProgressIndicatorProvider.checkCanceled()
            val declaration = expr.leftExpr?.variableDeclaration ?: continue
            for (qualifiedReference in declaration.qualifiedReferenceList) {
                if (qualifiedReference.primaryVar != null) {
                    result.add(qualifiedReference.primaryVar!!)
                }
            }
        }
        return result
    }

    private fun getAllGlobalScopedFileVariables(
            file: PsiFile?): List<ObjJVariableName> {
        if (file == null) {
            return EMPTY_VARIABLE_NAME_LIST
        }
        val result = ArrayList<ObjJVariableName>()
        for (variableDeclaration in file.getChildrenOfType( ObjJGlobalVariableDeclaration::class.java)) {
            result.add(variableDeclaration.variableName)
        }
        return result
    }


    private fun getAllAtGlobalFileVariables(
            file: PsiFile?): List<ObjJVariableName> {
        if (file == null) {
            return EMPTY_VARIABLE_NAME_LIST
        }
        val result = ArrayList<ObjJVariableName>()
        for (variableDeclaration in file.getChildrenOfType( ObjJGlobal::class.java)) {
            val variableName =variableDeclaration.variableName ?: continue
            result.add(variableName)
        }
        return result
    }

    private fun getAllVariablesFromBodyVariableAssignmentsList(bodyVariableAssignments: List<ObjJBodyVariableAssignment>, qualifiedNameIndex: Int): List<ObjJVariableName> {
        if (bodyVariableAssignments.isEmpty()) {
            return EMPTY_VARIABLE_NAME_LIST
        }
        val result = ArrayList<ObjJVariableName>()
        for (bodyVariableAssignment in bodyVariableAssignments) {
            ProgressIndicatorProvider.checkCanceled()
            ////LOGGER.info("Body variable assignment: <"+bodyVariableAssignment.getText()+">");
            result.addAll(getAllVariablesFromBodyVariableAssignment(bodyVariableAssignment, qualifiedNameIndex))
        }
        return result

    }

    private fun getAllVariablesFromBodyVariableAssignment(bodyVariableAssignment: ObjJBodyVariableAssignment?, qualifiedNameIndex: Int): List<ObjJVariableName> {
        if (bodyVariableAssignment == null) {
            return EMPTY_VARIABLE_NAME_LIST
        }
        val result = bodyVariableAssignment.variableDeclarationList?.variableNameList?.toMutableList() ?: mutableListOf()
        val references = mutableListOf<ObjJQualifiedReference>()
        for (variableDeclaration in bodyVariableAssignment.variableDeclarationList?.variableDeclarationList ?: listOf()) {
            references.addAll(variableDeclaration.qualifiedReferenceList)
        }
        if (qualifiedNameIndex != 0) {
            return emptyList()
        }
        for (qualifiedReference in references) {
            ProgressIndicatorProvider.checkCanceled()
            if (qualifiedNameIndex == -1) {
                result.addAll(qualifiedReference.variableNameList)
            } else if (qualifiedReference.variableNameList.size > qualifiedNameIndex) {
                val suggestion = qualifiedReference.variableNameList[qualifiedNameIndex]
                result.add(suggestion)
            }
        }
        return result
    }

    private fun getVariableFromBodyVariableAssignment(bodyVariableAssignment: ObjJBodyVariableAssignment?, qualifiedNameIndex: Int, filter: Filter<ObjJVariableName>): ObjJVariableName? {
        if (bodyVariableAssignment == null) {
            return null
        }
        val references = mutableListOf<ObjJQualifiedReference>()
        for (variableDeclaration in bodyVariableAssignment.variableDeclarationList?.variableDeclarationList?: listOf()) {
            references.addAll(variableDeclaration.qualifiedReferenceList)
        }
        if (qualifiedNameIndex != 0) {
            return null
        }
        for (variableName in bodyVariableAssignment.variableDeclarationList?.variableNameList ?: listOf()) {
            if (filter(variableName)) {
                return variableName
            }
        }
        for (qualifiedReference in references) {
            ProgressIndicatorProvider.checkCanceled()
            if (qualifiedNameIndex == -1) {
                for (temp in qualifiedReference.variableNameList) {
                    if (filter(temp)) {
                        return temp
                    }
                }
            } else if (qualifiedReference.variableNameList.size > qualifiedNameIndex) {
                val suggestion = qualifiedReference.variableNameList[qualifiedNameIndex]
                if (filter(suggestion)) {
                    return suggestion
                }
            }
        }
        return null
    }

    private fun getAllFunctionScopeVariables(
            functionDeclarationElementIn: ObjJFunctionDeclarationElement<*>?): List<ObjJVariableName> {
        var functionDeclarationElement = functionDeclarationElementIn

        val result = mutableListOf<ObjJVariableName>()
        while (functionDeclarationElement != null) {
            if (functionDeclarationElement.formalParameterArgList.isNotEmpty()) {
                for (parameterArg in functionDeclarationElement.formalParameterArgList) {
                    if (parameterArg.variableName != null)
                        result.add(parameterArg.variableName!!)
                }
            }
            functionDeclarationElement = functionDeclarationElement.getParentOfType(ObjJFunctionDeclarationElement::class.java)
        }
        return result
    }

    private fun getAllIterationVariables(
            iterationStatementIn: ObjJIterationStatement?): List<ObjJVariableName> {
        var iterationStatement = iterationStatementIn
        val result = ArrayList<ObjJVariableName>()
        while (iterationStatement != null) {

            val variableDeclarationList:MutableList<ObjJVariableDeclaration> = mutableListOf()
            if (iterationStatement is ObjJForStatement) {
                variableDeclarationList.addAll(iterationStatement.forLoopHeader?.forLoopPartsInBraces?.variableDeclarationList?.variableDeclarationList ?: listOf())

                result.addAll(iterationStatement.forLoopHeader?.forLoopPartsInBraces?.variableDeclarationList?.variableNameList ?: listOf())
                if (iterationStatement.forLoopHeader?.forLoopPartsInBraces?.inExpr != null) {
                    result.add(iterationStatement.forLoopHeader!!.forLoopPartsInBraces!!.inExpr!!.variableName)
                }
            }


            ProgressIndicatorProvider.checkCanceled()

            // get regular variable declarations in iteration statement
            for (declaration in variableDeclarationList) {
                ProgressIndicatorProvider.checkCanceled()
                for (qualifiedReference in declaration.qualifiedReferenceList) {
                    result.add(qualifiedReference.primaryVar!!)
                }
            }
            iterationStatement = iterationStatement.getParentOfType( ObjJIterationStatement::class.java)
        }
        return result
    }

    private fun getCatchProductionVariables(
            catchProduction: ObjJCatchProduction?): List<ObjJVariableName> {
        return if (catchProduction == null) {
            EMPTY_VARIABLE_NAME_LIST
        } else listOf(catchProduction.variableName!!)
    }

    private fun getPreprocessorDefineFunctionVariables(
            function: ObjJPreprocessorDefineFunction?): List<ObjJVariableName> {
        if (function == null || function.formalParameterArgList.isEmpty()) {
            return EMPTY_VARIABLE_NAME_LIST
        }
        val result = mutableListOf<ObjJVariableName?>()
        for (formalParameterArg in function.formalParameterArgList) {
            result.add(formalParameterArg.variableName)
        }
        return result.filterNotNull()
    }

    fun isInstanceVariableDeclaredInClassOrInheritance(variableName: ObjJVariableName): Boolean {
        return getAllContainingClassInstanceVariables(variableName).firstOrNull { itVar -> itVar.text == variableName.text } != null
    }


    fun getFormalVariableInstanceVariables(variableName: ObjJQualifiedReferenceComponent) : List<ObjJVariableName>? {
        val index = variableName.indexInQualifiedReference
        if (index < 1) {
            return null
        }
        val baseVariableName: ObjJVariableName = variableName.getParentOfType(ObjJQualifiedReference::class.java)?.variableNameList?.getOrNull(index - 1)
                ?: return null

        if (baseVariableName == variableName) {
            return null
        }

        val variableType:String = when (baseVariableName.text) {
            "self" -> {
                (variableName as? ObjJHasContainingClass)?.containingClassName
            }
            "super" -> {
                (variableName as? ObjJHasContainingClass)?.getContainingSuperClass()?.text
            }
            else -> {
                val resolvedSibling = baseVariableName.reference.resolve() ?: return null
                resolvedSibling.getParentOfType(ObjJMethodDeclarationSelector::class.java)?.formalVariableType?.text ?:
                resolvedSibling.getParentOfType(ObjJInstanceVariableDeclaration::class.java)?.formalVariableType?.text
            }
        } ?: return null
        return getAllContainingClassInstanceVariables(variableType, variableName.project)
    }
}
