package org.cappuccino_project.ide.intellij.plugin.psi.utils

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException
import org.cappuccino_project.ide.intellij.plugin.contributor.ObjJMethodCallCompletionContributorUtil
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJIcons
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.*
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes
import org.cappuccino_project.ide.intellij.plugin.references.*
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.references.presentation.ObjJSelectorItemPresentation
import org.cappuccino_project.ide.intellij.plugin.settings.ObjJPluginSettings
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJResolveableStub
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil

import javax.swing.*
import java.util.*

import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.MatchResult
import java.util.regex.Pattern

object ObjJPsiImplUtil {

    private val LOGGER = Logger.getLogger(ObjJPsiImplUtil::class.java.name)

    // ============================== //
    // ======= Named Ident ========== //
    // ============================== //

    fun hasText(variableName: ObjJVariableName, text: String): Boolean {
        return ObjJNamedPsiUtil.hasText(variableName, text)
    }

    fun getName(variableName: ObjJVariableName): String {
        return ObjJNamedPsiUtil.getName(variableName)
    }

    fun getName(selector: ObjJSelector): String {
        return ObjJMethodPsiUtils.getName(selector)
    }

    fun getName(defineFunction: ObjJPreprocessorDefineFunction): String {
        return defineFunction.functionNameAsString
    }

    fun getName(functionName: ObjJFunctionName): String {
        return functionName.text
    }

    fun getName(methodHeader: ObjJMethodHeader): String {
        return ObjJMethodPsiUtils.getName(methodHeader)
    }

    fun getName(variableDeclaration: ObjJInstanceVariableDeclaration): String {
        return if (variableDeclaration.variableName != null) variableDeclaration.variableName!!.text else ""
    }

    fun getNameIdentifier(selector: ObjJSelector): PsiElement {
        return ObjJMethodPsiUtils.getNameIdentifier(selector)
    }

    fun getRangeInElement(selector: ObjJSelector): TextRange {
        return ObjJMethodPsiUtils.getRangeInElement(selector)
    }

    fun getRangeInElement(variableName: ObjJVariableName): TextRange {
        return TextRange(0, variableName.textLength)
    }

    fun setName(defineFunction: ObjJPreprocessorDefineFunction, name: String): PsiElement {
        return ObjJFunctionDeclarationPsiUtil.setName(defineFunction, name)
    }

    fun setName(selectorElement: ObjJSelector, newSelectorValue: String): PsiElement {
        return ObjJMethodPsiUtils.setName(selectorElement, newSelectorValue)
    }

    fun setName(variableName: ObjJVariableName, newName: String): PsiElement {
        return ObjJNamedPsiUtil.setName(variableName, newName)
    }

    @Throws(IncorrectOperationException::class)
    fun setName(header: ObjJHasMethodSelector, name: String): PsiElement {
        return ObjJMethodPsiUtils.setName(header, name)
    }

    fun setName(instanceVariableDeclaration: ObjJInstanceVariableDeclaration, newName: String): PsiElement {
        return ObjJVariablePsiUtil.setName(instanceVariableDeclaration, newName)
    }


    fun setName(oldFunctionName: ObjJFunctionName, newFunctionName: String): ObjJFunctionName {
        if (newFunctionName.isEmpty()) {
            return oldFunctionName
        }
        val functionName = ObjJElementFactory.createFunctionName(oldFunctionName.project, newFunctionName)
        if (functionName == null) {
            LOGGER.log(Level.SEVERE, "new function name element is null")
            return oldFunctionName
        }
        oldFunctionName.parent.node.replaceChild(oldFunctionName.node, functionName.node)
        return functionName
    }

    // ============================== //
    // ====== Navigation Items ====== //
    // ============================== //

    fun getPresentation(selector: ObjJSelector): ItemPresentation {
        //LOGGER.log(Level.INFO, "Getting selector item presentation.");
        return ObjJSelectorItemPresentation(selector)
    }


    // ============================== //
    // =========== String =========== //
    // ============================== //

    fun getStringValue(stringLiteral: ObjJStringLiteral): String {
        val rawText = stringLiteral.text
        val pattern = Pattern.compile("@?\"(.*)\"|@?'(.*)'")
        val result = pattern.matcher(rawText)
        try {
            return if (result.groupCount() > 1 && result.group(1) != null) result.group(1) else ""
        } catch (e: Exception) {
            return ""
        }

    }

    // ============================== //
    // ======= Method Misc ========== //
    // ============================== //

    fun getMethodScope(methodHeader: ObjJMethodHeader): MethodScope {
        return ObjJMethodPsiUtils.getMethodScope(methodHeader)
    }

    fun getMethodScope(accessorProperty: ObjJAccessorProperty): MethodScope {
        return ObjJMethodPsiUtils.getMethodScope(accessorProperty)
    }

    fun getMethodScope(literal: ObjJSelectorLiteral): MethodScope {
        return ObjJMethodPsiUtils.getMethodScope(literal)
    }

    fun isStatic(hasMethodSelector: ObjJHasMethodSelector): Boolean {
        return ObjJMethodPsiUtils.isStatic(hasMethodSelector)
    }

    fun getPossibleCallTargetTypes(callTarget: ObjJMethodCall): List<String> {
        return if (callTarget.stub != null) {
            callTarget.stub.possibleCallTargetTypes
        } else ObjJCallTargetUtil.getPossibleCallTargetTypes(callTarget.callTarget)
    }

    // ============================== //
    // ======= Return Types ========= //
    // ============================== //

    fun getReturnType(methodHeader: ObjJMethodHeader): String {
        return ObjJMethodPsiUtils.getReturnType(methodHeader, true)
    }

    fun getReturnType(methodHeader: ObjJSelectorLiteral): String {
        return ObjJMethodPsiUtils.getReturnType(methodHeader)
    }

    fun getReturnType(accessorProperty: ObjJAccessorProperty): String {
        return ObjJMethodPsiUtils.getReturnType(accessorProperty)
    }

    fun getCallTargetText(methodCall: ObjJMethodCall): String {
        return ObjJMethodCallPsiUtil.getCallTargetText(methodCall)
    }


    // ============================== //
    // ========= Selectors ========== //
    // ============================== //

    fun getGetter(property: ObjJAccessorProperty): String? {
        return ObjJAccessorPropertyPsiUtil.getGetter(property)
    }

    fun getSetter(property: ObjJAccessorProperty): String? {
        return ObjJAccessorPropertyPsiUtil.getSetter(property)
    }

    fun getSelectorString(methodHeader: ObjJMethodHeader): String {
        return ObjJMethodPsiUtils.getSelectorString(methodHeader)
    }

    fun getSelectorString(selector: ObjJSelector, addSuffix: Boolean): String {
        return ObjJMethodPsiUtils.getSelectorString(selector, addSuffix)
    }

    fun getSelectorString(selector: ObjJMethodDeclarationSelector, addSuffix: Boolean): String {
        return ObjJMethodPsiUtils.getSelectorString(selector, addSuffix)
    }

    fun getSelectorString(accessorProperty: ObjJAccessorProperty): String {
        return ObjJAccessorPropertyPsiUtil.getSelectorString(accessorProperty)
    }

    fun getSelectorString(methodCall: ObjJMethodCall): String {
        return ObjJMethodCallPsiUtil.getSelectorString(methodCall)
    }

    fun getSelectorString(selectorLiteral: ObjJSelectorLiteral): String {
        return ObjJMethodPsiUtils.getSelectorString(selectorLiteral)
    }

    fun getSelectorStrings(methodCall: ObjJMethodCall): List<String> {
        return ObjJMethodCallPsiUtil.getSelectorStrings(methodCall)
    }

    fun getSelectorStrings(methodHeader: ObjJMethodHeader): List<String> {
        return ObjJMethodPsiUtils.getSelectorStrings(methodHeader)
    }

    fun getSelectorList(methodHeader: ObjJMethodHeader): List<ObjJSelector> {
        return ObjJMethodPsiUtils.getSelectorElementsFromMethodDeclarationSelectorList(methodHeader.methodDeclarationSelectorList)
    }

    fun getSelectorList(methodCall: ObjJMethodCall): List<ObjJSelector> {
        return ObjJMethodCallPsiUtil.getSelectorList(methodCall)
    }

    fun getSelectorStrings(selectorLiteral: ObjJSelectorLiteral): List<String> {
        return if (selectorLiteral.stub != null && !selectorLiteral.stub.selectorStrings.isEmpty()) {
            selectorLiteral.stub.selectorStrings
        } else ObjJMethodPsiUtils.getSelectorStringsFromSelectorList(selectorLiteral.selectorList)
    }

    fun getSelectorStrings(accessorProperty: ObjJAccessorProperty): List<String> {
        return ObjJAccessorPropertyPsiUtil.getSelectorStrings(accessorProperty)
    }

    fun getSelectorList(accessorProperty: ObjJAccessorProperty): List<ObjJSelector> {
        return ObjJAccessorPropertyPsiUtil.getSelectorList(accessorProperty)
    }

    fun findSelectorMatching(method: ObjJHasMethodSelector, selectorString: String): ObjJSelector? {
        return ObjJMethodPsiUtils.findSelectorMatching(method, selectorString)
    }

    fun getParamTypes(methodHeader: ObjJMethodHeader): List<ObjJFormalVariableType> {
        return ObjJMethodPsiUtils.getParamTypes(methodHeader.methodDeclarationSelectorList)
    }

    fun getParamTypesAsStrings(methodHeader: ObjJMethodHeader): List<String> {
        return ObjJMethodPsiUtils.getParamTypesAsString(methodHeader.methodDeclarationSelectorList)
    }

    fun getVarType(selector: ObjJMethodDeclarationSelector): ObjJFormalVariableType? {
        return ObjJMethodPsiUtils.getVarType(selector)
    }

    fun getMethodHeaders(protocolDeclaration: ObjJProtocolDeclaration): List<ObjJMethodHeader> {
        return ObjJClassDeclarationPsiUtil.getMethodHeaders(protocolDeclaration)
    }

    fun hasMethod(classDeclaration: ObjJProtocolDeclaration, selector: String): Boolean {
        return ObjJClassDeclarationPsiUtil.hasMethod(classDeclaration, selector)
    }

    fun getMethodHeaders(implementationDeclaration: ObjJImplementationDeclaration): List<ObjJMethodHeader> {
        return ObjJClassDeclarationPsiUtil.getMethodHeaders(implementationDeclaration)
    }

    fun hasMethod(classDeclaration: ObjJImplementationDeclaration, selector: String): Boolean {
        return ObjJClassDeclarationPsiUtil.hasMethod(classDeclaration, selector)
    }

    // ============================== //
    // ====== Virtual Methods ======= //
    // ============================== //


    fun getAccessorPropertyMethods(variableName: String, varType: String, property: ObjJAccessorProperty): List<String> {
        return ObjJAccessorPropertyPsiUtil.getAccessorPropertyMethods(variableName, varType, property)
    }

    fun getGetter(declaration: ObjJInstanceVariableDeclaration): ObjJMethodHeaderStub? {
        return ObjJAccessorPropertyPsiUtil.getGetter(declaration)
    }

    fun getGetterSelector(variableName: String, varType: String, property: ObjJAccessorProperty): String? {
        return ObjJAccessorPropertyPsiUtil.getGetterSelector(variableName, varType, property)
    }

    fun isGetter(accessorProperty: ObjJAccessorProperty): Boolean {
        return ObjJAccessorPropertyPsiUtil.isGetter(accessorProperty)
    }

    fun getSetter(declaration: ObjJInstanceVariableDeclaration): ObjJMethodHeaderStub? {
        return ObjJAccessorPropertyPsiUtil.getSetter(declaration)
    }


    fun getSetterSelector(variableName: String, varType: String, property: ObjJAccessorProperty): String? {
        return ObjJAccessorPropertyPsiUtil.getSetterSelector(variableName, varType, property)
    }

    fun getSelectorUntil(targetSelectorElement: ObjJSelector, include: Boolean): String? {
        return ObjJMethodPsiUtils.getSelectorUntil(targetSelectorElement, include)
    }

    fun getThisOrPreviousNonNullSelector(hasMethodSelector: ObjJHasMethodSelector?, subSelector: String?, selectorIndex: Int): ObjJSelector? {
        if (hasMethodSelector == null) {
            return null
        }
        //LOGGER.log(Level.INFO, "Getting thisOrPreviousNonNullSelector: from element of type: <"+hasMethodSelector.getNode().getElementType().toString() + "> with text: <"+hasMethodSelector.getText()+"> ");//declared in <" + getFileName(hasMethodSelector)+">");
        val selectorList = hasMethodSelector.selectorList
        //LOGGER.log(Level.INFO, "Got selector list.");
        if (selectorList.isEmpty()) {
            //LOGGER.log(Level.WARNING, "Cannot get this or previous non null selector when selector list is empty");
            return null
        }
        var thisSelectorIndex: Int
        if (selectorIndex < 0 || selectorIndex >= selectorList.size) {
            thisSelectorIndex = selectorList.size - 1
        } else {
            thisSelectorIndex = selectorIndex
        }
        var selector: ObjJSelector? = selectorList[thisSelectorIndex]
        while ((selector == null || selector.getSelectorString(false).isEmpty()) && thisSelectorIndex > 0) {
            selector = selectorList[--thisSelectorIndex]
        }
        if (selector != null) {
            return selector
        }
        val subSelectorPattern = if (subSelector != null) Pattern.compile(subSelector.replace(ObjJMethodCallCompletionContributorUtil.CARET_INDICATOR, "(.*)")) else null
        for (currentSelector in selectorList) {
            if (currentSelector != null && (subSelectorPattern == null || subSelectorPattern.matcher(currentSelector.getSelectorString(false)).matches())) {
                return currentSelector
            }
        }
        //LOGGER.log(Level.WARNING, "Failed to find selector matching <"+subSelector+"> or any selector before out of <"+selectorList.size()+"> selectors");
        return null
    }

    // ============================== //
    // ======== References ========== //
    // ============================== //

    fun getReference(hasMethodSelector: ObjJHasMethodSelector): PsiReference {
        return ObjJMethodCallReferenceProvider(hasMethodSelector)
    }

    fun getReference(selectorLiteral: ObjJSelectorLiteral): PsiReference {
        return ObjJMethodCallReferenceProvider(selectorLiteral)
    }

    fun getReference(selector: ObjJSelector): PsiReference {
        return ObjJSelectorReference(selector)
    }

    fun getReferences(selector: ObjJSelector): Array<PsiReference> {
        //LOGGER.log(Level.INFO, "Getting references(plural) for selector");
        return ReferenceProvidersRegistry.getReferencesFromProviders(selector, PsiReferenceService.Hints.NO_HINTS)
    }

    fun getReference(className: ObjJClassName): PsiReference {
        return ObjJClassNameReference(className)
    }

    fun getReferences(reference: ObjJQualifiedReference): Array<PsiReference> {
        return PsiReference.EMPTY_ARRAY
    }

    fun getReference(variableName: ObjJVariableName): PsiReference {
        return ObjJVariableReference(variableName)
    }

    fun getReference(functionName: ObjJFunctionName): PsiReference {
        return ObjJFunctionNameReference(functionName)
    }

    fun getReferences(className: ObjJClassName): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(className, PsiReferenceService.Hints.NO_HINTS)
    }

    fun getSelectorLiteralReference(hasSelectorElement: ObjJHasMethodSelector): ObjJSelectorLiteral? {
        return ObjJMethodPsiUtils.getSelectorLiteralReference(hasSelectorElement)
    }

    // ============================== //
    // ======== Class Decs ========== //
    // ============================== //

    fun getAllClassNameElements(project: Project): List<ObjJClassName> {
        return ObjJClassDeclarationPsiUtil.getAllClassNameElements(project)
    }

    fun isCategory(implementationDeclaration: ObjJImplementationDeclaration): Boolean {
        return ObjJClassDeclarationPsiUtil.isCategory(implementationDeclaration)
    }

    fun getContainingClass(element: PsiElement): ObjJClassDeclarationElement<*>? {
        return ObjJHasContainingClassPsiUtil.getContainingClass(element)
    }

    fun getContainingClassName(methodHeader: ObjJMethodHeader): String {
        return ObjJHasContainingClassPsiUtil.getContainingClassName(methodHeader)
    }

    fun getContainingClassName(compositeElement: ObjJCompositeElement): String {
        return ObjJHasContainingClassPsiUtil.getContainingClassName(compositeElement)
    }

    fun getContainingClassName(classDeclarationElement: ObjJClassDeclarationElement<*>?): String {
        return ObjJHasContainingClassPsiUtil.getContainingClassName(classDeclarationElement)
    }

    fun getContainingClassName(selectorLiteral: ObjJSelectorLiteral?): String {
        return ObjJHasContainingClassPsiUtil.getContainingClassName(selectorLiteral)
    }

    fun getAllInheritedClasses(className: String, project: Project): List<String> {
        return ObjJInheritanceUtil.getAllInheritedClasses(className, project)
    }

    fun getContainingSuperClassName(element: ObjJCompositeElement): String? {
        return ObjJHasContainingClassPsiUtil.getContainingSuperClassName(element)
    }

    fun getSuperClassName(implementationDeclaration: ObjJImplementationDeclaration): String? {
        return ObjJClassDeclarationPsiUtil.getSuperClassName(implementationDeclaration)
    }

    fun hasContainingClass(hasContainingClass: ObjJHasContainingClass, className: String?): Boolean {
        return className != null && hasContainingClass.containingClassName == className
    }

    // ============================== //
    // ========= Var Types ========== //
    // ============================== //


    fun getVarType(accessorProperty: ObjJAccessorProperty): String? {
        return ObjJAccessorPropertyPsiUtil.getVarType(accessorProperty)
    }

    fun getInstanceVarDeclarationFromDeclarations(instanceVariableDeclarations: List<ObjJInstanceVariableDeclaration>, variableName: String): ObjJVariableName? {
        return ObjJVariablePsiUtil.getInstanceVarDeclarationFromDeclarations(instanceVariableDeclarations, variableName)
    }

    fun getLastVar(qualifiedReference: ObjJQualifiedReference): ObjJVariableName? {
        return ObjJVariablePsiUtil.getLastVar(qualifiedReference)
    }

    fun getIdType(varTypeId: ObjJVarTypeId): String {
        return ObjJMethodPsiUtils.getIdReturnType(varTypeId)
    }

    fun getIdType(varTypeId: ObjJVarTypeId, follow: Boolean): String {
        return ObjJMethodPsiUtils.getIdReturnType(varTypeId, follow)
    }

    // ============================== //
    // =========== Blocks =========== //
    // ============================== //

    fun getBlock(expr: ObjJExpr): ObjJBlock? {
        return ObjJBlockPsiUtil.getBlock(expr)
    }

    fun getBlockList(expr: ObjJTryStatement): List<ObjJBlock> {
        return ObjJBlockPsiUtil.getTryStatementBlockList(expr)
    }

    fun getBlockList(element: ObjJCompositeElement): List<ObjJBlock> {
        return ObjJTreeUtil.getChildrenOfTypeAsList(element, ObjJBlock::class.java)
    }

    fun getBlockList(element: ObjJCaseClause): List<ObjJBlock> {
        return listOf<ObjJBlock>(element.block)
    }

    fun getBlock(function: ObjJPreprocessorDefineFunction): ObjJBlock? {
        return if (function.preprocessorDefineBody != null) function.preprocessorDefineBody!!.block else null
    }

    fun getOpenBrace(ifStatement: ObjJPreprocessorIfStatement): ObjJBlock? {
        return null
    }

    // ============================== //
    // ========== Function ========== //
    // ============================== //

    fun getName(functionDeclaration: ObjJFunctionDeclaration): String {
        return ObjJFunctionDeclarationPsiUtil.getName(functionDeclaration)
    }

    /**
     * Renames function
     * @param functionDeclaration function to rename
     * @param name new function name
     * @return new function name
     * @throws IncorrectOperationException exception
     */
    @Throws(IncorrectOperationException::class)
    fun setName(functionDeclaration: ObjJFunctionDeclaration, name: String): ObjJFunctionName {
        return ObjJFunctionDeclarationPsiUtil.setName(functionDeclaration, name)
    }

    /**
     * Renames function literal node.
     * @param functionLiteral the literal to rename
     * @param name the new name
     * @return this function literal
     * @throws IncorrectOperationException exception
     */
    @Throws(IncorrectOperationException::class)
    fun setName(functionLiteral: ObjJFunctionLiteral, name: String): ObjJFunctionLiteral {
        return ObjJFunctionDeclarationPsiUtil.setName(functionLiteral, name)
    }


    fun getFunctionNameNode(functionLiteral: ObjJFunctionLiteral): ObjJNamedElement? {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameNode(functionLiteral)
    }

    fun getFunctionNameNode(functionDec: ObjJPreprocessorDefineFunction): ObjJNamedElement? {
        return functionDec.functionName
    }

    fun getQualifiedNameText(functionCall: ObjJFunctionCall): String {
        return ObjJFunctionDeclarationPsiUtil.getQualifiedNameText(functionCall)
    }

    fun getFunctionNameAsString(functionLiteral: ObjJFunctionLiteral): String {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameAsString(functionLiteral)
    }

    fun getFunctionNamesAsString(functionLiteral: ObjJFunctionLiteral): List<String> {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNamesAsString(functionLiteral)
    }

    fun getFunctionNameAsString(functionDeclaration: ObjJFunctionDeclaration): String {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameAsString(functionDeclaration)
    }

    fun getFunctionNameAsString(functionDeclaration: ObjJPreprocessorDefineFunction): String {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameAsString(functionDeclaration)
    }

    fun getParamNameElements(functionDeclaration: ObjJFunctionDeclarationElement<*>): List<ObjJVariableName> {
        return ObjJFunctionDeclarationPsiUtil.getParamNameElements(functionDeclaration)
    }

    fun getParamNames(functionDeclaration: ObjJFunctionDeclarationElement<*>): List<String> {
        return ObjJFunctionDeclarationPsiUtil.getParamNames(functionDeclaration)
    }

    fun getReturnType(functionDeclaration: ObjJFunctionDeclaration): String {
        return ObjJFunctionDeclarationPsiUtil.getReturnType(functionDeclaration)
    }

    fun getReturnType(functionLiteral: ObjJFunctionLiteral): String {
        return ObjJFunctionDeclarationPsiUtil.getReturnType(functionLiteral)
    }

    fun getReturnType(functionDefinition: ObjJPreprocessorDefineFunction): String {
        return ObjJFunctionDeclarationPsiUtil.getReturnType(functionDefinition)
    }


    // ============================== //
    // ===== QualifiedReference ===== //
    // ============================== //

    fun getPartsAsString(qualifiedReference: ObjJQualifiedReference): String {
        return (if (qualifiedReference.methodCall != null) "{?}" else "") + getPartsAsString(ObjJTreeUtil.getChildrenOfTypeAsList(qualifiedReference, ObjJQualifiedNamePart::class.java))
    }

    fun getPartsAsStringArray(qualifiedReference: ObjJQualifiedReference): List<String> {
        return getPartsAsStringArray(ObjJTreeUtil.getChildrenOfTypeAsList(qualifiedReference, ObjJQualifiedNamePart::class.java))
    }

    fun getPartsAsStringArray(qualifiedNameParts: List<ObjJQualifiedNamePart>?): List<String> {
        if (qualifiedNameParts == null) {
            return emptyList()
        }
        val out = ArrayList<String>()
        for (part in qualifiedNameParts) {
            out.add(if (part.qualifiedNameText != null) part.qualifiedNameText else "")
        }
        return out
    }

    fun getPartsAsString(qualifiedNameParts: List<ObjJQualifiedNamePart>): String {
        return ArrayUtils.join(getPartsAsStringArray(qualifiedNameParts), ".")
    }

    fun getQualifiedNameText(variableName: ObjJVariableName): String {
        return variableName.text
    }

    fun toString(variableName: ObjJVariableName): String {
        return ObjJVariablePsiUtil.toString(variableName)
    }

    fun getDescriptiveText(psiElement: PsiElement): String? {
        if (psiElement is ObjJSelector) {
            return getSelectorDescriptiveName(psiElement)
        } else if (psiElement is ObjJVariableName) {
            return psiElement.text
        } else if (psiElement is ObjJClassName) {
            return getClassDescriptiveText(psiElement)
        } else if (psiElement is ObjJFunctionName) {
            return psiElement.getText()
        }
        return ""
    }

    private fun getClassDescriptiveText(classNameElement: ObjJClassName): String? {
        val classDeclarationElement = classNameElement.getParentOfType(ObjJClassDeclarationElement<*>::class.java)
        var className = classNameElement.text
        if (classDeclarationElement == null || classDeclarationElement.classNameString != className) {
            return className
        }
        if (classDeclarationElement is ObjJImplementationDeclaration) {
            val implementationDeclaration = classDeclarationElement as ObjJImplementationDeclaration?
            if (implementationDeclaration.categoryName != null) {
                className += " (" + implementationDeclaration.categoryName!!.className.text + ")"
            }
        }
        return className
    }

    fun getSelectorDescriptiveName(selector: ObjJSelector): String {
        val selectorLiteral = ObjJTreeUtil.getParentOfType(selector, ObjJSelectorLiteral::class.java)
        if (selectorLiteral != null) {
            return "@selector(" + selectorLiteral.selectorString + ")"
        }
        val variableDeclaration = ObjJTreeUtil.getParentOfType(selector, ObjJInstanceVariableDeclaration::class.java)
        if (variableDeclaration != null) {
            val className = variableDeclaration.containingClassName
            val property = ObjJTreeUtil.getParentOfType(selector, ObjJAccessorProperty::class.java)
            val propertyString = if (property != null) property.accessorPropertyType.text + "=" else ""
            val returnType = if (variableDeclaration.stub != null) variableDeclaration.stub.varType else variableDeclaration.formalVariableType.text
            return "- (" + returnType + ") @accessors(" + propertyString + selector.getSelectorString(false) + ")"
        }
        val methodCall = ObjJTreeUtil.getParentOfType(selector, ObjJMethodCall::class.java)
        var selectorString: String? = null
        val className: String? = null
        if (methodCall != null) {
            selectorString = methodCall.selectorString
        }
        if (selectorString == null) {
            val methodHeader = ObjJTreeUtil.getParentOfType(selector, ObjJMethodHeaderDeclaration<*>::class.java)
            if (methodHeader != null) {
                selectorString = if (methodHeader is ObjJMethodHeader) getFormattedSelector((methodHeader as ObjJMethodHeader?)!!) else methodHeader.selectorString
                val methodScopeString = if (methodHeader.isStatic) "+" else "-"
                return methodScopeString + " (" + methodHeader.returnType + ")" + selectorString
            }
        }
        selectorString = if (selectorString != null) selectorString else selector.getSelectorString(true)
        return "[* $selectorString]"
    }

    private fun getFormattedSelector(methodHeader: ObjJMethodHeader): String {
        val builder = StringBuilder()
        for (selector in methodHeader.methodDeclarationSelectorList) {
            if (selector.selector != null) {
                builder.append(selector.selector!!.getSelectorString(false))
            }
            builder.append(ObjJMethodPsiUtils.SELECTOR_SYMBOL)
            if (selector.formalVariableType != null) {
                builder.append("(").append(selector.formalVariableType!!.text).append(")")
            }
            if (selector.variableName != null) {
                builder.append(selector.variableName!!.text)
            }
            builder.append(" ")
        }
        return builder.substring(0, builder.length - 1)
    }

    // ============================== //
    // ========== Imports =========== //
    // ============================== //

    fun getFileName(reference: ObjJFrameworkReference): String {
        return ObjJImportPsiUtils.getFileName(reference)
    }

    fun getFileName(framework: ObjJImportFramework): String {
        return ObjJImportPsiUtils.getFileName(framework)
    }

    fun getFileName(framework: ObjJIncludeFramework): String {
        return ObjJImportPsiUtils.getFileName(framework)
    }

    fun getFileName(framework: ObjJImportFile): String {
        return ObjJImportPsiUtils.getFileName(framework)
    }

    fun getFileName(framework: ObjJIncludeFile): String {
        return ObjJImportPsiUtils.getFileName(framework)
    }

    fun getFrameworkName(reference: ObjJFrameworkReference): String? {
        return ObjJImportPsiUtils.getFrameworkName(reference)
    }


    fun getFrameworkName(framework: ObjJImportFramework): String? {
        return ObjJImportPsiUtils.getFrameworkName(framework)
    }

    fun getFrameworkName(framework: ObjJIncludeFile): String? {
        return ObjJImportPsiUtils.getFrameworkName(framework)
    }

    fun getFrameworkName(framework: ObjJImportFile): String? {
        return ObjJImportPsiUtils.getFrameworkName(framework)
    }

    fun getFrameworkName(framework: ObjJIncludeFramework): String? {
        return ObjJImportPsiUtils.getFrameworkName(framework)
    }

    // ============================== //
    // ===== Global Variables ======= //
    // ============================== //
    fun getFileName(declaration: ObjJGlobalVariableDeclaration): String? {
        return ObjJVariablePsiUtil.getFileName(declaration)
    }

    fun getVariableNameString(declaration: ObjJGlobalVariableDeclaration): String {
        return ObjJVariablePsiUtil.getVariableNameString(declaration)
    }

    fun getVariableType(declaration: ObjJGlobalVariableDeclaration): String? {
        return ObjJVariablePsiUtil.getVariableType(declaration)
    }

    // ============================== //
    // ===== VariableAssignments ==== //
    // ============================== //

    fun getAssignedValue(assignmentLogical: ObjJVariableAssignmentLogical): ObjJExpr {
        return ObjJVariableAssignmentsPsiUtil.getAssignedValue(assignmentLogical)
    }

    fun getQualifiedReferenceList(assignmentLogical: ObjJVariableAssignmentLogical): List<ObjJQualifiedReference> {
        return listOf<ObjJQualifiedReference>(assignmentLogical.qualifiedReference)
    }

    // ============================== //
    // ====== Iterator Elements ===== //
    // ============================== //

    fun getConditionalExpression(doWhileStatement: ObjJDoWhileStatement?): ObjJExpr? {
        return if (doWhileStatement == null || doWhileStatement.conditionExpression == null) {
            null
        } else doWhileStatement.conditionExpression!!.expr
    }

    // ============================== //
    // =========== Misc ============= //
    // ============================== //

    fun getFileName(element: PsiElement?): String? {
        if (element == null) {
            return null
        }
        return if (element.containingFile == null || element.containingFile.virtualFile == null) {
            null
        } else element.containingFile.virtualFile.name
    }

    // ============================== //
    // ====== Should Resolve ======== //
    // ============================== //

    fun shouldResolve(psiElement: PsiElement?): Boolean {
        return ObjJResolveableElementUtil.shouldResolve(psiElement)
    }

    fun shouldResolve(psiElement: ObjJClassDeclarationElement<*>?): Boolean {
        return ObjJResolveableElementUtil.shouldResolve(psiElement)
    }

    fun shouldResolve(psiElement: PsiElement?, shouldNotResolveLoggingStatement: String?): Boolean {
        return ObjJResolveableElementUtil.shouldResolve(psiElement, shouldNotResolveLoggingStatement)
    }

    fun shouldResolve(hasContainingClass: ObjJHasContainingClass?): Boolean {
        return ObjJResolveableElementUtil.shouldResolve(hasContainingClass as PsiElement?) && shouldResolve(hasContainingClass!!.containingClass)
    }

    // ============================== //
    // =========== PARSER =========== //
    // ============================== //

    fun eos(compositeElement: PsiElement?): Boolean {
        if (compositeElement == null) {
            return false
        }
        var ahead = ObjJTreeUtil.getNextNode(compositeElement)
        if (ahead == null && compositeElement.parent != null) {
            return eos(compositeElement.parent)
        }
        var hadLineTerminator = false
        while (ahead != null && (ahead.elementType === com.intellij.psi.TokenType.WHITE_SPACE || ahead.elementType === ObjJTypes.ObjJ_LINE_TERMINATOR)) {
            if (ahead === ObjJTypes.ObjJ_LINE_TERMINATOR) {
                hadLineTerminator = true
            }
            while (ahead!!.treeNext == null && ahead.treeParent != null) {
                ahead = ahead.treeParent
            }
            ahead = ahead.treeNext
        }
        return ahead != null && eosToken(ahead.elementType, hadLineTerminator)
    }

    fun eosToken(ahead: IElementType?, hadLineTerminator: Boolean): Boolean {

        if (ahead == null) {
            LOGGER.log(Level.INFO, "EOS assumed as ahead == null")
            return true
        }

        // Check if the token is, or contains a line terminator.
        //LOGGER.log(Level.INFO, String.format("LineTerminatorAheadToken: <%s>; CurrentToken <%s> Is Line Terminator?:  <%b>", ahead, builder_.getTokenText(), isLineTerminator));
        var isLineTerminator = ahead === ObjJTypes.ObjJ_BLOCK_COMMENT ||
                ahead === ObjJTypes.ObjJ_SINGLE_LINE_COMMENT ||
                ahead === ObjJTypes.ObjJ_ELSE ||
                ahead === ObjJTypes.ObjJ_IF ||
                ahead === ObjJTypes.ObjJ_CLOSE_BRACE ||
                ahead === ObjJTypes.ObjJ_WHILE ||
                ahead === ObjJTypes.ObjJ_DO ||
                ahead === ObjJTypes.ObjJ_PP_PRAGMA ||
                ahead === ObjJTypes.ObjJ_PP_IF ||
                ahead === ObjJTypes.ObjJ_PP_ELSE ||
                ahead === ObjJTypes.ObjJ_PP_ELSE_IF ||
                ahead === ObjJTypes.ObjJ_PP_END_IF ||
                ahead === ObjJTypes.ObjJ_SEMI_COLON
        if (isLineTerminator || !ObjJPluginSettings.inferEOS()) {
            if (!isLineTerminator) {
                //LOGGER.log(Level.INFO, "Failed EOS check. Ahead token is <"+ahead.toString()+">");
            }
            return isLineTerminator
        }
        isLineTerminator = hadLineTerminator && (ahead === ObjJTypes.ObjJ_BREAK ||
                ahead === ObjJTypes.ObjJ_VAR ||
                ahead === ObjJTypes.ObjJ_AT_IMPLEMENTATION ||
                ahead === ObjJTypes.ObjJ_AT_IMPORT ||
                ahead === ObjJTypes.ObjJ_AT_GLOBAL ||
                ahead === ObjJTypes.ObjJ_TYPE_DEF ||
                ahead === ObjJTypes.ObjJ_FUNCTION ||
                ahead === ObjJTypes.ObjJ_AT_PROTOCOL ||
                ahead === ObjJTypes.ObjJ_CONTINUE ||
                ahead === ObjJTypes.ObjJ_CONST ||
                ahead === ObjJTypes.ObjJ_RETURN ||
                ahead === ObjJTypes.ObjJ_SWITCH ||
                ahead === ObjJTypes.ObjJ_LET ||
                ahead === ObjJTypes.ObjJ_CASE)
        return isLineTerminator
    }

    fun hasNodeType(element: PsiElement?, elementType: IElementType): Boolean {
        return element != null && element.node.elementType === elementType
    }

    // ============================== //
    // ======= Presentation ========= //
    // ============================== //

    fun getPresentation(implementationDeclaration: ObjJImplementationDeclaration): ItemPresentation {
        return ObjJClassDeclarationPsiUtil.getPresentation(implementationDeclaration)
    }

    fun getPresentation(protocolDeclaration: ObjJProtocolDeclaration): ItemPresentation {
        return ObjJClassDeclarationPsiUtil.getPresentation(protocolDeclaration)
    }

    fun getIcon(element: PsiElement): Icon? {
        if (element is ObjJClassName) {
            val classDeclarationElement = element.getParentOfType(ObjJClassDeclarationElement<*>::class.java)

            val className = element.getText()
            if (classDeclarationElement == null || classDeclarationElement.classNameString != className) {
                return null
            }
            if (classDeclarationElement is ObjJImplementationDeclaration) {
                val implementationDeclaration = element.getParent() as ObjJImplementationDeclaration
                return if (implementationDeclaration.isCategory) {
                    ObjJIcons.CATEGORY_ICON
                } else {
                    ObjJIcons.CLASS_ICON
                }
            } else if (classDeclarationElement is ObjJProtocolDeclaration) {
                return ObjJIcons.PROTOCOL_ICON
            }
            return null
        }

        if (element is ObjJFunctionName) {
            return ObjJIcons.FUNCTION_ICON
        }

        if (element is ObjJVariableName) {
            return ObjJIcons.VARIABLE_ICON
        }

        if (element is ObjJSelector) {
            if (isIn(element, ObjJMethodHeaderDeclaration<*>::class.java)) {
                return ObjJIcons.METHOD_ICON
            }
            if (isIn(element, ObjJInstanceVariableDeclaration::class.java)) {
                return ObjJIcons.ACCESSOR_ICON
            }
            if (isIn(element, ObjJSelectorLiteral::class.java)) {
                return ObjJIcons.SELECTOR_ICON
            }
        }
        return null
    }

    fun <PsiT : PsiElement> isIn(element: PsiElement, parentClass: Class<PsiT>): Boolean {
        return ObjJTreeUtil.getParentOfType(element, parentClass) != null
    }

}
