package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.indices.*
import com.google.common.collect.ImmutableList
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.utils.*
import com.intellij.openapi.project.Project
import org.fest.util.Lists

import java.util.ArrayList
import java.util.logging.Level
import java.util.logging.Logger

object ObjJVariableNameUtil {

    private val LOGGER = Logger.getLogger("ObjJVariableNameUtil")
    private val EMPTY_VARIABLE_NAME_LIST = ImmutableList.copyOf<ObjJVariableName>(arrayOfNulls(0))

    fun getMatchingPrecedingVariableNameElements(variableName: ObjJCompositeElement, qualifiedIndex: Int): List<ObjJVariableName> {
        val startOffset = variableName.textRange.startOffset
        val variableNameQualifiedString: String = if (variableName is ObjJVariableName) {
            getQualifiedNameAsString(variableName, qualifiedIndex)
        } else {
            //LOGGER.log(Level.WARNING, "Trying to match variable name element to a non variable name. Element is of type: "+variableName.getNode().toString()+"<"+variableName.getText()+">");
            variableName.text
        }

        val hasContainingClass = ObjJHasContainingClassPsiUtil.getContainingClass(variableName) != null
        return getAndFilterSiblingVariableNameElements(variableName, qualifiedIndex) { thisVariable -> isMatchingElement(variableNameQualifiedString, thisVariable, hasContainingClass, startOffset, qualifiedIndex) }
    }

    fun getMatchingPrecedingVariableAssignmentNameElements(variableName: ObjJCompositeElement, qualifiedIndex: Int): List<ObjJVariableName> {
        val startOffset = variableName.textRange.startOffset
        val variableNameQualifiedString: String
        if (variableName is ObjJVariableName) {
            variableNameQualifiedString = getQualifiedNameAsString(variableName, qualifiedIndex)
        } else {
            //LOGGER.log(Level.WARNING, "Trying to match variable name element to a non variable name. Element is of type: "+variableName.getNode().toString()+"<"+variableName.getText()+">");
            variableNameQualifiedString = variableName.text
        }
        val hasContainingClass = ObjJHasContainingClassPsiUtil.getContainingClass(variableName) != null
        return getAndFilterSiblingVariableAssignmentNameElements(variableName, qualifiedIndex, { thisVariable -> isMatchingElement(variableNameQualifiedString, thisVariable, hasContainingClass, startOffset, qualifiedIndex) })
    }

    private fun isMatchingElement(variableNameQualifiedString: String, variableToCheck: ObjJVariableName?, hasContainingClass: Boolean, startOffset: Int, qualifiedIndex: Int): Boolean {
        if (variableToCheck == null) {
            LOGGER.log(Level.SEVERE, "Variable name to check should not be null")
            return false
        }
        val thisVariablesFqName = getQualifiedNameAsString(variableToCheck, qualifiedIndex)
        //LOGGER.log(Level.INFO, "getMatchingPrecedingVariableNameElements: <"+variableNameQualifiedString+"> ?= <"+thisVariablesFqName+">");
        if (variableNameQualifiedString != thisVariablesFqName) {
            return false
        }
        //LOGGER.log(Level.INFO, "Variable names match for variable: <"+variableNameQualifiedString+">; Is Offset <"+startOffset+" < "+variableToCheck.getTextRange().getStartOffset() + "? " + (variableToCheck.getTextRange().getStartOffset() < startOffset));
        if (variableToCheck.containingClass == null) {
            if (hasContainingClass) {
                return true
            }
        } else if (hasContainingClass) {
            //return false;
        }
        return variableToCheck.textRange.startOffset < startOffset
    }

    @JvmOverloads
    fun getQualifiedNameAsString(variableName: ObjJVariableName, stopBeforeIndex: Int = -1): String {
        val qualifiedReference = variableName.getParentOfType( ObjJQualifiedReference::class.java)
        return getQualifiedNameAsString(qualifiedReference, variableName.text, stopBeforeIndex) ?: ""
    }

    @JvmOverloads
    fun getQualifiedNameAsString(qualifiedReference: ObjJQualifiedReference?, defaultValue: String?, stopBeforeIndex: Int = -1): String? {
        if (qualifiedReference == null) {
            return defaultValue
        }
        val variableNames = qualifiedReference.variableNameList
        if (variableNames.isEmpty()) {
            return defaultValue
        }
        val numVariableNames = if (stopBeforeIndex != -1 && variableNames.size > stopBeforeIndex) stopBeforeIndex else variableNames.size
        val builder = StringBuilder(variableNames[0].text)
        for (i in 1 until numVariableNames) {
            builder.append(".").append(variableNames[i].text)
        }
        //LOGGER.log(Level.INFO, "Qualified name is: <"+builder.toString()+"> for var in file: "+variableName.getContainingFile().getVirtualFile().getName()+"> at offset: <"+variableName.getTextRange().getStartOffset()+">");
        return builder.toString()
    }

    fun getPrecedingVariableAssignmentNameElements(variableName: PsiElement, qualifiedIndex: Int): List<ObjJVariableName> {
        val startOffset = variableName.textRange.startOffset
        val file = variableName.containingFile
        //LOGGER.log(Level.INFO, String.format("Qualified Index: <%d>; TextOffset: <%d>; TextRange: <%d,%d>", qualifiedIndex, variableName.getTextOffset(), variableName.getTextRange().getStartOffset(), variableName.getTextRange().getEndOffset()));
        return getAndFilterSiblingVariableAssignmentNameElements(variableName, qualifiedIndex, { `var` -> `var` !== variableName && (`var`.getContainingFile().isEquivalentTo(file) || `var`.getTextRange().getStartOffset() < startOffset) })
    }

    fun getPrecedingVariableNameElements(variableName: PsiElement, qualifiedIndex: Int): List<ObjJVariableName> {
        val startOffset = variableName.textRange.startOffset
        val file = variableName.containingFile
        //LOGGER.log(Level.INFO, String.format("Qualified Index: <%d>; TextOffset: <%d>; TextRange: <%d,%d>", qualifiedIndex, variableName.getTextOffset(), variableName.getTextRange().getStartOffset(), variableName.getTextRange().getEndOffset()));
        return getAndFilterSiblingVariableNameElements(variableName, qualifiedIndex, { `var` -> `var` !== variableName && (`var`.getContainingFile().isEquivalentTo(file) || `var`.getTextRange().getStartOffset() < startOffset) })
    }

    fun getAndFilterSiblingVariableAssignmentNameElements(element: PsiElement, qualifiedNameIndex: Int, filter: Filter<ObjJVariableName>): List<ObjJVariableName> {
        val rawVariableNameElements = getSiblingVariableAssignmentNameElements(element, qualifiedNameIndex)
//LOGGER.log(Level.INFO, String.format("Get Siblings by var name before filter. BeforeFilter<%d>; AfterFilter:<%d>", rawVariableNameElements.size(), foldingDescriptors.size()));
        return ArrayUtils.filter(rawVariableNameElements, filter)
    }

    fun getAndFilterSiblingVariableNameElements(element: PsiElement, qualifiedNameIndex: Int, filter: Filter<ObjJVariableName>): List<ObjJVariableName> {
        val rawVariableNameElements = getSiblingVariableNameElements(element, qualifiedNameIndex)
//LOGGER.log(Level.INFO, String.format("Get Siblings by var name before filter. BeforeFilter<%d>; AfterFilter:<%d>", rawVariableNameElements.size(), foldingDescriptors.size()));
        return ArrayUtils.filter(rawVariableNameElements, filter)
    }


    /**
     * Gets every preceding variable name element, even if it is not an assignment
     * @param element element to find siblings for
     * @param qualifiedNameIndex variable name index in chain, should be 0 for right now
     * @return list of variable name elements
     *
     * todo Allow checking of non 0 qualified name index
     */
    fun getSiblingVariableNameElements(element: PsiElement, qualifiedNameIndex: Int): List<ObjJVariableName> {
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
        //LOGGER.log(Level.INFO, "Num VariableNames after getting file vars: <"+(result.size()-currentSize)+">");
        return result
    }


    fun getSiblingVariableAssignmentNameElements(element: PsiElement, qualifiedNameIndex: Int): List<ObjJVariableName> {
        val result = getAllVariableNamesInAssignmentsInContainingBlocks(element, qualifiedNameIndex)
        var currentSize = result.size
        //LOGGER.log(Level.INFO, "Num from blocks: <"+currentSize+">");
        if (qualifiedNameIndex <= 1) {
            result.addAll(getAllContainingClassInstanceVariables(element))
            //LOGGER.log(Level.INFO, "Num VariableNames after class vars: <"+result.size()+">");
            currentSize = result.size
        }

        result.addAll(getAllAtGlobalFileVariables(element.containingFile))
        result.addAll(getAllGlobalScopedFileVariables(element.containingFile))
        result.addAll(getAllMethodDeclarationSelectorVars(element))
        result.addAll(getAllIterationVariables(element.getParentOfType( ObjJIterationStatement::class.java)))
        result.addAll(getAllFileScopedVariables(element.containingFile, qualifiedNameIndex))
        result.addAll(getAllFunctionScopeVariables(element.getParentOfType(ObjJFunctionDeclarationElement::class.java)))
        result.addAll(getCatchProductionVariables(element.getParentOfType( ObjJCatchProduction::class.java)))
        result.addAll(getPreprocessorDefineFunctionVariables(element.getParentOfType( ObjJPreprocessorDefineFunction::class.java)))
        //LOGGER.log(Level.INFO, "Num VariableNames after getting file vars: <"+(result.size()-currentSize)+">");
        return result
    }


    fun getSiblingVariableAssignmentNameElement(element: PsiElement, qualifiedNameIndex: Int, filter: Filter<ObjJVariableName>): ObjJVariableName? {
        var variableName: ObjJVariableName?
        variableName = getVariableNameDeclarationInContainingBlocks(element, qualifiedNameIndex, filter)
        if (variableName != null && variableName inSameFile element) {
            return if (!variableName.isEquivalentTo(element)) variableName else null
        }
        if (qualifiedNameIndex <= 1) {
            variableName = getFirstMatchOrNull(getAllMethodDeclarationSelectorVars(element), filter)
            if (variableName != null) {
                return if (!variableName.isEquivalentTo(element)) variableName else null
            }
            variableName = getFirstMatchOrNull(getAllContainingClassInstanceVariables(element), filter)
            if (variableName != null) {
                return if (!variableName.isEquivalentTo(element)) variableName else null
            }
            variableName = getFirstMatchOrNull(getAllIterationVariables(element.getParentOfType( ObjJIterationStatement::class.java)), filter)
            if (variableName != null) {
                return if (!variableName.isEquivalentTo(element)) variableName else null
            }
            variableName = getFirstMatchOrNull(getAllFunctionScopeVariables(element.getParentOfType( ObjJFunctionDeclarationElement::class.java)), filter)
            if (variableName != null) {
                return if (!variableName.isEquivalentTo(element)) variableName else null
            }
            variableName = getFirstMatchOrNull(getAllGlobalScopedFileVariables(element.containingFile), filter)
            if (variableName != null) {
                return variableName
            }
            variableName = getFirstMatchOrNull(getAllAtGlobalFileVariables(element.containingFile), filter)
            if (variableName != null) {
                return variableName
            }
        }

        variableName = getFirstMatchOrNull(getAllFileScopedVariables(element.containingFile, qualifiedNameIndex), filter)
        if (variableName != null) {
            return if (!variableName.isEquivalentTo(element)) variableName else null
        }
        variableName = getFirstMatchOrNull(getCatchProductionVariables(element.getParentOfType( ObjJCatchProduction::class.java)), filter)
        if (variableName != null) {
            return if (!variableName.isEquivalentTo(element)) variableName else null
        }
        variableName = getFirstMatchOrNull(getPreprocessorDefineFunctionVariables(element.getParentOfType( ObjJPreprocessorDefineFunction::class.java)), filter)
        if (variableName != null) {
            return if (!variableName.isEquivalentTo(element)) variableName else null
        }
        if (DumbService.isDumb(element.project)) {
            return null
        }
        val globalVariableDeclarations = ObjJGlobalVariableNamesIndex.instance[element.text, element.project] as MutableList
        if (!globalVariableDeclarations.isEmpty()) {
            return globalVariableDeclarations[0].variableName
        }
        return null//getVariableNameDeclarationInContainingBlocksFuzzy(element, qualifiedNameIndex, filter)
    }

    private fun getVariableNameDeclarationInContainingBlocksFuzzy(element: PsiElement, qualifiedNameIndex: Int, filter: Filter<ObjJVariableName>): ObjJVariableName? {
        val block = PsiTreeUtil.getTopmostParentOfType(element, ObjJBlock::class.java) ?: return null
        val varName = element.text
        val variableNames = block.getBlockChildrenOfType(ObjJVariableName::class.java, true)//ObjJVariableNameByScopeIndex.instance.getInRange(ObjJFileUtil.getContainingFileName(element.containingFile)!!, block.textRange, element.project)
        return getFirstMatchOrNull(variableNames) { variableName ->
            if (variableName.text != varName) {
                return@getFirstMatchOrNull false
            }
            var parent : PsiElement = variableName.parent as? ObjJQualifiedReference ?: return@getFirstMatchOrNull false
            parent = parent.parent
            parent is ObjJBodyVariableAssignment || parent is ObjJVariableDeclaration
        }
    }

    private fun getVariableNameDeclarationInContainingBlocks(element: PsiElement, qualifiedNameIndex: Int, filter: Filter<ObjJVariableName>): ObjJVariableName? {
        val block = element as? ObjJBlock ?: PsiTreeUtil.getParentOfType(element, ObjJBlock::class.java) ?: return null
        return getVariableNameDeclarationInContainingBlocks(block, element, qualifiedNameIndex, filter)
    }

    private fun getVariableNameDeclarationInContainingBlocks(block:ObjJBlock, element: PsiElement, qualifiedNameIndex: Int, filter: Filter<ObjJVariableName>): ObjJVariableName? {
        val bodyVariableAssignments = block.getBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true) as MutableList
        var out: ObjJVariableName?
        for (bodyVariableAssignment in bodyVariableAssignments) {
            if (bodyVariableAssignment notInSameFile element) {
                LOGGER.log(Level.SEVERE, "BodyVariableAssignment is not in same parent as element")
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

    fun getFirstMatchOrNull(variableNameElements: List<ObjJVariableName>, filter: Filter<ObjJVariableName>): ObjJVariableName? {
        for (variableName in variableNameElements) {
            ProgressIndicatorProvider.checkCanceled()
            if (filter(variableName)) {
                return variableName
            }
        }
        return null
    }

    private fun getAllVariableNamesInAssignmentsInContainingBlocks(element: PsiElement, qualifiedNameIndex: Int): MutableList<ObjJVariableName> {
        val result = ArrayList<ObjJVariableName>()
        val block = element as? ObjJBlock ?: PsiTreeUtil.getParentOfType(element, ObjJBlock::class.java) ?: return result
        val bodyVariableAssignments = block.getBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true) as MutableList
        bodyVariableAssignments.addAll(block!!.getParentBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true))
        for (bodyVariableAssignment in bodyVariableAssignments) {
            ProgressIndicatorProvider.checkCanceled()
            result.addAll(getAllVariablesFromBodyVariableAssignment(bodyVariableAssignment, qualifiedNameIndex))
        }
        return result
    }

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
            //LOGGER.log(Level.INFO, "Variable <"+element.getText()+">  is not in block");
            return EMPTY_VARIABLE_NAME_LIST
        }
        val containingFile = element.containingFile
        val fileName = ObjJFileUtil.getContainingFileName(containingFile)!!
//LOGGER.log(Level.INFO, "Variable <"+element.getText()+"> is in block in file: <"+fileName+"> at offset: "+containingBlock.getTextRange().getStartOffset());
        return ObjJVariableNameByScopeIndex.instance.getInRange(fileName, containingBlock.textRange, element.project)
    }

    private fun getAllContainingClassInstanceVariables(element: PsiElement): List<ObjJVariableName> {
        return if (element is ObjJHasContainingClass) getAllContainingClassInstanceVariables(element.containingClassName, element.project) else Lists.emptyList()
    }

    fun getAllContainingClassInstanceVariables(containingClassName:String?, project:Project): List<ObjJVariableName> {
        val result = ArrayList<ObjJVariableName>()
        //LOGGER.log(Level.INFO, "Getting all containing class instance variables: $containingClassName")
        if (DumbService.getInstance(project).isDumb) {
            //LOGGER.log(Level.INFO, "Cannot get instance variable as project is in dumb mode");
            return EMPTY_VARIABLE_NAME_LIST
        }
        if (containingClassName == null || isUniversalMethodCaller(containingClassName)) {
            return EMPTY_VARIABLE_NAME_LIST
        }
        for (variableHoldingClassName in ObjJInheritanceUtil.getAllInheritedClasses(containingClassName, project)) {
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
                    //LOGGER.log(Level.INFO, "Selector variable name is null");
                    continue
                }
                //LOGGER.log(Level.INFO, "Adding method header selector: "+methodDeclarationSelector.getVariableName().getText());
                result.add(methodDeclarationSelector.variableName!!)
            }
        } else {
            //LOGGER.log(Level.INFO, "Psi element is not within a variable declaration");
        }
        return result
    }

    fun getNamedElementList(reference:ObjJQualifiedReference) : List<ObjJNamedElement> {
        return reference.getChildrenOfType(ObjJNamedElement::class.java)
    }

    fun getQualifiedNameParts(reference:ObjJQualifiedReference) : List<ObjJQualifiedReferenceComponent> {
        return reference.getChildrenOfType(ObjJQualifiedReferenceComponent::class.java)
    }

    fun getIndexInQualifiedNameParent(elementToFindIndexFor: PsiElement?): Int {
        //Get named part, as those will be the direct child of the qualified reference
        val qualifiedNamePart = elementToFindIndexFor as? ObjJQualifiedReferenceComponent ?: elementToFindIndexFor.getParentOfType(ObjJQualifiedReferenceComponent::class.java) ?: return 0
        // Get the parent qualified reference, to find this elements index within it
        val qualifiedReferenceParent = qualifiedNamePart.getParentOfType( ObjJQualifiedReference::class.java) ?: return 0
        //Get te index in the named parts first
        val qualifiedNameIndex = qualifiedReferenceParent.qualifiedNameParts.indexOf(qualifiedNamePart)
        // Method call can be first, but is not included in named element list
        // if method call is first, this named element must be at least second.
        // so add 1 in place of the method call
        if (qualifiedReferenceParent.methodCall != null) {
            // can return, because if first var is method call,
            // it cannot be 'self' or 'super' so check is unnecessary
            return qualifiedNameIndex + 1
        }
        // If qualified reference is 'self' or 'super' then you
        // can subtract 1, so that the reference will still be resolved
        // Variable name reference will quit if index is greater than 1
        // in most instances
        else if ("self" == qualifiedReferenceParent.primaryVar?.text || "super" == qualifiedReferenceParent.primaryVar?.text) {
            return qualifiedNameIndex - 1
        }
        return qualifiedNameIndex
    }

    fun getAllFileScopedVariables(file: PsiFile?, qualifiedNameIndex: Int): List<ObjJVariableName> {
        if (file == null) {
            //LOGGER.log(Level.INFO, "Cannot get all file scoped variables. File is null");
            return EMPTY_VARIABLE_NAME_LIST
        }
        val result = ArrayList<ObjJVariableName>()
        val bodyVariableAssignments = file.getChildrenOfType( ObjJBodyVariableAssignment::class.java)
        result.addAll(getAllVariablesFromBodyVariableAssignmentsList(bodyVariableAssignments, qualifiedNameIndex))
        result.addAll(getAllFileScopeGlobalVariables(file))
        result.addAll(getAllPreProcDefinedVariables(file))
        return result
    }

    private fun getAllPreProcDefinedVariables(file: PsiFile): List<ObjJVariableName> {
        val definedFunctions: List<ObjJPreprocessorDefineFunction>
        if (file is ObjJFile) {
            definedFunctions = file.getChildrenOfType(ObjJPreprocessorDefineFunction::class.java)
        } else {
            definedFunctions = file.getChildrenOfType( ObjJPreprocessorDefineFunction::class.java)
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
            if (expr == null || expr.leftExpr == null || expr.leftExpr!!.variableDeclaration == null) {
                continue
            }
            val declaration = expr.leftExpr!!.variableDeclaration
            for (qualifiedReference in declaration!!.qualifiedReferenceList) {
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
            if (variableDeclaration.variableName != null) {
                result.add(variableDeclaration.variableName)
            }
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
            result.add(variableDeclaration.variableName)
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
            //LOGGER.log(Level.INFO, "Body variable assignment: <"+bodyVariableAssignment.getText()+">");
            result.addAll(getAllVariablesFromBodyVariableAssignment(bodyVariableAssignment, qualifiedNameIndex))
        }
        return result

    }

    private fun getAllVariablesFromBodyVariableAssignment(bodyVariableAssignment: ObjJBodyVariableAssignment?, qualifiedNameIndex: Int): List<ObjJVariableName> {
        if (bodyVariableAssignment == null) {
            return EMPTY_VARIABLE_NAME_LIST
        }
        val result = bodyVariableAssignment.variableNameList
        val references = mutableListOf<ObjJQualifiedReference>()
        for (variableDeclaration in bodyVariableAssignment.variableDeclarationList) {
            //LOGGER.log(Level.INFO,"VariableDec: <"+variableDeclaration.getText()+">");
            references.addAll(variableDeclaration.qualifiedReferenceList)
        }
        if (qualifiedNameIndex != 0) {
            return Lists.emptyList()
        }
        for (qualifiedReference in references) {
            ProgressIndicatorProvider.checkCanceled()
            //LOGGER.log(Level.INFO, "Checking variable dec for qualified reference: <"+qualifiedReference.getText()+">");
            if (qualifiedNameIndex == -1) {
                result.addAll(qualifiedReference.variableNameList)
            } else if (qualifiedReference.variableNameList.size > qualifiedNameIndex) {
                val suggestion = qualifiedReference.variableNameList[qualifiedNameIndex]
                result.add(suggestion)
            } else {
                //LOGGER.log(Level.INFO, "Not adding variable <"+qualifiedReference.getText()+"> as Index is foldingDescriptors of bounds.");
            }
        }
        return result
    }

    private fun getVariableFromBodyVariableAssignment(bodyVariableAssignment: ObjJBodyVariableAssignment?, qualifiedNameIndex: Int, filter: Filter<ObjJVariableName>): ObjJVariableName? {
        if (bodyVariableAssignment == null) {
            return null
        }
        val references = mutableListOf<ObjJQualifiedReference>()
        for (variableDeclaration in bodyVariableAssignment.variableDeclarationList) {
            references.addAll(variableDeclaration.qualifiedReferenceList)
        }
        if (qualifiedNameIndex != 0) {
            return null
        }
        for (variableName in bodyVariableAssignment.variableNameList) {
            if (filter(variableName)) {
                return variableName
            }
        }
        for (qualifiedReference in references) {
            ProgressIndicatorProvider.checkCanceled()
            //LOGGER.log(Level.INFO, "Checking variable dec for qualified reference: <"+qualifiedReference.getText()+">");
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
            } else {
                //LOGGER.log(Level.INFO, "Not adding variable <"+qualifiedReference.getText()+"> as Index is foldingDescriptors of bounds.");
            }
        }
        return null
    }

    private fun getAllFunctionScopeVariables(
            functionDeclarationElement: ObjJFunctionDeclarationElement<*>?): List<ObjJVariableName> {
        if (functionDeclarationElement == null || functionDeclarationElement.formalParameterArgList.isEmpty()) {
            return EMPTY_VARIABLE_NAME_LIST
        }
        val result = ArrayList<ObjJVariableName>()
        for (parameterArg in functionDeclarationElement.formalParameterArgList) {
            result.add((parameterArg as ObjJFormalParameterArg).variableName)
        }
        return result
    }

    fun getAllParentBlockVariables(element: PsiElement?, qualifiedIndex: Int): List<ObjJVariableName> {
        val result = ArrayList<ObjJVariableName>()
        if (element == null) {
            return result
        }
        for (declaration in element.getParentBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true)) {
            ProgressIndicatorProvider.checkCanceled()
            //LOGGER.log(Level.INFO, "Adding all iteration statement variables for dec: <"+declaration.getText()+">");
            result.addAll(getAllVariablesFromBodyVariableAssignment(declaration, qualifiedIndex))
        }
        return result
    }

    private fun getAllIterationVariables(
            iterationStatement: ObjJIterationStatement?): List<ObjJVariableName> {
        var iterationStatement = iterationStatement
        val result = ArrayList<ObjJVariableName>()
        while (iterationStatement != null) {
            ProgressIndicatorProvider.checkCanceled()
            //Get variable if in an `in` statement
            //i.e.  `for (var v in ob)`
            if (iterationStatement.inExpr != null) {
                result.add(iterationStatement.inExpr!!.variableName)
            }

            // get regular variable declarations in iteration statement
            for (declaration in iterationStatement.variableDeclarationList) {
                ProgressIndicatorProvider.checkCanceled()
                //LOGGER.log(Level.INFO, "Adding all iteration statement variables for dec: <"+declaration.getText()+">");
                for (qualifiedReference in declaration.qualifiedReferenceList) {
                    result.add(qualifiedReference.primaryVar!!)
                }
            }
            for (reference in iterationStatement.variableNameList) {
                result.add(reference)
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
        val result = ArrayList<ObjJVariableName>()
        for (formalParameterArg in function.formalParameterArgList) {
            result.add(formalParameterArg.variableName)
        }
        return result
    }

    fun isInstanceVarDeclaredInClassOrInheritance(variableName: ObjJVariableName): Boolean {
        return getFirstMatchOrNull(getAllContainingClassInstanceVariables(variableName), { `var` -> `var`.text == variableName.text }) != null
    }

    fun variablesShareInstanceVariableScope(variableName: ObjJVariableName, variableName2: ObjJVariableName) : Boolean {
        return instanceVariablesSharedClasses(variableName, variableName2).isNotEmpty()
    }

    fun instanceVariablesSharedClasses(variableName1:ObjJVariableName, variableName2:ObjJVariableName) : Set<String> {
        return getVariableWithNameContainingClasses(variableName1) intersect getVariableWithNameContainingClasses(variableName2)
    }

    fun getVariableWithNameContainingClasses(variableNameElement:ObjJVariableName) : Set<String> {
        val project = variableNameElement.project
        val variableName = variableNameElement.text
        val className = variableNameElement.containingClassName
        val containingClassInheritedClasses:List<String> = ObjJInheritanceUtil.getAllInheritedClassesStrict(className, project)
        val classesContainingInstanceVariableWithName:List<String> = ObjJInstanceVariablesByNameIndex.instance[variableName, project].map {
            it.containingClassName
        }
        return containingClassInheritedClasses intersect classesContainingInstanceVariableWithName
    }


    fun resolveQualifiedReferenceVariable(variableName:ObjJVariableName) : ObjJVariableName? {
        val formalVariableTypeInstanceVariableList = getFormalVariableInstanceVariables(variableName) ?: return null
        return getFirstMatchOrNull(formalVariableTypeInstanceVariableList, { `var` -> `var`.text == variableName.text })
    }

    fun getFormalVariableInstanceVariables(variableName: ObjJVariableName) : List<ObjJVariableName>? {
        val index = variableName.indexInQualifiedReference
        if (index < 1) {
            //LOGGER.log(Level.INFO, "ObjJVariableNameUtil.getFormalVariableInstanceVariables(${variableName.text}) Index is less than 1")
            return null
        }
        val baseVariableName: ObjJVariableName = variableName.getParentOfType(ObjJQualifiedReference::class.java)?.variableNameList?.get(index - 1)
                ?: return null
        val variableType:String = when (baseVariableName.text) {
            "self" -> {
                //LOGGER.log(Level.INFO, "Getting instance variable completions for self")
                variableName.containingClassName
            }
            "super" -> {
                //LOGGER.log(Level.INFO, "Getting instance variable completions for super")
                variableName.getContainingSuperClass()?.text
            }
            else -> {
                //LOGGER.log(Level.INFO, "Getting instance variable completions for variable ${variableName.text}")
                val resolvedSibling = baseVariableName.reference.resolve() ?: return null
                resolvedSibling.getParentOfType(ObjJMethodDeclarationSelector::class.java)?.formalVariableType?.text ?:
                resolvedSibling.getParentOfType(ObjJInstanceVariableDeclaration::class.java)?.formalVariableType?.text
            }
        } ?: return null
        //LOGGER.log(Level.INFO, "Resolved variable is of type: "+variableType)
        return getAllContainingClassInstanceVariables(variableType, variableName.project)
    }


}
