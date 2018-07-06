package cappuccino.ide.intellij.plugin.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import cappuccino.ide.intellij.plugin.contributor.ObjJKeywordsList
import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.indices.ObjJInstanceVariablesByNameIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.ObjJVariableReference
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.AnnotationLevel
import cappuccino.ide.intellij.plugin.settings.ObjJVariableAnnotatorSettings
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil

import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * A class used to annotate variable references
 */
internal object ObjJVariableAnnotatorUtil {

    private val LOGGER = Logger.getLogger(ObjJVariableAnnotatorUtil::class.java.name)
    private val OVERSHADOWS_VARIABLE_STRING_FORMAT = "Variable overshadows existing variable in %s"
    private val OVERSHADOWS_FUNCTION_NAME_STRING_FORMAT = "Variable overshadows function with name %s"
    private val OVERSHADOWS_METHOD_HEADER_VARIABLE = "Variable overshadows method variable"
    private val STATIC_VAR_NAMES = Arrays.asList("this", "Array", "ObjectiveJ", "arguments", "document", "window")
    //private static final HashMap<PsiFile, List<Integer>> checked = new HashMap<>();

    /**
     * Annotate variable name element
     * @param variableName variable name element
     * @param annotationHolder annotation holder
     */
    fun annotateVariable(
            variableName: ObjJVariableName?,
            annotationHolder: AnnotationHolder) {
        if (variableName == null || variableName.text.isEmpty()) {
            LOGGER.log(Level.WARNING, "Var Name Is Null for annotator.")
            return
        }

        val prevNode = variableName.getPreviousNonEmptyNode(true)
        if (prevNode != null && prevNode.elementType === ObjJTypes.ObjJ_DOT) {
            return
        }

        //LOGGER.log(Level.INFO, "Checking variableName <"+variableName.getText()+">");
        if (DumbService.isDumb(variableName.project)) {
            DumbService.getInstance(variableName.project).smartInvokeLater { annotateVariable(variableName, annotationHolder) }
            return
        }
        annotateIfVariableOvershadows(variableName, annotationHolder)
        annotateIfVariableIsNotDeclaredBeforeUse(variableName, annotationHolder)
    }

    private fun annotateIfVariableIsNotDeclaredBeforeUse(variableNameIn: ObjJVariableName, annotationHolder: AnnotationHolder) {
        var variableName : ObjJVariableName? = variableNameIn

        if (variableName?.getParentOfType( ObjJInstanceVariableList::class.java) != null) {
            when (variableName.text) {
                "super", "this", "self" -> annotationHolder.createErrorAnnotation(variableName, "Using reserved variable name")
            }
            return
        }
        if (variableName?.parent is ObjJQualifiedReference) {
            variableName = (variableName.parent!! as ObjJQualifiedReference).primaryVar
        }
        if (variableName == null) {
            return
        }

        if (STATIC_VAR_NAMES.contains(variableName.text)) {
            annotateStaticVariableNameReference(variableName, annotationHolder)
            return
        }

        if (isItselfAVariableDeclaration(variableName)) {
            return
        }
        val project = variableName.project
        if (DumbService.isDumb(project)) {
            LOGGER.log(Level.WARNING, "annotating variable should have been skipped if in dumb mode")
            return
        }

        if (!ObjJClassDeclarationsIndex.instance.get(variableName.text, variableName.project).isEmpty()) {
            return
        }

        if (isDeclaredInEnclosingScopesHeader(variableName)) {
            return
        }

        if (isVariableDeclaredBeforeUse(variableName)) {
            //LOGGER.log(Level.INFO, "Variable is <" + variableName.getText() + "> declared before use.");
            return
        }


        var tempElement = variableName.getNextNonEmptySibling(true)
        if (tempElement != null && tempElement.text == ".") {
            tempElement = tempElement.getNextNonEmptySibling(true)
            if (tempElement is ObjJFunctionCall) {
                val functionCall = tempElement as ObjJFunctionCall?
                if (functionCall!!.functionName != null && functionCall.functionName!!.text == "call") {
                    if (ObjJFunctionsIndex.instance.get(variableName.name, variableName.project).isEmpty()) {
                        annotationHolder.createWarningAnnotation(variableName, "Failed to find function with name <" + variableName.name + ">")
                    }
                    return
                }
            }
        }
        val declarations : MutableList<ObjJGlobalVariableDeclaration> = ObjJGlobalVariableNamesIndex.instance.get(variableName.text, variableName.project)
        if (!declarations.isEmpty()) {
            annotationHolder.createInfoAnnotation(variableName, "References global variable in file <" + (if (declarations.get(0).fileName != null) declarations.get(0).fileName else "UNDEFINED" + ">") + ">")
            return
        }
        if (variableName.text.substring(0, 1) == variableName.text.substring(0, 1).toUpperCase()) {
            //annotationHolder.createWeakWarningAnnotation(variableName,"Variable may reference javascript class");
            return
        }

        if (variableName.hasText("self") || variableName.hasText("super")) {
            if (isUniversalMethodCaller(variableName.containingClassName)) {
                annotationHolder.createErrorAnnotation(variableName, variableName.text + " used outside of class")
            }
            return
        }
        //LOGGER.log(Level.INFO, "Var <" + variableName.getText() + "> is undeclared.");
        annotationHolder.createWeakWarningAnnotation(variableName.textRange, "Variable may not have been declared before use")

    }

    private fun isVariableDeclaredBeforeUse(variableName: ObjJVariableName): Boolean {
        if (ObjJKeywordsList.keywords.contains(variableName.text)) {
            return true
        }
        val precedingVariableNameReferences = ObjJVariableNameUtil.getMatchingPrecedingVariableNameElements(variableName, 0)
        return !precedingVariableNameReferences.isEmpty() || !ObjJFunctionsIndex.instance.get(variableName.text, variableName.project).isEmpty() || ObjJVariableReference(variableName).resolve() != null
    }

    private fun annotateStaticVariableNameReference(variableName: ObjJVariableName, annotationHolder: AnnotationHolder) {
        val variableNameString = variableName.text
        when (variableNameString) {
            "this" -> if (variableName.getParentOfType( ObjJBlock::class.java) == null) {
                annotationHolder.createWarningAnnotation(variableName, "Possible misuse of 'this' outside of block")
            }
        }
    }

    private fun isDeclaredInEnclosingScopesHeader(variableName: ObjJVariableName): Boolean {
        return ObjJVariableNameUtil.isInstanceVarDeclaredInClassOrInheritance(variableName) ||
                isDeclaredInContainingMethodHeader(variableName) ||
                isDeclaredInFunctionScope(variableName) ||
                !ObjJVariableNameUtil.getMatchingPrecedingVariableNameElements(variableName, 0).isEmpty()
    }

    private fun isDeclaredInContainingMethodHeader(variableName: ObjJVariableName): Boolean {
        val methodDeclaration = variableName.getParentOfType( ObjJMethodDeclaration::class.java)
        return methodDeclaration != null && ObjJMethodPsiUtils.getHeaderVariableNameMatching(methodDeclaration.methodHeader, variableName.text) != null
    }

    private fun isDeclaredInFunctionScope(variableName: ObjJVariableName): Boolean {
        val functionDeclarationElement = variableName.getParentOfType( ObjJFunctionDeclarationElement::class.java)
        if (functionDeclarationElement != null) {
            for (ob in functionDeclarationElement.formalParameterArgList) {
                if (ob.variableName.text == variableName.text) {
                    return true
                }
            }
        }
        return false
    }

    private fun isItselfAVariableDeclaration(variableName: ObjJVariableName): Boolean {
        //If variable name is itself an instance variable
        if (variableName.parent is ObjJInstanceVariableDeclaration) {
            return true
        }
        //If variable name element is itself a method header declaration variable
        if (variableName.getParentOfType( ObjJMethodHeaderDeclaration::class.java) != null) {
            return true
        }

        if (variableName.parent is ObjJGlobalVariableDeclaration) {
            return true
        }

        //If variable name is itself an function variable
        if (variableName.parent is ObjJFormalParameterArg) {
            return true
        }

        //If variable name itself is declared in catch header in try/catch block
        if (variableName.parent is ObjJCatchProduction) {
            return true
        }
        //If variable name itself a javascript object property name
        if (variableName.parent is ObjJPropertyAssignment) {
            return true
        }

        if (variableName.parent is ObjJInExpr) {
            return true
        }

        if (variableName.getParentOfType(ObjJPreprocessorDefineFunction::class.java) != null) {
            return true
        }

        if (variableName.parent is ObjJGlobal) {
            return true
        }

        val reference = variableName.getParentOfType( ObjJQualifiedReference::class.java) ?: return false

        if (reference.parent is ObjJBodyVariableAssignment) {
            return (reference.parent as ObjJBodyVariableAssignment).varModifier != null
        }

        if (reference.parent is ObjJIterationStatement) {
            return true
        }

        var assignment: ObjJBodyVariableAssignment? = null
        if (reference.parent is ObjJVariableDeclaration) {
            val variableDeclaration = reference.parent as ObjJVariableDeclaration
            if (variableDeclaration.parent is ObjJIterationStatement && variableDeclaration.siblingOfTypeOccursAtLeastOnceBefore(ObjJVarModifier::class.java)) {
                return true
            } else if (variableDeclaration.parent is ObjJGlobalVariableDeclaration) {
                return true
            }// else {
            //LOGGER.log(Level.INFO, "Variable declaration has a parent of type: <"+variableDeclaration.getParent().getNode().getElementType().toString()+">");
            //}
            assignment = if (variableDeclaration.parent is ObjJBodyVariableAssignment) variableDeclaration.parent as ObjJBodyVariableAssignment else null
        }
        return assignment != null && assignment.varModifier != null
    }

    /**
     * Annotates variable if it overshadows a variable in enclosing scope
     * @param variableName variable to possibly annotate
     * @param annotationHolder annotation holder
     */
    private fun annotateIfVariableOvershadows(variableName: ObjJVariableName, annotationHolder: AnnotationHolder) {
        if (variableName.text.isEmpty() || !isItselfAVariableDeclaration(variableName)) {
            return
        }
        val variableList = variableName.getParentOfType( ObjJInstanceVariableList::class.java)
        if (variableList != null) {
            val thisInstanceVariable = variableName.getParentOfType(ObjJInstanceVariableDeclaration::class.java)
            val startOffset = thisInstanceVariable?.textRange?.startOffset ?: 0
            val variableNameString = variableName.text
            for (instanceVariableDeclaration in variableList.instanceVariableDeclarationList) {
                if (instanceVariableDeclaration.variableName == null) {
                    continue
                }
                if (instanceVariableDeclaration.variableName!!.hasText(variableNameString) && instanceVariableDeclaration.textRange.startOffset < startOffset) {
                    annotationHolder.createErrorAnnotation(variableName, "Variable with name already declared.")
                    return
                }
            }
        }
        if (isBodyVariableAssignment(variableName)) {
            annotateIfOvershadowsBlocks(variableName, annotationHolder)
            annotateIfOvershadowsMethodVariable(variableName, annotationHolder)
            annotateVariableIfOvershadowInstanceVariable(variableName, annotationHolder)
            annotateVariableIfOvershadowsFileVars(variableName, annotationHolder)
        } else if (isInstanceVariable(variableName)) {
            annotateVariableIfOvershadowsFileVars(variableName, annotationHolder)
        }
    }

    /**
     * Checks whether this variable is a body variable assignment declaration
     * @param variableName variable name element
     * @return `true` if variable name element is part of a variable declaration
     */
    private fun isBodyVariableAssignment(variableName: ObjJVariableName): Boolean {
        val bodyVariableAssignment = variableName.getParentOfType( ObjJBodyVariableAssignment::class.java)
        return bodyVariableAssignment != null && bodyVariableAssignment.varModifier != null
    }

    /**
     * Checks whether this variable name is part of a instance variable declaration
     * @param variableName variable name element
     * @return `true` if variable name is part of instance variable declaration
     */
    private fun isInstanceVariable(variableName: ObjJVariableName): Boolean {
        return variableName.getParentOfType( ObjJInstanceVariableDeclaration::class.java) != null
    }

    private fun annotateIfOvershadowsMethodVariable(variableName: ObjJVariableName, annotationHolder: AnnotationHolder) {
        //Variable is defined in header itself
        if (variableName.getParentOfType(ObjJMethodHeader::class.java) != null) {
            return
        }
        //Check if method is actually in a method declaration
        val methodDeclaration = variableName.getParentOfType(ObjJMethodDeclaration::class.java) ?: return

        //Check if variable overshadows variable defined in method header
        if (ObjJMethodPsiUtils.getHeaderVariableNameMatching(methodDeclaration.methodHeader, variableName.text) != null) {
            createAnnotation(ObjJVariableAnnotatorSettings.OVERSHADOWS_METHOD_VARIABLE_SETTING.value!!, variableName, OVERSHADOWS_METHOD_HEADER_VARIABLE, annotationHolder)
        }
    }

    /**
     * Annotates a body variable assignment if it overshadows an instance variable
     * @param variableName variable name element
     * @param annotationHolder annotation holder
     */
    private fun annotateVariableIfOvershadowInstanceVariable(variableName: ObjJVariableName, annotationHolder: AnnotationHolder) {
        val project = variableName.project
        val classDeclarationElement = variableName.getParentOfType( ObjJClassDeclarationElement::class.java)
                ?: return
        val variableContainingClass = classDeclarationElement.getClassNameString()
        var scope: String? = null
        val inheritedClassNames = ObjJInheritanceUtil.getAllInheritedClasses(classDeclarationElement.getClassNameString(), classDeclarationElement.project)
        val annotationLevel = ObjJVariableAnnotatorSettings.OVERSHADOWS_INSTANCE_VARIABLE_SETTING.value
        for (instanceVariableDeclaration in ObjJInstanceVariablesByNameIndex.instance.get(classDeclarationElement.getClassNameString(), project)) {
            ProgressIndicatorProvider.checkCanceled()
            val instanceVarContainingClass = instanceVariableDeclaration.containingClassName
            if (instanceVarContainingClass == variableContainingClass) {
                scope = "containing class <" + classDeclarationElement.getClassNameString() + ">"
                break
            }
            if (inheritedClassNames.contains(instanceVarContainingClass)) {
                scope = "parent class <$instanceVarContainingClass>"
                break
            }
        }
        if (scope != null) {
            createAnnotation(annotationLevel!!, variableName, String.format(OVERSHADOWS_VARIABLE_STRING_FORMAT, scope), annotationHolder)
        }
    }

    private fun annotateIfOvershadowsBlocks(variableName: ObjJVariableName, annotationHolder: AnnotationHolder) {
        val bodyVariableAssignments = variableName.getParentBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true)
        if (bodyVariableAssignments.isEmpty()) {
            return
        }
        val offset = variableName.textRange.startOffset
        val variableNameString = variableName.text
        val annotationLevel = ObjJVariableAnnotatorSettings.OVERSHADOWS_BLOCK_VARIABLE_SETTING.value
        for (bodyVariableAssignment in bodyVariableAssignments) {
            if (isDeclaredInBodyVariableAssignment(bodyVariableAssignment, variableNameString, offset)) {
                createAnnotation(annotationLevel!!, variableName, "Variable overshadows variable in enclosing block", annotationHolder)
                return
            }
        }
    }

    private fun isDeclaredInBodyVariableAssignment(variableAssignment: ObjJBodyVariableAssignment, variableNameString: String, offset: Int): Boolean {
        if (variableAssignment.varModifier == null) {
            return false
        }
        val qualifiedReferences = variableAssignment.qualifiedReferenceList
        val varNames = ArrayList<ObjJVariableName>()
        for (declaration in variableAssignment.variableDeclarationList) {
            qualifiedReferences.addAll(declaration.qualifiedReferenceList)
        }
        for (qualifiedReference in qualifiedReferences) {
            varNames.add(qualifiedReference.primaryVar!!)
        }
        return ObjJVariableNameUtil.getFirstMatchOrNull(varNames) { it.text == variableNameString && offset > it.textRange.startOffset } != null
    }

    /**
     * Annotes variable if it overshadows any file scoped variables or function names
     * @param variableName variable name
     * @param annotationHolder annotation holder
     */
    private fun annotateVariableIfOvershadowsFileVars(variableName: ObjJVariableName, annotationHolder: AnnotationHolder) {
        val file = variableName.containingFile
        val reference = ObjJVariableNameUtil.getFirstMatchOrNull(ObjJVariableNameUtil.getAllFileScopedVariables(file, 0)) { `var` -> variableName.text == `var`.text }
        if (reference != null && reference !== variableName) {
            annotationHolder.createWarningAnnotation(variableName, String.format(OVERSHADOWS_VARIABLE_STRING_FORMAT, "file scope"))
            return
        }
        val annotationLevel = ObjJVariableAnnotatorSettings.OVERSHADOWS_FILE_VARIABLE_SETTING.value
        for (declarationElement in ObjJFunctionsIndex.instance.get(variableName.text, variableName.project)) {
            ProgressIndicatorProvider.checkCanceled()
            if (declarationElement.getContainingFile().isEquivalentTo(file) && declarationElement.functionNameNode != null && variableName.textRange.startOffset > declarationElement.functionNameNode!!.getTextRange().getStartOffset()) {
                createAnnotation(annotationLevel!!, variableName, String.format(OVERSHADOWS_FUNCTION_NAME_STRING_FORMAT, variableName.text), annotationHolder)
            }

        }
    }

    private fun createAnnotation(level: AnnotationLevel, target: PsiElement, message: String, annotationHolder: AnnotationHolder) {
        when (level) {
            AnnotationLevel.ERROR -> {
                annotationHolder.createErrorAnnotation(target, message)
                return
            }
            AnnotationLevel.WARNING -> {
                annotationHolder.createWarningAnnotation(target, message)
                return
            }
            AnnotationLevel.WEAK_WARNING -> annotationHolder.createWeakWarningAnnotation(target, message)
        }
    }

    private fun createAnnotation(level: AnnotationLevel, target: TextRange, message: String, annotationHolder: AnnotationHolder) {
        when (level) {
            AnnotationLevel.ERROR -> {
                annotationHolder.createErrorAnnotation(target, message)
                return
            }
            AnnotationLevel.WARNING -> {
                annotationHolder.createWarningAnnotation(target, message)
                return
            }
            AnnotationLevel.WEAK_WARNING -> annotationHolder.createWeakWarningAnnotation(target, message)
        }
    }
}
