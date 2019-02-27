package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.contributor.ObjJMethodCallCompletionContributorUtil
import cappuccino.ide.intellij.plugin.structure.ObjJCodeFoldingBuilder
import cappuccino.ide.intellij.plugin.structure.ObjJStructureViewElement
import cappuccino.ide.intellij.plugin.lang.ObjJIcons
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.references.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.references.presentation.ObjJSelectorItemPresentation
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.editor.FoldingGroup

import javax.swing.*
import java.util.*

import java.util.regex.Pattern

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
    fun getQualifiedNameParts(qualifiedReference:ObjJQualifiedReference) : List<ObjJQualifiedReferenceComponent> {
        return ObjJVariableNameUtil.getQualifiedNameParts(qualifiedReference)
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
        if (newClassName == null || newClassName.isEmpty()) {
            return className
        }
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
        oldFunctionName.parent.node.replaceChild(oldFunctionName.node, functionName.node)
        return functionName
    }

    @JvmStatic
    fun getClassType(classDeclaration: ObjJClassDeclarationElement<*>): ObjJClassType {
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
    fun getContainingSuperClass(hasContainingClass: ObjJHasContainingClass, returnDefault: Boolean = false): ObjJClassName? = cappuccino.ide.intellij.plugin.psi.utils.getContainingSuperClass(hasContainingClass, returnDefault)

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
    fun getPresentation(className:ObjJClassName) : ItemPresentation {
        return cappuccino.ide.intellij.plugin.psi.utils.getPresentation(className)
    }

    // ============================== //
    // =========== String =========== //
    // ============================== //

    @JvmStatic
    fun getStringValue(stringLiteral: ObjJStringLiteral): String {
        val rawText = stringLiteral.text
        val quotationMark:String = if (rawText.startsWith("\"")) "\"" else if (rawText.startsWith("'")) "'" else return rawText
        val outText = if (rawText.startsWith(quotationMark)) rawText.substring(1) else rawText
        return if (outText.endsWith(quotationMark))outText.substring(0, outText.length - 1) else outText

    }

    @JvmStatic
    fun toString(variableName:ObjJVariableName) : String {
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
    fun getVariableType(globalVariableDeclaration: ObjJGlobalVariableDeclaration) : String? {
        return ObjJVariablePsiUtil.getVariableType(globalVariableDeclaration)
    }

    // ============================== //
    // ======== Method Call ========= //
    // ============================== //
    @JvmStatic
    fun getCallTargetText(methodCall: ObjJMethodCall): String =
            cappuccino.ide.intellij.plugin.psi.utils.getCallTargetText(methodCall)

    @JvmStatic
    fun getPossibleCallTargetTypes(callTarget:ObjJCallTarget) : List<String> {
        return Collections.singletonList(ObjJClassType.UNDETERMINED)//ObjJCallTargetUtil.getPossibleCallTargetTypes(callTarget)
    }

    @JvmStatic
    fun getPossibleCallTargetTypes(methodCall: ObjJMethodCall) : List<String> {
        return Collections.singletonList(ObjJClassType.UNDETERMINED)// ObjJCallTargetUtil.getPossibleCallTargetTypes(methodCall)
    }

    // ============================== //
    // ========= Selectors ========== //
    // ============================== //

    @JvmStatic
    fun getSelectorString(methodHeader: ObjJMethodHeader): String {
        return ObjJMethodPsiUtils.getSelectorString(methodHeader)
    }

    @JvmStatic
    fun getSelectorString(selector: ObjJSelector, addSuffix: Boolean): String {
        return ObjJMethodPsiUtils.getSelectorString(selector, addSuffix)
    }

    @JvmStatic
    fun getSelectorString(selector: ObjJMethodDeclarationSelector, addSuffix: Boolean): String {
        return ObjJMethodPsiUtils.getSelectorString(selector, addSuffix)
    }

    @JvmStatic
    fun getSelectorString(selectorLiteral: ObjJSelectorLiteral): String {
        return ObjJMethodPsiUtils.getSelectorString(selectorLiteral)
    }

    @JvmStatic
    fun getSelectorString(property: ObjJAccessorProperty): String =
            ObjJAccessorPropertyPsiUtil.getSelectorString(property)

    @JvmStatic
    fun getSelectorString(methodCall: ObjJMethodCall): String =
            cappuccino.ide.intellij.plugin.psi.utils.getSelectorString(methodCall)

    @JvmStatic
    fun getSelectorStrings(methodHeader: ObjJMethodHeader): List<String> {
        return ObjJMethodPsiUtils.getSelectorStrings(methodHeader)
    }

    @JvmStatic
    fun getSelectorStrings(methodCall: ObjJMethodCall): List<String> =
            cappuccino.ide.intellij.plugin.psi.utils.getSelectorStrings(methodCall)

    @JvmStatic
    fun getSelectorStrings(selectorLiteral: ObjJSelectorLiteral): List<String> {
        return if (selectorLiteral.stub != null && !selectorLiteral.stub.selectorStrings.isEmpty()) {
            selectorLiteral.stub.selectorStrings
        } else ObjJMethodPsiUtils.getSelectorStringsFromSelectorList(selectorLiteral.selectorList)
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
        return ObjJMethodPsiUtils.getSelectorUntil(targetSelectorElement, include)
    }

    @JvmStatic
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
        thisSelectorIndex = if (selectorIndex < 0 || selectorIndex >= selectorList.size) {
            selectorList.size - 1
        } else {
            selectorIndex
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
            if (subSelectorPattern == null || subSelectorPattern.matcher(currentSelector.getSelectorString(false)).matches()) {
                return currentSelector
            }
        }
        //LOGGER.log(Level.WARNING, "Failed to find selector matching <"+subSelector+"> or any selector before foldingDescriptors of <"+selectorList.size()+"> selectors");
        return null
    }

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
    fun getFormalVariableType(selector:ObjJMethodDeclarationSelector) : ObjJFormalVariableType? {
        return selector.methodHeaderSelectorFormalVariableType?.formalVariableType
    }

    @JvmStatic
    fun hasMethod(classElement: ObjJClassDeclarationElement<*>, selector: String): Boolean {
        return !classElement.getMethodHeaders().none { it.selectorString == selector }
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
    fun getAccessorPropertyList(declaration: ObjJInstanceVariableDeclaration) : List<ObjJAccessorProperty>  {
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
        //LOGGER.log(Level.INFO, "Getting references(plural) for selector");
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
    fun getAllInheritedClasses(className: String, project: Project): List<String> {
        return ObjJInheritanceUtil.getAllInheritedClasses(className, project)
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
    fun getVarType(variable:ObjJGlobalVariableDeclaration) : String? {
        return ObjJClassType.UNDETERMINED
    }

    @JvmStatic
    fun getIndexInQualifiedReference(namedElement: PsiElement?): Int {
        return ObjJVariableNameUtil.getIndexInQualifiedNameParent(namedElement)
    }

    @JvmStatic
    fun getIndexInQualifiedReference(namedElement: ObjJQualifiedReferenceComponent): Int {
        return ObjJVariableNameUtil.getIndexInQualifiedNameParent(namedElement)
    }

    @JvmStatic
    fun getVariableNameString(globalVariableDeclaration: ObjJGlobalVariableDeclaration) : String {
        return globalVariableDeclaration.variableName.text
    }

    @JvmStatic
    fun getLastVar(qualifiedReference: ObjJQualifiedReference) : ObjJVariableName? {
        return ObjJVariablePsiUtil.getLastVariableName(qualifiedReference)
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
        val block:ObjJBlock = hasBlock.block ?: return listOf()
        return listOf(block)
    }

    @JvmStatic
    fun getBlockList(switchStatement: ObjJSwitchStatement) : List<ObjJBlock> {
        val out = ArrayList<ObjJBlock>()
        for (clause in switchStatement.caseClauseList) {
            val block = clause.block ?: continue
            out.add(block)
        }
        return out
    }

    @JvmStatic
    fun getBlockList(ifStatement: ObjJIfStatement): List<ObjJBlock> {
        val out = ArrayList<ObjJBlock>()
        out.addAll(ifStatement.blockElementList)
        for(elseIfBlock in ifStatement.elseIfStatementList) {
            val block = elseIfBlock.block
            if (block != null) {
                out.add(block)
            }
        }
        out.addAll(ifStatement.getChildrenOfType(ObjJBlock::class.java))
        return out
    }

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
        return getBlockList(defineFunction)
    }

    @JvmStatic
    fun getBlock(function: ObjJPreprocessorDefineFunction): ObjJBlock? {
        return function.block
    }

    @JvmStatic
    fun getBlock(expr: ObjJExpr): ObjJBlock? =
            cappuccino.ide.intellij.plugin.psi.utils.getBlock(expr)

    @JvmStatic
    fun getBlock(case:ObjJCaseClause) : ObjJBlock? {
        return case.getChildOfType(ObjJBracketLessBlock::class.java)
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
    fun getFunctionNameNode(functionDec: ObjJPreprocessorDefineFunction): ObjJNamedElement? {
        return functionDec.functionName
    }

    @JvmStatic
    fun getFunctionNameAsString(functionLiteral: ObjJFunctionLiteral): String {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNameAsString(functionLiteral)
    }

    @JvmStatic
    fun getFunctionNamesAsString(functionLiteral: ObjJFunctionLiteral): List<String> {
        return ObjJFunctionDeclarationPsiUtil.getFunctionNamesAsString(functionLiteral)
    }

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
    fun getOpenBrace(@Suppress("UNUSED_PARAMETER") element: PsiElement): PsiElement? = null

    // ============================== //
    // ===== QualifiedReference ===== //
    // ============================== //

    @JvmStatic
    fun getPartsAsString(qualifiedReference: ObjJQualifiedReference): String {
        return (if (qualifiedReference.methodCall != null) "{?}" else "") + getPartsAsString(qualifiedReference.getChildrenOfType(ObjJQualifiedReferenceComponent::class.java))
    }

    @JvmStatic
    fun getPartsAsStringArray(qualifiedReference: ObjJQualifiedReference): List<String> {
        return getPartsAsStringArray(qualifiedReference.getChildrenOfType(ObjJQualifiedReferenceComponent::class.java))
    }

    private fun getPartsAsStringArray(qualifiedNameParts: List<ObjJQualifiedReferenceComponent>?): List<String> {
        if (qualifiedNameParts == null) {
            return emptyList()
        }
        val out = ArrayList<String>()
        for (part in qualifiedNameParts) {
            out.add(if (part.qualifiedNameText != null) part.qualifiedNameText!! else "")
        }
        return out
    }

    private fun getPartsAsString(qualifiedNameParts: List<ObjJQualifiedReferenceComponent>): String {
        return ArrayUtils.join(getPartsAsStringArray(qualifiedNameParts), ".")
    }

    @JvmStatic
    fun getQualifiedNameText(variableName: ObjJVariableName): String {
        return variableName.text
    }

    @JvmStatic
    fun getDescriptiveText(psiElement: PsiElement): String? {
        return when (psiElement) {
            is ObjJSelector -> getSelectorDescriptiveName(psiElement)
            is ObjJVariableName -> psiElement.text
            is ObjJClassName -> getClassDescriptiveText(psiElement)
            is ObjJFunctionName -> psiElement.getText()
            else -> ""
        }
    }

    private fun getClassDescriptiveText(classNameElement: ObjJClassName): String? {
        val classDeclarationElement = classNameElement.getParentOfType(ObjJClassDeclarationElement::class.java)
        var className = classNameElement.text
        if (classDeclarationElement == null || classDeclarationElement.getClassNameString() != className) {
            return className
        }
        if (classDeclarationElement is ObjJImplementationDeclaration) {
            if (classDeclarationElement.categoryName != null) {
                className += " (" + classDeclarationElement.categoryName!!.className.text + ")"
            }
        }
        return className
    }

    private fun getSelectorDescriptiveName(selector: ObjJSelector): String {
        val selectorLiteral = selector.getParentOfType(ObjJSelectorLiteral::class.java)
        if (selectorLiteral != null) {
            return "@selector(" + selectorLiteral.selectorString + ")"
        }
        val variableDeclaration = selector.getParentOfType(ObjJInstanceVariableDeclaration::class.java)
        if (variableDeclaration != null) {
            val property = selector.getParentOfType(ObjJAccessorProperty::class.java)
            val propertyString = if (property != null) property.accessorPropertyType.text + "=" else ""
            val returnType = if (variableDeclaration.stub != null) variableDeclaration.stub.varType else variableDeclaration.formalVariableType.text
            return "- (" + returnType + ") @accessors(" + propertyString + selector.getSelectorString(false) + ")"
        }
        val methodCall = selector.getParentOfType(ObjJMethodCall::class.java)
        var selectorString: String? = null
        if (methodCall != null) {
            selectorString = methodCall.selectorString
        }
        if (selectorString == null) {
            val methodHeader = selector.getParentOfType(ObjJMethodHeaderDeclaration::class.java)
            if (methodHeader != null) {
                selectorString = if (methodHeader is ObjJMethodHeader) getFormattedSelector((methodHeader as ObjJMethodHeader?)!!) else methodHeader.selectorString
                val methodScopeString = if (methodHeader.isStatic) "+" else "-"
                return methodScopeString + " (" + methodHeader.returnType + ")" + selectorString
            }
        }
        selectorString = selectorString ?: selector.getSelectorString(true)
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
        return if (doWhileStatement == null || doWhileStatement.conditionExpression == null) {
            null
        } else doWhileStatement.conditionExpression!!.expr
    }

    // ============================== //
    // =========== Misc ============= //
    // ============================== //

    @JvmStatic
    fun getFileName(element: PsiElement?): String? {
        if (element == null) {
            return null
        }
        return if (element.containingFile == null || element.containingFile.virtualFile == null) {
            null
        } else element.containingFile.virtualFile.name
    }

    @JvmStatic
    fun getConditionalExpression(composite:ObjJConditionalStatement) : ObjJExpr? {
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
    fun createTreeStructureElement(declaration: ObjJImplementationDeclaration): ObjJStructureViewElement {
        val fileName = ObjJFileUtil.getContainingFileName(declaration)
        val presentation: ItemPresentation = when {
            declaration.isCategory -> PresentationData("@category ${declaration.getClassName()} (${declaration.categoryNameString})", fileName, ObjJIcons.CATEGORY_ICON, null)
            declaration.superClassName != null && declaration.superClassName?.isNotEmpty() == true -> PresentationData("@implementation ${declaration.getClassNameString()} : ${declaration.superClassName}", fileName, ObjJIcons.CLASS_ICON, null)
            else -> PresentationData("@implementation ${declaration.getClassNameString()}", fileName, ObjJIcons.CLASS_ICON, null)
        }
        return ObjJStructureViewElement(declaration, presentation, declaration.getClassNameString())
    }

    @JvmStatic
    fun getTreeStructureChildElements(declaration: ObjJImplementationDeclaration): Array<ObjJStructureViewElement> {
        val out: MutableList<ObjJStructureViewElement> = mutableListOf()
        declaration.instanceVariableList?.instanceVariableDeclarationList?.forEach {
            out.add(it.createTreeStructureElement())
        }
        declaration.getChildrenOfType(ObjJHasTreeStructureElement::class.java).forEach {
            out.add(it.createTreeStructureElement())
        }
        return out.toTypedArray()
    }

    @JvmStatic
    fun createTreeStructureElement(instanceVariable: ObjJInstanceVariableDeclaration): ObjJStructureViewElement {
        val label = "ivar: ${instanceVariable.formalVariableType.text} ${instanceVariable.variableName?.text
                ?: "{UNDEF}"}${if (instanceVariable.accessor != null) " @accessors" else ""}"
        val presentation = PresentationData(label, ObjJFileUtil.getContainingFileName(instanceVariable), ObjJIcons.VARIABLE_ICON, null)
        return ObjJStructureViewElement(instanceVariable, presentation, "_" + (instanceVariable.variableName?.text
                ?: "UNDEF"))
    }

    @JvmStatic
    fun createTreeStructureElement(declaration: ObjJProtocolDeclaration): ObjJStructureViewElement {
        val fileName = ObjJFileUtil.getContainingFileName(declaration)
        val presentation: ItemPresentation = PresentationData("@protocol ${declaration.getClassNameString()}", fileName, ObjJIcons.PROTOCOL_ICON, null)
        return ObjJStructureViewElement(declaration, presentation, declaration.getClassNameString())
    }


    @JvmStatic
    fun createTreeStructureElement(header: ObjJProtocolScopedBlock): ObjJStructureViewElement {
        val fileName = ObjJFileUtil.getContainingFileName(header)
        val text = if (header.atOptional != null) "@optional" else "@required"
        return ObjJStructureViewElement(header, PresentationData(text, fileName, null, null), "")
    }

    @JvmStatic
    fun createTreeStructureElement(header: ObjJMethodDeclaration): ObjJStructureViewElement {
        return createTreeStructureElement(header.methodHeader)
    }

    @JvmStatic
    fun createTreeStructureElement(header: ObjJMethodHeader): ObjJStructureViewElement {
        val fileName = ObjJFileUtil.getContainingFileName(header)
        val presentation: ItemPresentation = PresentationData(header.text.replace("[\n\r]*", ""), fileName, ObjJIcons.METHOD_ICON, null)
        return ObjJStructureViewElement(header, presentation, header.containingClassName)
    }

    // ============================== //
    // =========== PARSER =========== //
    // ============================== //

    @JvmStatic
    fun eos(compositeElement: PsiElement?): Boolean {
        if (compositeElement == null) {
            return false
        }
        var ahead = compositeElement.getNextNode()
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

    @JvmStatic
    fun eosToken(ahead: IElementType?, hadLineTerminator: Boolean): Boolean {

        if (ahead == null) {
            //LOGGER.log(Level.INFO, "EOS assumed as ahead == null")
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

    @Suppress("unused")
    @JvmStatic
    fun PsiElement?.hasNodeType(elementType: IElementType): Boolean {
        return this != null && this.node.elementType === elementType
    }

    // ============================== //
    // ======= Presentation ========= //
    // ============================== //

    @JvmStatic
    fun getIcon(element: PsiElement): Icon? {
        if (element is ObjJClassName) {
            val classDeclarationElement = element.getParentOfType(ObjJClassDeclarationElement::class.java)

            val className = element.getText()
            if (classDeclarationElement == null || classDeclarationElement.getClassNameString() != className) {
                return null
            }
            if (classDeclarationElement is ObjJImplementationDeclaration) {
                return if (classDeclarationElement.isCategory) {
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
            if (element.isIn(ObjJMethodHeaderDeclaration::class.java)) {
                return ObjJIcons.METHOD_ICON
            }
            if (element.isIn(ObjJInstanceVariableDeclaration::class.java)) {
                return ObjJIcons.ACCESSOR_ICON
            }
            if (element.isIn(ObjJSelectorLiteral::class.java)) {
                return ObjJIcons.SELECTOR_ICON
            }
        }
        return null
    }


    fun getFormalParameterArgList(functionDeclaration: ObjJFunctionDeclaration): List<ObjJFormalParameterArg> {
        return functionDeclaration.formalParameterList?.formalParameterArgList ?: listOf()
    }

    fun getLastFormalParameterArg(functionDeclaration: ObjJFunctionDeclaration): ObjJLastFormalParameterArg? {
        return functionDeclaration.formalParameterList?.lastFormalParameterArg
    }

    fun getFormalParameterArgList(functionDeclaration: ObjJFunctionLiteral): List<ObjJFormalParameterArg> {
        return functionDeclaration.formalParameterList?.formalParameterArgList ?: listOf()
    }

    fun getLastFormalParameterArg(functionDeclaration: ObjJFunctionLiteral): ObjJLastFormalParameterArg? {
        return functionDeclaration.formalParameterList?.lastFormalParameterArg
    }


    fun getIterationStatementList(block:ObjJBlockElement): List<ObjJIterationStatement> {
        return block.getChildrenOfType(ObjJIterationStatement::class.java)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    @JvmStatic
    fun <PsiT : PsiElement> PsiElement.isIn(parentClass: Class<PsiT>): Boolean {
        return getParentOfType(parentClass) != null
    }

}
