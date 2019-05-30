package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.hints.description
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.structure.ObjJCodeFoldingBuilder
import cappuccino.ide.intellij.plugin.structure.ObjJStructureViewElement
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.references.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassTypeName
import cappuccino.ide.intellij.plugin.references.presentation.ObjJSelectorItemPresentation
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.psi.tree.TokenSet

import javax.swing.*
import java.util.*

@Suppress("UNUSED_PARAMETER")
object ObjJPsiImplUtil {

    //private val LOGGER = Logger.getLogger(ObjJPsiImplUtil::class.java.name)

    // ============================== //
    // ======= Named Ident ========== //
    // ============================== //
    @JvmStatic
    fun hasText(variableName: ObjJVariableName, text: String): Boolean {
        return ObjJNamedPsiUtil.hasText(variableName, text)
    }

    @JvmStatic
    fun getName(variableName: ObjJVariableName): String {
        return ObjJNamedPsiUtil.getName(variableName)
    }

    @JvmStatic
    fun getName(selector: ObjJSelector): String {
        return ObjJMethodPsiUtils.getName(selector)
    }

    @JvmStatic
    fun getName(defineFunction: ObjJPreprocessorDefineFunction): String {
        return defineFunction.functionNameAsString
    }

    @JvmStatic
    fun getName(functionName: ObjJFunctionName): String {
        return functionName.text
    }

    @JvmStatic
    fun getName(methodHeader: ObjJMethodHeader): String {
        return ObjJMethodPsiUtils.getName(methodHeader)
    }

    @JvmStatic
    fun getName(variableDeclaration: ObjJInstanceVariableDeclaration): String {
        return if (variableDeclaration.variableName != null) variableDeclaration.variableName!!.text else ""
    }

    @JvmStatic
    fun getNameIdentifier(selector: ObjJSelector): PsiElement {
        return ObjJMethodPsiUtils.getNameIdentifier(selector)
    }

    @JvmStatic
    fun getQualifiedNameParts(qualifiedReference: ObjJQualifiedReference): List<ObjJQualifiedReferenceComponent> {
        return ObjJQualifiedReferenceUtil.getQualifiedNameParts(qualifiedReference)
    }

    @JvmStatic
    fun getQualifiedNameParts(qualifiedReference: ObjJQualifiedReferencePrime): List<ObjJQualifiedReferenceComponent> {
        return ObjJQualifiedReferenceUtil.getQualifiedNameParts(qualifiedReference)
    }

    @JvmStatic
    fun getRangeInElement(selector: ObjJSelector): TextRange {
        return ObjJMethodPsiUtils.getRangeInElement(selector)
    }

    @JvmStatic
    fun getRangeInElement(variableName: ObjJVariableName): TextRange {
        return TextRange(0, variableName.textLength)
    }

    @JvmStatic
    fun setName(defineFunction: ObjJPreprocessorDefineFunction, name: String): PsiElement {
        return ObjJFunctionDeclarationPsiUtil.setName(defineFunction, name)
    }

    @JvmStatic
    fun setName(selectorElement: ObjJSelector, newSelectorValue: String): PsiElement {
        return ObjJMethodPsiUtils.setName(selectorElement, newSelectorValue)
    }

    @JvmStatic
    fun setName(className: ObjJClassName, newClassName: String?): PsiElement {
        return ObjJNamedPsiUtil.setName(className, newClassName)
    }

    @JvmStatic
    fun setName(variableName: ObjJVariableName, newName: String): PsiElement {
        return ObjJNamedPsiUtil.setName(variableName, newName)
    }

    @JvmStatic
    @Throws(IncorrectOperationException::class)
    fun setName(header: ObjJHasMethodSelector, name: String): PsiElement {
        return ObjJMethodPsiUtils.setName(header, name)
    }

    @JvmStatic
    fun setName(instanceVariableDeclaration: ObjJInstanceVariableDeclaration, newName: String): PsiElement {
        return ObjJNamedPsiUtil.setName(instanceVariableDeclaration, newName)
    }

    @JvmStatic
    fun setName(oldFunctionName: ObjJFunctionName, newFunctionName: String): ObjJFunctionName {
        if (newFunctionName.isEmpty()) {
            return oldFunctionName
        }
        val functionName = ObjJElementFactory.createFunctionName(oldFunctionName.project, newFunctionName)
                ?: return oldFunctionName
        oldFunctionName.parent.node.replaceChild(oldFunctionName.node, functionName.node)
        return functionName
    }

    @JvmStatic
    fun getClassType(classDeclaration: ObjJClassDeclarationElement<*>): ObjJClassTypeName {
        val classNameString = classDeclaration.getClassNameString()
        return if (!isUniversalMethodCaller(classNameString)) ObjJClassType.getClassType(classNameString) else ObjJClassType.UNDEF
    }

    @JvmStatic
    fun getClassNameString(classElement: ObjJImplementationDeclaration): String {
        if (classElement.stub != null) {
            return classElement.stub?.className ?: ""
        }
        return classElement.getClassName()?.text ?: ObjJClassType.UNDEF_CLASS_NAME
    }

    @JvmStatic
    fun getClassNameString(classElement: ObjJProtocolDeclaration): String {
        if (classElement.stub != null) {
            return classElement.stub.className
        }
        return classElement.getClassName()?.text ?: ObjJClassType.UNDEF_CLASS_NAME
    }

    @JvmStatic
    fun getClassNameString(typedef: ObjJTypeDef): String {
        return typedef.className.text ?: ""
    }

    @JvmStatic
    fun getSuperClassName(declaration: ObjJImplementationDeclaration): String? =
            cappuccino.ide.intellij.plugin.psi.utils.getSuperClassName(declaration)

    @JvmStatic
    fun getCategoryNameString(declaration: ObjJImplementationDeclaration): String? {
        return declaration.stub?.categoryName ?: declaration.categoryName?.className?.text
    }

    @JvmStatic
    fun getContainingSuperClass(hasContainingClass: ObjJHasContainingClass, returnDefault: Boolean = false): ObjJClassName? =
            cappuccino.ide.intellij.plugin.psi.utils.getContainingSuperClass(hasContainingClass, returnDefault)

    @JvmStatic
    fun isCategory(declaration: ObjJImplementationDeclaration): Boolean =
            cappuccino.ide.intellij.plugin.psi.utils.isCategory(declaration)

    // ============================== //
    // ====== Navigation Items ====== //
    // ============================== //

    @JvmStatic
    fun getPresentation(selector: ObjJSelector): ItemPresentation? = ObjJSelectorItemPresentation(selector)

    @JvmStatic
    fun getPresentation(classDec: ObjJProtocolDeclaration): ItemPresentation? =
            cappuccino.ide.intellij.plugin.psi.utils.getPresentation(classDec)

    @JvmStatic
    fun getPresentation(classDec: ObjJImplementationDeclaration): ItemPresentation? =
            cappuccino.ide.intellij.plugin.psi.utils.getPresentation(classDec)

    @JvmStatic
    fun getPresentation(className: ObjJClassName): ItemPresentation {
        return cappuccino.ide.intellij.plugin.psi.utils.getPresentation(className)
    }

    // ============================== //
    // =========== String =========== //
    // ============================== //

    @JvmStatic
    fun getStringValue(stringLiteral: ObjJStringLiteral): String {
        val rawText = stringLiteral.text
        val quotationMark: String = if (rawText.startsWith("\"")) "\"" else if (rawText.startsWith("'")) "'" else return rawText
        val outText = if (rawText.startsWith(quotationMark)) rawText.substring(1) else rawText
        return if (outText.endsWith(quotationMark)) outText.substring(0, outText.length - 1) else outText

    }

    @JvmStatic
    fun toString(variableName: ObjJVariableName): String {
        return ObjJVariablePsiUtil.toString(variableName)
    }

    // ============================== //
    // ====== MethodHeaders ========= //
    // ============================== //
    @JvmStatic
    fun getMethodHeaders(declaration: ObjJImplementationDeclaration): List<ObjJMethodHeader> =
            cappuccino.ide.intellij.plugin.psi.utils.getMethodHeaders(declaration)

    @JvmStatic
    fun getMethodHeaders(declaration: ObjJProtocolDeclaration): List<ObjJMethodHeader> =
            cappuccino.ide.intellij.plugin.psi.utils.getMethodHeaders(declaration)

    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun getMethodHeaders(_typedef: ObjJTypeDef): List<ObjJMethodHeader> =
            listOf()

    @JvmStatic
    fun hasMethod(declaration: ObjJImplementationDeclaration, selector: String): Boolean =
            cappuccino.ide.intellij.plugin.psi.utils.hasMethod(declaration, selector)

    @JvmStatic
    fun hasMethod(declaration: ObjJProtocolDeclaration, selector: String): Boolean =
            cappuccino.ide.intellij.plugin.psi.utils.hasMethod(declaration, selector)


    @JvmStatic
    fun getAllUnimplementedProtocolMethods(declaration: ObjJImplementationDeclaration): Map<ObjJClassName, ObjJProtocolDeclarationPsiUtil.ProtocolMethods> =
            cappuccino.ide.intellij.plugin.psi.utils.getAllUnimplementedProtocolMethods(declaration)

    @JvmStatic
    fun getUnimplementedProtocolMethods(declaration: ObjJImplementationDeclaration, protocolName: String): ObjJProtocolDeclarationPsiUtil.ProtocolMethods =
            cappuccino.ide.intellij.plugin.psi.utils.getUnimplementedProtocolMethods(declaration, protocolName)
    // ============================== //
    // ======= Method Misc ========== //
    // ============================== //

    @JvmStatic
    fun getMethodScope(methodHeader: ObjJMethodHeader): MethodScope {
        return ObjJMethodPsiUtils.getMethodScope(methodHeader)
    }

    @JvmStatic
    fun getMethodScope(accessorProperty: ObjJAccessorProperty): MethodScope {
        return ObjJMethodPsiUtils.getMethodScope(accessorProperty)
    }

    @JvmStatic
    fun getMethodScope(literal: ObjJSelectorLiteral): MethodScope {
        return ObjJMethodPsiUtils.getMethodScope(literal)
    }

    @JvmStatic
    fun isStatic(hasMethodSelector: ObjJHasMethodSelector): Boolean {
        return ObjJMethodPsiUtils.isStatic(hasMethodSelector)
    }

    // ============================== //
    // ======= Return Types ========= //
    // ============================== //

    @JvmStatic
    fun getReturnType(methodHeader: ObjJMethodHeader): String {
        return ObjJMethodPsiUtils.getReturnType(methodHeader, true)
    }

    @JvmStatic
    fun getReturnType(methodHeader: ObjJSelectorLiteral): String {
        return ObjJMethodPsiUtils.getReturnType(methodHeader)
    }

    @JvmStatic
    fun getReturnType(accessorProperty: ObjJAccessorProperty): String {
        return ObjJMethodPsiUtils.getReturnType(accessorProperty)
    }

    @JvmStatic
    fun getVariableType(globalVariableDeclaration: ObjJGlobalVariableDeclaration): String? {
        return ObjJVariablePsiUtil.getVariableType(globalVariableDeclaration)
    }

    // ============================== //
    // ======== Method Call ========= //
    // ============================== //
    @JvmStatic
    fun getCallTargetText(methodCall: ObjJMethodCall): String =
            cappuccino.ide.intellij.plugin.psi.utils.getCallTargetText(methodCall)

    @JvmStatic
    fun getPossibleCallTargetTypes(callTarget: ObjJCallTarget): List<String> {
        return getPossibleClassTypesForCallTarget(callTarget).toList()
    }

    @JvmStatic
    fun getPossibleCallTargetTypes(methodCall: ObjJMethodCall): List<String> {
        return Collections.singletonList(ObjJClassType.UNDETERMINED)// ObjJCallTargetUtil.getPossibleCallTargetTypes(methodCall)
    }

    // ============================== //
    // ========= Selectors ========== //
    // ============================== //

    @JvmStatic
    fun getSelectorString(methodHeader: ObjJMethodHeader): String {
        return cappuccino.ide.intellij.plugin.psi.utils.getSelectorString(methodHeader)
    }

    @JvmStatic
    fun getSelectorString(selector: ObjJSelector, addSuffix: Boolean): String {
        return cappuccino.ide.intellij.plugin.psi.utils.getSelectorString(selector, addSuffix)
    }

    @JvmStatic
    fun getSelectorString(selector: ObjJMethodDeclarationSelector, addSuffix: Boolean): String {
        return cappuccino.ide.intellij.plugin.psi.utils.getSelectorString(selector, addSuffix)
    }

    @JvmStatic
    fun getSelectorString(selectorLiteral: ObjJSelectorLiteral): String {
        return cappuccino.ide.intellij.plugin.psi.utils.getSelectorString(selectorLiteral)
    }

    @JvmStatic
    fun getSelectorString(property: ObjJAccessorProperty): String =
            ObjJAccessorPropertyPsiUtil.getSelectorString(property)

    @JvmStatic
    fun getSelectorString(methodCall: ObjJMethodCall): String =
            cappuccino.ide.intellij.plugin.psi.utils.getSelectorString(methodCall)

    @JvmStatic
    fun getSelectorStrings(methodHeader: ObjJMethodHeader): List<String> {
        return cappuccino.ide.intellij.plugin.psi.utils.getSelectorStrings(methodHeader)
    }

    @JvmStatic
    fun getSelectorStrings(methodCall: ObjJMethodCall): List<String> =
            cappuccino.ide.intellij.plugin.psi.utils.getSelectorStrings(methodCall)

    @JvmStatic
    fun getSelectorStrings(selectorLiteral: ObjJSelectorLiteral): List<String> {
        val selectors = selectorLiteral.stub?.selectorStrings ?: listOf()
        return if (selectors.isNotEmpty()) {
            selectors
        } else
            getSelectorStringsFromSelectorList(selectorLiteral.selectorList)
    }

    @JvmStatic
    fun getSelectorStrings(accessorProperty: ObjJAccessorProperty): List<String> =
            ObjJAccessorPropertyPsiUtil.getSelectorStrings(accessorProperty)


    @JvmStatic
    fun getSelectorList(methodCall: ObjJMethodCall): List<ObjJSelector?> =
            cappuccino.ide.intellij.plugin.psi.utils.getSelectorList(methodCall)

    @JvmStatic
    fun getSelectorList(methodHeader: ObjJMethodHeader): List<ObjJSelector?> =
            ObjJMethodPsiUtils.getSelectorList(methodHeader)

    @JvmStatic
    fun getSelectorList(accessorProperty: ObjJAccessorProperty): List<ObjJSelector> =
            ObjJAccessorPropertyPsiUtil.getSelectorList(accessorProperty)

    @JvmStatic
    fun getSelectorIndex(selector: ObjJSelector): Int =
            ObjJMethodPsiUtils.getSelectorIndex(selector)

    @JvmStatic
    fun getSelectorUntil(targetSelectorElement: ObjJSelector, include: Boolean): String? {
        return cappuccino.ide.intellij.plugin.psi.utils.getSelectorUntil(targetSelectorElement, include)
    }

    @JvmStatic
    fun getThisOrPreviousNonNullSelector(hasMethodSelector: ObjJHasMethodSelector?, subSelector: String?, selectorIndex: Int): ObjJSelector? =
            ObjJMethodPsiUtils.getThisOrPreviousNonNullSelector(hasMethodSelector, subSelector, selectorIndex)

    @JvmStatic
    fun findSelectorMatching(method: ObjJHasMethodSelector, selectorString: String): ObjJSelector? {
        return ObjJMethodPsiUtils.findSelectorMatching(method, selectorString)
    }

    @JvmStatic
    fun getParamTypes(methodHeader: ObjJMethodHeader): List<ObjJFormalVariableType?> {
        return ObjJMethodPsiUtils.getParamTypes(methodHeader.methodDeclarationSelectorList)
    }

    @JvmStatic
    fun getParamTypesAsStrings(methodHeader: ObjJMethodHeader): List<String> {
        return ObjJMethodPsiUtils.getParamTypesAsString(methodHeader.methodDeclarationSelectorList)
    }

    @JvmStatic
    fun getVarType(selector: ObjJMethodDeclarationSelector): ObjJFormalVariableType? {
        return ObjJMethodPsiUtils.getVarType(selector)
    }

    @JvmStatic
    fun getVarType(property: ObjJAccessorProperty): String? = ObjJAccessorPropertyPsiUtil.getVarType(property)

    @JvmStatic
    fun getFormalVariableType(selector: ObjJMethodDeclarationSelector): ObjJFormalVariableType? {
        return selector.methodHeaderSelectorFormalVariableType?.formalVariableType
    }

    @JvmStatic
    fun isRequired(methodHeader: ObjJMethodHeader): Boolean =
            ObjJMethodPsiUtils.isRequired(methodHeader)

    // ============================== //
    // ====== Virtual Methods ======= //
    // ============================== //

    @JvmStatic
    fun getGetter(property: ObjJAccessorProperty): String? =
            ObjJAccessorPropertyPsiUtil.getGetter(property)

    @JvmStatic
    fun getGetter(variableDeclaration: ObjJInstanceVariableDeclaration): ObjJMethodHeaderStub? =
            ObjJAccessorPropertyPsiUtil.getGetter(variableDeclaration)

    @JvmStatic
    fun isGetter(property: ObjJAccessorProperty): Boolean =
            ObjJAccessorPropertyPsiUtil.isGetter(property)

    @JvmStatic
    fun getSetter(property: ObjJAccessorProperty): String? =
            ObjJAccessorPropertyPsiUtil.getSetter(property)

    @JvmStatic
    fun getSetter(variableDeclaration: ObjJInstanceVariableDeclaration): ObjJMethodHeaderStub? =
            ObjJAccessorPropertyPsiUtil.getSetter(variableDeclaration)

    @JvmStatic
    fun getAccessorPropertyList(declaration: ObjJInstanceVariableDeclaration): List<ObjJAccessorProperty> {
        return ObjJAccessorPropertyPsiUtil.getAccessorPropertiesList(declaration)
    }

    // ============================== //
    // ======== References ========== //
    // ============================== //

    @JvmStatic
    fun getReference(hasMethodSelector: ObjJHasMethodSelector): PsiReference {
        return ObjJMethodCallReferenceProvider(hasMethodSelector)
    }

    @JvmStatic
    fun getReference(selectorLiteral: ObjJSelectorLiteral): PsiReference {
        return ObjJMethodCallReferenceProvider(selectorLiteral)
    }

    @JvmStatic
    fun getReference(selector: ObjJSelector): PsiReference {
        return ObjJSelectorReference(selector)
    }

    @JvmStatic
    fun getReferences(selector: ObjJSelector): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(selector, PsiReferenceService.Hints.NO_HINTS)
    }

    @JvmStatic
    fun getReference(className: ObjJClassName): PsiReference {
        return ObjJClassNameReference(className)
    }

    @JvmStatic
    fun getReference(variableName: ObjJVariableName): PsiReference {
        return ObjJVariableReference(variableName)
    }

    @JvmStatic
    fun getReference(functionName: ObjJFunctionName): PsiReference {
        return ObjJFunctionNameReference(functionName)
    }

    @JvmStatic
    fun getReferences(className: ObjJClassName): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(className, PsiReferenceService.Hints.NO_HINTS)
    }

    @JvmStatic
    fun getSelectorLiteralReference(hasSelectorElement: ObjJHasMethodSelector): ObjJSelectorLiteral? {
        return ObjJMethodPsiUtils.getSelectorLiteralReference(hasSelectorElement)
    }

    // ============================== //
    // ======== Class Decs ========== //
    // ============================== //
    @JvmStatic
    fun getContainingSuperClass(psiElement: ObjJCompositeElement, returnDefault: Boolean): ObjJClassName? =
            cappuccino.ide.intellij.plugin.psi.utils.getContainingSuperClass(psiElement, returnDefault)

    @JvmStatic
    fun getContainingClass(element: PsiElement): ObjJClassDeclarationElement<*>? {
        return ObjJHasContainingClassPsiUtil.getContainingClass(element)
    }

    @JvmStatic
    fun getContainingClassName(methodHeader: ObjJMethodHeader): String {
        return ObjJHasContainingClassPsiUtil.getContainingClassName(methodHeader)
    }

    @JvmStatic
    fun getContainingClassName(compositeElement: ObjJCompositeElement): String {
        return ObjJHasContainingClassPsiUtil.getContainingClassName(compositeElement)
    }

    @JvmStatic
    fun getContainingClassName(classDeclarationElement: ObjJClassDeclarationElement<*>?): String {
        return ObjJHasContainingClassPsiUtil.getContainingClassName(classDeclarationElement)
    }

    @JvmStatic
    fun getContainingClassName(selectorLiteral: ObjJSelectorLiteral?): String {
        return ObjJHasContainingClassPsiUtil.getContainingClassName(selectorLiteral)
    }

    @JvmStatic
    fun getContainingSuperClassName(element: ObjJCompositeElement): String? {
        return ObjJHasContainingClassPsiUtil.getContainingSuperClassName(element)
    }

    @JvmStatic
    fun hasContainingClass(hasContainingClass: ObjJHasContainingClass, className: String?): Boolean {
        return className != null && hasContainingClass.containingClassName == className
    }

    @JvmStatic
    fun getInheritedProtocols(classDeclaration: ObjJImplementationDeclaration): List<String> =
            cappuccino.ide.intellij.plugin.psi.utils.getInheritedProtocols(classDeclaration)

    @JvmStatic
    fun getInheritedProtocols(classDeclaration: ObjJProtocolDeclaration): List<String> =
            cappuccino.ide.intellij.plugin.psi.utils.getInheritedProtocols(classDeclaration)

    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun getInheritedProtocols(typedef: ObjJTypeDef): List<String> = listOf()


    // ============================== //
    // ========= Variables ========== //
    // ============================== //

    @JvmStatic
    fun getIdType(varTypeId: ObjJVarTypeId): String {
        return ObjJMethodPsiUtils.getIdReturnType(varTypeId)
    }

    @JvmStatic
    fun getIdType(varTypeId: ObjJVarTypeId, follow: Boolean): String {
        return ObjJMethodPsiUtils.getIdReturnType(varTypeId, follow)
    }

    @JvmStatic
    fun getVarType(variable: ObjJGlobalVariableDeclaration): String? {
        return ObjJClassType.UNDETERMINED
    }

    @JvmStatic
    fun getIndexInQualifiedReference(namedElement: PsiElement?): Int {
        return ObjJQualifiedReferenceUtil.getIndexInQualifiedNameParent(namedElement)
    }

    @JvmStatic
    fun getIndexInQualifiedReference(namedElement: ObjJQualifiedReferenceComponent): Int {
        return ObjJQualifiedReferenceUtil.getIndexInQualifiedNameParent(namedElement)
    }

    @JvmStatic
    fun getVariableNameString(globalVariableDeclaration: ObjJGlobalVariableDeclaration): String {
        return globalVariableDeclaration.variableName.text
    }

    @JvmStatic
    fun getLastVar(qualifiedReference: ObjJQualifiedReference): ObjJVariableName? {
        return ObjJQualifiedReferenceUtil.getLastVariableName(qualifiedReference)
    }


    // ============================== //
    // =========== Blocks =========== //
    // ============================== //

    @JvmStatic
    fun getBlockList(element: ObjJCompositeElement): List<ObjJBlock> {
        return element.getChildrenOfType(ObjJBlock::class.java)
    }

    @JvmStatic
    fun getBlockList(hasBlock: ObjJHasBlockStatement): List<ObjJBlock> {
        val block: ObjJBlock = hasBlock.block ?: return listOf()
        return listOf(block)
    }

    @JvmStatic
    fun getBlockList(switchStatement: ObjJSwitchStatement): List<ObjJBlock> =
            cappuccino.ide.intellij.plugin.psi.utils.getBlockList(switchStatement)

    @JvmStatic
    fun getBlockList(ifStatement: ObjJIfStatement): List<ObjJBlock> =
            cappuccino.ide.intellij.plugin.psi.utils.getBlockList(ifStatement)

    @JvmStatic
    fun getBlockList(block: ObjJBlockElement): List<ObjJBlock> {
        return Arrays.asList(block)
    }

    @JvmStatic
    fun getBlockList(caseClause: ObjJCaseClause): List<ObjJBlock> {
        return if (caseClause.block != null)
            listOf(caseClause.block!!)
        else
            listOf()
    }

    @JvmStatic
    fun getBlockList(tryStatement: ObjJTryStatement) =
            cappuccino.ide.intellij.plugin.psi.utils.getBlockList(tryStatement)

    @JvmStatic
    fun getBlockList(expr: ObjJExpr): List<ObjJBlock> =
            cappuccino.ide.intellij.plugin.psi.utils.getBlockList(expr)

    @JvmStatic
    fun getBlockList(methodDeclaration: ObjJMethodDeclaration): List<ObjJBlock> {
        val block = methodDeclaration.block
        return if (block != null) listOf(block) else listOf()
    }

    @JvmStatic
    fun getBlockList(defineFunction: ObjJPreprocessorDefineFunction): List<ObjJBlock> {
        return cappuccino.ide.intellij.plugin.psi.utils.getBlockList(defineFunction)
    }

    @JvmStatic
    fun getBlock(function: ObjJPreprocessorDefineFunction): ObjJBlock? {
        return function.block
    }

    @JvmStatic
    fun getBlock(expr: ObjJExpr): ObjJBlock? =
            cappuccino.ide.intellij.plugin.psi.utils.getBlock(expr)

    @JvmStatic
    fun getBlock(case: ObjJCaseClause): ObjJBlock? {
        return case.getChildOfType(ObjJBracketLessBlock::class.java)
    }

    @JvmStatic
    fun getBlock(doWhileStatement: ObjJDoWhileStatement): ObjJBlock? {
        return doWhileStatement.getChildOfType(ObjJStatementOrBlock::class.java)
    }

    @JvmStatic
    fun getBlock(element: ObjJCompositeElement): ObjJBlock? {
        return element.getChildOfType(ObjJBlock::class.java)
    }

    @JvmStatic
    fun getOpenBrace(@Suppress("UNUSED_PARAMETER") ifStatement: ObjJPreprocessorIfStatement): ObjJBlock? {
        return null
    }

    // ============================== //
    // ========== Function ========== //
    // ============================== //

    @JvmStatic
    fun getName(className: ObjJClassName): String {
        return className.text
    }

    @JvmStatic
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
    @JvmStatic
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
    @JvmStatic
    @Throws(IncorrectOperationException::class)
    fun setName(functionLiteral: ObjJFunctionLiteral, name: String): ObjJFunctionLiteral {
        return ObjJFunctionDeclarationPsiUtil.setName(functionLiteral, name)
    }


    @JvmStatic
    fun getFunctionNameNode(functionLiteral: ObjJFunctionLiteral): ObjJNamedElement? {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameNode(functionLiteral)
    }

    @JvmStatic
    fun getFunctionNameAsString(functionLiteral: ObjJFunctionLiteral): String {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameAsString(functionLiteral)
    }
/*
    @JvmStatic
    fun getFunctionNamesAsString(functionLiteral: ObjJFunctionLiteral): List<String> {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNamesAsString(functionLiteral)
    }
*/
    @JvmStatic
    fun getFunctionNameAsString(functionDeclaration: ObjJFunctionDeclaration): String {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameAsString(functionDeclaration)
    }

    @JvmStatic
    fun getFunctionNameAsString(functionDeclaration: ObjJPreprocessorDefineFunction): String {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameAsString(functionDeclaration)
    }

    @JvmStatic
    fun getParamNameElements(functionDeclaration: ObjJFunctionDeclarationElement<*>): List<ObjJVariableName> {
        return ObjJFunctionDeclarationPsiUtil.getParamNameElements(functionDeclaration)
    }

    /**
     * A method to get the function scope of a given function
     * This is to prevent a current problem of resolving to functions outside scope.
     */
    @JvmStatic
    fun getFunctionScope(functionDeclaration: ObjJFunctionDeclarationElement<*>): ObjJFunctionScope =
            ObjJFunctionDeclarationPsiUtil.getFunctionScope(functionDeclaration)

    @JvmStatic
    fun getParamNames(functionDeclaration: ObjJFunctionDeclarationElement<*>): List<String> {
        return ObjJFunctionDeclarationPsiUtil.getParamNames(functionDeclaration)
    }

    @JvmStatic
    fun getReturnType(functionDeclaration: ObjJFunctionDeclaration): String {
        return ObjJFunctionDeclarationPsiUtil.getReturnType(functionDeclaration)
    }

    @JvmStatic
    fun getReturnType(functionLiteral: ObjJFunctionLiteral): String {
        return ObjJFunctionDeclarationPsiUtil.getReturnType(functionLiteral)
    }

    @JvmStatic
    fun getReturnType(functionDefinition: ObjJPreprocessorDefineFunction): String? =
            ObjJFunctionDeclarationPsiUtil.getReturnType(functionDefinition)

    @JvmStatic
    fun getQualifiedNameText(functionCall: ObjJFunctionCall): String {
        return ObjJFunctionDeclarationPsiUtil.getQualifiedNameText(functionCall) ?: ""
    }

    @JvmStatic
    fun getOpenBraceOrAtOpenBrace(element:PsiElement) : PsiElement? {
        return element.getChildByType(ObjJTypes.ObjJ_OPEN_BRACE) ?: element.getChildByType(ObjJTypes.ObjJ_AT_OPEN_BRACE)
    }

    @JvmStatic
    fun getOpenBrace(element:PsiElement) : PsiElement? {
        return element.getChildByType(ObjJTypes.ObjJ_OPEN_BRACE)
    }

    @JvmStatic
    fun getCloseBrace(element:PsiElement) : PsiElement? {
        return element.getChildByType(ObjJTypes.ObjJ_CLOSE_BRACE)
    }

    // ============================== //
    // ===== QualifiedReference ===== //
    // ============================== //

    @JvmStatic
    fun getQualifiedNameText(variableName: ObjJVariableName): String {
        return variableName.text
    }

    @JvmStatic
    fun getQualifiedNameText(methodCall: ObjJMethodCall): String {
        return methodCall.text
    }

    @JvmStatic
    fun getDescriptiveText(psiElement: PsiElement): String? =
            ObjJDescriptionUtil.getDescriptiveText(psiElement)

    // ============================== //
    // ========== Imports =========== //
    // ============================== //

    @JvmStatic
    fun getFileName(reference: ObjJFrameworkReference): String {
        return ObjJImportPsiUtils.getFileName(reference)
    }

    @JvmStatic
    fun getFileName(framework: ObjJImportFramework): String {
        return ObjJImportPsiUtils.getFileName(framework)
    }

    @JvmStatic
    fun getFileName(framework: ObjJIncludeFramework): String {
        return ObjJImportPsiUtils.getFileName(framework)
    }

    @JvmStatic
    fun getFileName(framework: ObjJImportFile): String {
        return ObjJImportPsiUtils.getFileName(framework)
    }

    @JvmStatic
    fun getFileName(framework: ObjJIncludeFile): String {
        return ObjJImportPsiUtils.getFileName(framework)
    }

    @JvmStatic
    fun getFrameworkName(reference: ObjJFrameworkReference): String? {
        return ObjJImportPsiUtils.getFrameworkName(reference)
    }


    @JvmStatic
    fun getFrameworkName(framework: ObjJImportFramework): String? {
        return ObjJImportPsiUtils.getFrameworkName(framework)
    }

    @JvmStatic
    fun getFrameworkName(framework: ObjJIncludeFile): String? {
        return ObjJImportPsiUtils.getFrameworkName(framework)
    }

    @JvmStatic
    fun getFrameworkName(framework: ObjJImportFile): String? {
        return ObjJImportPsiUtils.getFrameworkName(framework)
    }

    @JvmStatic
    fun getFrameworkName(framework: ObjJIncludeFramework): String? {
        return ObjJImportPsiUtils.getFrameworkName(framework)
    }

    @JvmStatic
    fun getImportAsUnifiedString(importStatement: ObjJImportStatement<*>) =
            (importStatement.frameworkName ?: "") + ObjJImportStatement.DELIMITER + importStatement.fileName

    // ============================== //
    // ===== VariableAssignments ==== //
    // ============================== //

    @JvmStatic
    fun getAssignedValue(assignmentLogical: ObjJVariableAssignmentLogical): ObjJExpr {
        return ObjJVariableAssignmentsPsiUtil.getAssignedValue(assignmentLogical)
    }

    @JvmStatic
    fun getQualifiedReferenceList(assignmentLogical: ObjJVariableAssignmentLogical): List<ObjJQualifiedReference> {
        return if (assignmentLogical.qualifiedReference != null) listOf(assignmentLogical.qualifiedReference!!) else listOf()
    }

    // ============================== //
    // ====== Iterator Elements ===== //
    // ============================== //

    @JvmStatic
    fun getConditionalExpression(doWhileStatement: ObjJDoWhileStatement?): ObjJExpr? {
        return doWhileStatement?.conditionExpression?.expr
    }

    // ============================== //
    // =========== Misc ============= //
    // ============================== //

    @JvmStatic
    fun getFileName(element: PsiElement?): String? {
        if (element == null) {
            return null
        }
        return element.containingFile?.name ?: element.containingFile?.virtualFile?.name
    }

    @JvmStatic
    fun getConditionalExpression(composite: ObjJConditionalStatement): ObjJExpr? {
        return composite.getChildOfType(ObjJConditionExpression::class.java)?.expr
    }

    // ============================== //
    // ====== Should Resolve ======== //
    // ============================== //

    @JvmStatic
    fun shouldResolve(psiElement: PsiElement?): Boolean {
        return ObjJResolveableElementUtil.shouldResolve(psiElement)
    }

    @JvmStatic
    fun shouldResolve(psiElement: ObjJClassDeclarationElement<*>?): Boolean {
        return ObjJResolveableElementUtil.shouldResolve(psiElement)
    }

    @JvmStatic
    fun shouldResolve(psiElement: PsiElement?, shouldNotResolveLoggingStatement: String?): Boolean {
        return ObjJResolveableElementUtil.shouldResolve(psiElement, shouldNotResolveLoggingStatement)
    }

    @JvmStatic
    fun shouldResolve(hasContainingClass: ObjJHasContainingClass?): Boolean {
        return ObjJResolveableElementUtil.shouldResolve(hasContainingClass as PsiElement?) && shouldResolve(hasContainingClass!!.containingClass)
    }

    // ============================== //
    // ========== Folding =========== //
    // ============================== //

    @JvmStatic
    fun createFoldingDescriptor(implementation: ObjJImplementationDeclaration, foldingGroup: FoldingGroup) =
            ObjJCodeFoldingBuilder.execute(implementation, foldingGroup)

    @JvmStatic
    fun createFoldingDescriptor(protocol: ObjJProtocolDeclaration, foldingGroup: FoldingGroup) =
            ObjJCodeFoldingBuilder.execute(protocol, foldingGroup)

    @JvmStatic
    fun createFoldingDescriptor(methodDeclaration: ObjJMethodDeclaration, foldingGroup: FoldingGroup) =
            ObjJCodeFoldingBuilder.execute(methodDeclaration, foldingGroup)

    @JvmStatic
    fun createFoldingDescriptor(comment: ObjJComment, foldingGroup: FoldingGroup) =
            ObjJCodeFoldingBuilder.execute(comment, foldingGroup)

    @JvmStatic
    fun createFoldingDescriptor(variablesList: ObjJInstanceVariableList, foldingGroup: FoldingGroup) =
            ObjJCodeFoldingBuilder.execute(variablesList, foldingGroup)

    @JvmStatic
    fun createFoldingDescriptor(variableAssignment: ObjJBodyVariableAssignment, foldingGroup: FoldingGroup) =
            ObjJCodeFoldingBuilder.execute(variableAssignment, foldingGroup)

    @JvmStatic
    fun createFoldingDescriptor(variableAssignment: ObjJFunctionDeclarationElement<*>, foldingGroup: FoldingGroup) =
            ObjJCodeFoldingBuilder.execute(variableAssignment, foldingGroup)

    // ============================== //
    // ======= Structure View ======= //
    // ============================== //

    @JvmStatic
    fun createTreeStructureElement(declaration: ObjJImplementationDeclaration): ObjJStructureViewElement =
            ObjJTreeStructureUtil.createTreeStructureElement(declaration)

    @JvmStatic
    fun getTreeStructureChildElements(declaration: ObjJImplementationDeclaration): Array<ObjJStructureViewElement> = ObjJTreeStructureUtil.getTreeStructureChildElements(declaration)

    @JvmStatic
    fun createTreeStructureElement(instanceVariable: ObjJInstanceVariableDeclaration): ObjJStructureViewElement = ObjJTreeStructureUtil.createTreeStructureElement(instanceVariable)

    @JvmStatic
    fun createTreeStructureElement(declaration: ObjJProtocolDeclaration): ObjJStructureViewElement = ObjJTreeStructureUtil.createTreeStructureElement(declaration)

    @JvmStatic
    fun createTreeStructureElement(protocol: ObjJProtocolScopedMethodBlock): ObjJStructureViewElement = ObjJTreeStructureUtil.createTreeStructureElement(protocol)

    @JvmStatic
    fun createTreeStructureElement(methodDeclaration: ObjJMethodDeclaration): ObjJStructureViewElement = createTreeStructureElement(methodDeclaration.methodHeader)

    @JvmStatic
    fun createTreeStructureElement(header: ObjJMethodHeader): ObjJStructureViewElement = ObjJTreeStructureUtil.createTreeStructureElement(header)

    // ============================== //
    // =========== PARSER =========== //
    // ============================== //

    @JvmStatic
    fun eos(compositeElement: PsiElement?): Boolean = ObjJSharedParserFunctions.eos(compositeElement)

    @JvmStatic
    fun eosToken(ahead: IElementType?, hadLineTerminator: Boolean): Boolean = ObjJSharedParserFunctions.eosToken(ahead, hadLineTerminator)

    // ============================== //
    // ======= Presentation ========= //
    // ============================== //

    @JvmStatic
    fun getIcon(element: PsiElement): Icon? = cappuccino.ide.intellij.plugin.utils.getIcon(element)


    @JvmStatic
    fun getFormalParameterArgList(functionDeclaration: ObjJFunctionDeclaration): List<ObjJFormalParameterArg> {
        return functionDeclaration.formalParameterList?.formalParameterArgList ?: listOf()
    }

    @JvmStatic
    fun getLastFormalParameterArg(functionDeclaration: ObjJFunctionDeclaration): ObjJLastFormalParameterArg? {
        return functionDeclaration.formalParameterList?.lastFormalParameterArg
    }

    @JvmStatic
    fun getFormalParameterArgList(functionDeclaration: ObjJFunctionLiteral): List<ObjJFormalParameterArg> {
        return functionDeclaration.formalParameterList?.formalParameterArgList ?: listOf()
    }

    @JvmStatic
    fun getLastFormalParameterArg(functionDeclaration: ObjJFunctionLiteral): ObjJLastFormalParameterArg? {
        return functionDeclaration.formalParameterList?.lastFormalParameterArg
    }

    @JvmStatic
    fun getFormalParameterArgList(functionDeclaration: ObjJPreprocessorDefineFunction): List<ObjJFormalParameterArg> {
        return functionDeclaration.formalParameterList?.formalParameterArgList ?: listOf()
    }

    @JvmStatic
    fun getLastFormalParameterArg(functionDeclaration: ObjJPreprocessorDefineFunction): ObjJLastFormalParameterArg? {
        return functionDeclaration.formalParameterList?.lastFormalParameterArg
    }


    @JvmStatic
    fun getExprList(functionCall: ObjJFunctionCall): List<ObjJExpr> {
        return functionCall.arguments.exprList
    }


    @JvmStatic
    fun getCloseParen(functionCall: ObjJFunctionCall): PsiElement {
        return functionCall.arguments.closeParen
    }

    @JvmStatic
    fun getOpenParen(functionCall: ObjJFunctionCall): PsiElement {
        return functionCall.arguments.openParen
    }

    @JvmStatic
    fun getIterationStatementList(block: ObjJBlockElement): List<ObjJIterationStatement> {
        return block.getChildrenOfType(ObjJIterationStatement::class.java)
    }

    @JvmStatic
    fun getIterationStatementList(block: ObjJHasBlockStatement): List<ObjJIterationStatement> {
        return block.getChildrenOfType(ObjJIterationStatement::class.java)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    @JvmStatic
    fun <PsiT : PsiElement> PsiElement.isIn(parentClass: Class<PsiT>): Boolean {
        return getParentOfType(parentClass) != null
    }

    @JvmStatic
    fun getContainingClassNameOrNull(element:ObjJHasContainingClass) : String? {
        return element.containingClass?.classType?.className
    }

}
