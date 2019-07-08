package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.caches.*
import cappuccino.ide.intellij.plugin.hints.ObjJFunctionDescription
import cappuccino.ide.intellij.plugin.hints.description
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.inference.inferQualifiedReferenceType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListClass
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassTypeName
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope
import cappuccino.ide.intellij.plugin.references.*
import cappuccino.ide.intellij.plugin.references.presentation.ObjJSelectorItemPresentation
import cappuccino.ide.intellij.plugin.structure.ObjJCodeFoldingBuilder
import cappuccino.ide.intellij.plugin.structure.ObjJStructureViewElement
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.QualifiedReferenceStubComponents
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJSelectorStruct
import cappuccino.ide.intellij.plugin.stubs.types.toQualifiedNamePaths
import cappuccino.ide.intellij.plugin.stubs.types.toStubParts
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException
import java.util.*
import javax.swing.Icon

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
    fun getName(propertyName: ObjJPropertyName): String {
        return propertyName.key
    }

    @JvmStatic
    fun getName(defineFunction: ObjJPreprocessorDefineFunction): String {
        return defineFunction.functionNameString
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
    fun getFunctionNameString(functionCall: ObjJFunctionCall): String? {
        return functionCall.functionName?.text
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
    fun setName(propertyName: ObjJPropertyName, name: String): ObjJPropertyName {
        return propertyName
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
        val classNameString = classDeclaration.classNameString
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
        return typedef.className?.text ?: ""
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

    @JvmStatic
    fun getCache(protocol: ObjJProtocolDeclaration): ObjJProtocolDeclarationCache {
        return ObjJProtocolDeclarationCache(protocol)
    }

    @JvmStatic
    fun getCache(declaration: ObjJImplementationDeclaration): ObjJImplementationDeclarationCache {
        return ObjJImplementationDeclarationCache(declaration)
    }

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
        val offset = if (outText.endsWith(quotationMark)) 1 else 0
        return if (outText.endsWith(quotationMark)) outText.substring(0, outText.length - offset) else outText

    }

    @JvmStatic
    fun toString(variableName: ObjJVariableName): String {
        return ObjJVariablePsiUtil.toString(variableName)
    }

    // ============================== //
    // ====== MethodHeaders ========= //
    // ============================== //
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

    @JvmStatic
    fun getMethodHeaderCache(methodHeader: ObjJMethodHeaderDeclaration<*>): ObjJMethodHeaderDeclarationCache = createMethodHeaderCache(methodHeader)

    // ============================== //
    // ======= Return Types ========= //
    // ============================== //

    @JvmStatic
    fun getReturnTypes(methodHeader: ObjJMethodHeader, tag: Long): Set<String> {
        return ObjJMethodPsiUtils.getReturnTypes(methodHeader, true, tag)
    }

    @JvmStatic
    fun getReturnTypes(methodHeader: ObjJSelectorLiteral): Set<String> {
        return setOf(ObjJMethodPsiUtils.getExplicitReturnType(methodHeader))
    }

    @JvmStatic
    fun getReturnTypes(accessorProperty: ObjJAccessorProperty): Set<String> {
        return setOf(ObjJMethodPsiUtils.getExplicitReturnType(accessorProperty))
    }

    @JvmStatic
    fun getExplicitReturnType(methodHeader: ObjJMethodHeader): String {
        return ObjJMethodPsiUtils.getExplicitReturnType(methodHeader, true)
    }

    @JvmStatic
    fun getExplicitReturnType(methodHeader: ObjJSelectorLiteral): String {
        return ObjJMethodPsiUtils.getExplicitReturnType(methodHeader)
    }

    @JvmStatic
    fun getExplicitReturnType(accessorProperty: ObjJAccessorProperty): String {
        return ObjJMethodPsiUtils.getExplicitReturnType(accessorProperty)
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
    fun getPossibleCallTargetTypes(callTarget: ObjJCallTarget, tag: Long): List<String> {
        return getPossibleClassTypesForCallTarget(callTarget, tag).toList()
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
    fun respondsToSelectors(variableName: ObjJVariableName): List<ObjJSelectorLiteral> = ObjJVariablePsiUtil.respondsToSelectors(variableName)

    @JvmStatic
    fun respondsToSelector(variableName: ObjJVariableName, selector: String): Boolean = ObjJVariablePsiUtil.respondsToSelector(variableName, selector)

    @JvmStatic
    fun respondsToSelectorStrings(variableName: ObjJVariableName): Set<String> = ObjJVariablePsiUtil.respondsToSelectorStrings(variableName)

    @JvmStatic
    fun getParamTypes(methodHeader: ObjJMethodHeader): List<ObjJFormalVariableType?> {
        return ObjJMethodPsiUtils.getParamTypes(methodHeader.methodDeclarationSelectorList)
    }

    @JvmStatic
    fun getParamTypesAsStrings(methodHeader: ObjJMethodHeader): List<String> {
        return ObjJMethodPsiUtils.getParamTypesAsString(methodHeader.methodDeclarationSelectorList)
    }

    @JvmStatic
    fun getSelectorsAsStructs(selectorLiteral:ObjJSelectorLiteral) : List<ObjJSelectorStruct> {
        return ObjJMethodPsiUtils.getSelectorsAsStructs(selectorLiteral)
    }


    @JvmStatic
    fun getSelectorsAsStructs(header:ObjJMethodHeader) : List<ObjJSelectorStruct> {
        return ObjJMethodPsiUtils.getSelectorsAsStructs(header)
    }

    @JvmStatic
    fun getSelectorsAsStructs(accessorProperty: ObjJAccessorProperty) : List<ObjJSelectorStruct> {
        return ObjJAccessorPropertyPsiUtil.getSelectorsAsStructs(accessorProperty)
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
    fun getVariableType(type:ObjJFormalVariableType) : String {
        return type.varTypeId?.className?.text ?: type.text
    }

    @JvmStatic
    fun isRequired(methodHeader: ObjJMethodHeader): Boolean =
            ObjJMethodPsiUtils.isRequired(methodHeader)


    @JvmStatic
    fun getSingleVariableNameElementOrNull(callTarget: ObjJCallTarget): ObjJVariableName? {
        val qualifiedReferenceComponents = callTarget.qualifiedReference?.qualifiedNameParts
                ?: return null
        if (qualifiedReferenceComponents.size != 1 && qualifiedReferenceComponents[0] !is ObjJVariableName)
            return null
        return (qualifiedReferenceComponents[0] as? ObjJVariableName)?.reference?.resolve() as? ObjJVariableName
    }
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
    fun getReference(variableName: ObjJVariableName): ObjJVariableReference {
        return ObjJVariableReference(variableName)
    }

    @JvmStatic
    fun getReferences(variableName: ObjJVariableName): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(variableName, PsiReferenceService.Hints.NO_HINTS)
    }

    @JvmStatic
    fun getReference(functionName: ObjJFunctionName): PsiPolyVariantReference {
        return ObjJFunctionNameReference(functionName)
    }

    @JvmStatic
    fun getReferences(className: ObjJClassName): Array<PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(className, PsiReferenceService.Hints.NO_HINTS)
    }


    @JvmStatic
    fun getReference(fileName: ObjJFrameworkFileName): PsiReference {
        return ObjJImportFileNameReference(fileName)
    }

    @JvmStatic
    fun getReference(fileName: ObjJFileNameAsImportString): PsiPolyVariantReference {
        return ObjJFileNameAsStringLiteralReference(fileName)
    }

    @JvmStatic
    fun getReference(fileName: ObjJStringLiteral): PsiPolyVariantReference {
        return ObjJStringLiteralReference(fileName)
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
    fun getIndexInQualifiedReference(enclosedExpression: ObjJParenEnclosedExpr): Int {
        return 0
    }

    @JvmStatic
    fun getVariableNameString(globalVariableDeclaration: ObjJGlobalVariableDeclaration): String {
        return globalVariableDeclaration.variableName.text
    }

    @JvmStatic
    fun getLastVar(qualifiedReference: ObjJQualifiedReference): ObjJVariableName? {
        return ObjJQualifiedReferenceUtil.getLastVariableName(qualifiedReference)
    }

    @JvmStatic
    fun getVariableType(variable: ObjJInstanceVariableDeclaration): String = ObjJVariablePsiUtil.getVariableType(variable)


    @JvmStatic
    fun getMethods(variableName: ObjJVariableName, tag: Long): List<ObjJMethodHeaderDeclaration<*>> {
        return variableName.getCachedMethods(tag)
    }

    @JvmStatic
    fun getMethodSelectors(variableName: ObjJVariableName, tag: Long): Set<String> {
        return getMethods(variableName, tag).map { it.selectorString }.toSet()
    }

    @JvmStatic
    fun getVariableType(variableName: ObjJVariableName, tag: Long): InferenceResult? {
        return variableName.getClassTypes(tag)
                ?: inferQualifiedReferenceType(variableName.previousSiblings + variableName, tag)
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
    fun getFunctionNamesAsString(functionLiteral: ObjJFunctionLiteral): List<String> {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNamesAsString(functionLiteral)
    }

    @JvmStatic
    fun getFunctionNameString(functionLiteral: ObjJFunctionLiteral): String {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameAsString(functionLiteral)
    }

    @JvmStatic
    fun getFunctionNameString(functionDeclaration: ObjJFunctionDeclaration): String {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameAsString(functionDeclaration)
    }

    @JvmStatic
    fun getFunctionNameString(functionDeclaration: ObjJPreprocessorDefineFunction): String {
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
    fun getReturnType(functionDeclaration: ObjJFunctionDeclaration, tag: Long): String? {
        return ObjJFunctionDeclarationPsiUtil.getReturnType(functionDeclaration, tag)
    }

    @JvmStatic
    fun getReturnType(functionLiteral: ObjJFunctionLiteral, tag: Long): String? {
        return ObjJFunctionDeclarationPsiUtil.getReturnType(functionLiteral, tag)
    }

    @JvmStatic
    fun getReturnType(functionDefinition: ObjJPreprocessorDefineFunction, tag: Long): String? =
            ObjJFunctionDeclarationPsiUtil.getReturnType(functionDefinition, tag)

    @JvmStatic
    fun getQualifiedNameText(functionCall: ObjJFunctionCall): String {
        return ObjJFunctionDeclarationPsiUtil.getQualifiedNameText(functionCall) ?: ""
    }

    @Suppress("unused")
    @JvmStatic
    fun getOpenBraceOrAtOpenBrace(element: PsiElement): PsiElement? {
        return element.getChildByType(ObjJTypes.ObjJ_OPEN_BRACE) ?: element.getChildByType(ObjJTypes.ObjJ_AT_OPEN_BRACE)
    }

    @JvmStatic
    fun getOpenBrace(element: PsiElement): PsiElement? {
        return element.getChildByType(ObjJTypes.ObjJ_OPEN_BRACE)
    }

    @JvmStatic
    fun getCloseBrace(element: PsiElement): PsiElement? {
        return element.getChildByType(ObjJTypes.ObjJ_CLOSE_BRACE)
    }

    @JvmStatic
    fun getCache(functionDeclaration: ObjJFunctionDeclarationElement<*>): ObjJFunctionDeclarationCache = ObjJFunctionDeclarationCache(functionDeclaration)

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
    fun getDescription(functionDeclaration: ObjJFunctionDeclarationElement<*>): ObjJFunctionDescription {
        val name = functionDeclaration.functionNameString
        val returnType = functionDeclaration.getReturnType(createTag())
        val description = ObjJFunctionDescription(name, returnType)
        functionDeclaration.formalParameterArgList.forEach {
            description.addParameter(it.description)
        }
        return description
    }

    @JvmStatic
    fun getDescriptiveText(psiElement: PsiElement): String? =
            ObjJDescriptionUtil.getDescriptiveText(psiElement)

    @JvmStatic
    fun getQualifiedNamePath(qualifiedReference: ObjJQualifiedReference): QualifiedReferenceStubComponents {
        return qualifiedReference.stub?.components ?: qualifiedReference.toStubParts()
    }

    @JvmStatic
    fun getQualifiedNamePaths(declaration: ObjJVariableDeclaration): List<QualifiedReferenceStubComponents> = declaration.stub?.qualifiedNamesList
            ?: declaration.toQualifiedNamePaths()

    @JvmStatic
    fun hasVarKeyword(declaration: ObjJVariableDeclaration): Boolean {
        return (declaration.parent.parent as? ObjJBodyVariableAssignment)?.varModifier != null
    }

    // ============================== //
    // ========== Imports =========== //
    // ============================== //

    @JvmStatic
    fun getFileNameString(descriptor: ObjJFrameworkDescriptor): String {
        return ObjJImportPsiUtils.getFileNameString(descriptor)
    }

    @JvmStatic
    fun getFileNameString(fileNameAsImportString: ObjJFileNameAsImportString): String {
        return ObjJImportPsiUtils.getFileNameString(fileNameAsImportString)
    }

    @JvmStatic
    fun getFileNameString(framework: ObjJImportFramework): String {
        return ObjJImportPsiUtils.getFileNameString(framework)
    }

    @JvmStatic
    fun getFileNameString(framework: ObjJIncludeFramework): String {
        return ObjJImportPsiUtils.getFileNameString(framework)
    }

    @JvmStatic
    fun getFileNameString(framework: ObjJImportFile): String {
        return ObjJImportPsiUtils.getFileNameString(framework)
    }

    @JvmStatic
    fun getFileNameString(framework: ObjJIncludeFile): String {
        return ObjJImportPsiUtils.getFileNameString(framework)
    }

    @JvmStatic
    fun getFileNameString(importIncludeStatement: ObjJImportIncludeStatement): String = importIncludeStatement.importIncludeElement?.fileNameString
            ?: ""

    @JvmStatic
    fun getFrameworkNameString(descriptor: ObjJFrameworkDescriptor): String? {
        return ObjJImportPsiUtils.getFrameworkNameString(descriptor)
    }


    @JvmStatic
    fun getFrameworkNameString(framework: ObjJImportFramework): String? {
        return ObjJImportPsiUtils.getFrameworkNameString(framework)
    }

    @JvmStatic
    fun getFrameworkNameString(framework: ObjJIncludeFile): String? {
        return ObjJImportPsiUtils.getFrameworkNameString(framework)
    }

    @JvmStatic
    fun getFrameworkNameString(framework: ObjJImportFile): String? {
        return ObjJImportPsiUtils.getFrameworkNameString(framework)
    }

    @JvmStatic
    fun getFrameworkNameString(framework: ObjJIncludeFramework): String? {
        return ObjJImportPsiUtils.getFrameworkNameString(framework)
    }

    @JvmStatic
    fun getFrameworkNameString(importIncludeStatement: ObjJImportIncludeStatement): String? = importIncludeStatement.importIncludeElement?.frameworkNameString

    @JvmStatic
    fun getImportAsUnifiedString(importElement: ObjJImportElement<*>) =
            (importElement.frameworkNameString ?: "") + ObjJImportElement.DELIMITER + importElement.fileNameString

    @JvmStatic
    fun getName(fileName: ObjJFileNameAsImportString): String {
        return fileName.stringLiteral.stringValue
    }

    @JvmStatic
    fun setName(fileName: ObjJFileNameAsImportString, newName: String): PsiElement {
        return fileName
    }

    @JvmStatic
    fun getName(fileName: ObjJFrameworkFileName, newName: String): String {
        return fileName.text
    }

    @JvmStatic
    fun setName(fileName: ObjJFrameworkFileName, newName: String): PsiElement {
        return fileName
    }

    @JvmStatic
    fun getReference(statement: ObjJImportIncludeStatement): PsiReference? = ObjJImportPsiUtils.getReference(statement)

    @JvmStatic
    fun resolve(statement: ObjJImportIncludeStatement): ObjJFile? = ObjJImportPsiUtils.resolve(statement)

    @JvmStatic
    fun multiResolve(statement: ObjJImportIncludeStatement): List<ObjJFile> = ObjJImportPsiUtils.multiResolve(statement)

    @JvmStatic
    fun getImportIncludeElement(importIncludeStatement: ObjJImportIncludeStatement): ObjJImportElement<*>? = ObjJImportPsiUtils.getImportIncludeElement(importIncludeStatement)

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
    fun getCloseParen(functionCall: ObjJFunctionCall): PsiElement? {
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


    // ============================== //
    // ======= Object Literal ======= //
    // ============================== //
    @JvmStatic
    fun toJsObjectTypeSimple(element: ObjJObjectLiteral): JsTypeListClass {
        return ObjJObjectPsiUtils.toJsObjectTypeSimple(element)
    }

    @JvmStatic
    fun toJsObjectType(element: ObjJObjectLiteral, tag: Long): JsTypeListClass {
        return ObjJObjectPsiUtils.toJsObjectType(element, tag)
    }

    @JvmStatic
    fun getKey(assignment: ObjJPropertyAssignment): String {
        return ObjJObjectPsiUtils.getKey(assignment)
    }

    @JvmStatic
    fun getKey(propertyName: ObjJPropertyName): String {
        return ObjJObjectPsiUtils.getKey(propertyName)
    }

    @JvmStatic
    fun getNamespacedName(assignment: ObjJPropertyAssignment): String {
        return getNamespacedName(assignment.propertyName)
    }

    @JvmStatic
    fun getNamespacedName(propertyName: ObjJPropertyName): String {
        return propertyName.stub?.namespacedName ?: ObjJObjectPsiUtils.getNamespacedName(propertyName)
    }

}
