package org.cappuccino_project.ide.intellij.plugin.psi.utils

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJSelectorInferredMethodIndex
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJVarTypeIdStub
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils
import org.jetbrains.annotations.Contract

import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType.*
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils.EMPTY_STRING_ARRAY

object ObjJMethodPsiUtils {

    private val LOGGER = Logger.getLogger(ObjJMethodPsiUtils::class.java.canonicalName)
    val SELECTOR_SYMBOL = ":"
    val EMPTY_SELECTOR = getSelectorString("{EMPTY}")
    val ALLOC_SELECTOR = getSelectorString("alloc")


    @Contract(pure = true)
    fun getSelectorString(selector: String?): String {
        return (selector ?: "") + SELECTOR_SYMBOL
    }

    fun getSelectorStringFromSelectorStrings(selectors: List<String>): String {
        return ArrayUtils.join(selectors, SELECTOR_SYMBOL, true)
    }

    fun getSelectorStringFromSelectorList(selectors: List<ObjJSelector>): String {
        return getSelectorStringFromSelectorStrings(getSelectorStringsFromSelectorList(selectors))
    }

    fun getSelectorElementsFromMethodDeclarationSelectorList(declarationSelectors: List<ObjJMethodDeclarationSelector>?): List<ObjJSelector> {
        if (declarationSelectors == null || declarationSelectors.isEmpty()) {
            return emptyList()
        }
        val out = ArrayList<ObjJSelector>()
        for (selector in declarationSelectors) {
            out.add(selector.selector)
        }
        return out
    }

    @Contract("null -> !null")
    fun getParamTypes(declarationSelectors: List<ObjJMethodDeclarationSelector>?): List<ObjJFormalVariableType> {
        if (declarationSelectors == null || declarationSelectors.isEmpty()) {
            return emptyList()
        }
        val out = ArrayList<ObjJFormalVariableType>()
        for (selector in declarationSelectors) {
            out.add(selector.varType)
        }
        return out
    }


    @Contract("null -> !null")
    fun getParamTypesAsString(declarationSelectors: List<ObjJMethodDeclarationSelector>?): List<String> {
        if (declarationSelectors == null || declarationSelectors.isEmpty()) {
            return EMPTY_STRING_ARRAY
        }
        val out = ArrayList<String>()
        for (selector in declarationSelectors) {
            out.add(if (selector.varType != null) selector.varType!!.text else null)
        }
        return out
    }

    fun getSelectorStringsFromSelectorList(selectorElements: List<ObjJSelector>): List<String> {
        if (selectorElements.isEmpty()) {
            return EMPTY_STRING_ARRAY
        }
        val selectorStrings = ArrayList<String>()
        for (selector in selectorElements) {
            selectorStrings.add(getSelectorString(selector, false))
        }
        return selectorStrings
    }

    private fun getSelectorStringsFromMethodDeclarationSelectorList(
            selectorElements: List<ObjJMethodDeclarationSelector>): List<String> {
        if (selectorElements.isEmpty()) {
            return EMPTY_STRING_ARRAY
        }
        val selectorStrings = ArrayList<String>()
        for (selectorElement in selectorElements) {
            selectorStrings.add(getSelectorString(selectorElement, false))
        }
        return selectorStrings
    }

    fun getSelectorString(selectorElement: ObjJMethodDeclarationSelector?, addSuffix: Boolean): String {
        val selector = selectorElement?.selector
        return getSelectorString(selector, addSuffix)
    }

    @Contract(pure = true)
    fun getSelectorString(selectorElement: ObjJSelector?, addSuffix: Boolean): String {
        val selector = if (selectorElement != null) selectorElement.text else ""
        return if (addSuffix) {
            selector + SELECTOR_SYMBOL
        } else {
            selector
        }
    }

    fun getSelectorString(selectorLiteral: ObjJSelectorLiteral): String {
        return if (selectorLiteral.stub != null) {
            selectorLiteral.stub.selectorString
        } else getSelectorStringFromSelectorStrings(selectorLiteral.selectorStrings)
    }


    fun getSelectorUntil(targetSelectorElement: ObjJSelector, include: Boolean): String? {
        val parent = ObjJTreeUtil.getParentOfType(targetSelectorElement, ObjJHasMethodSelector::class.java)
                ?: return null

        val builder = StringBuilder()
        val selectors = parent.selectorList
        for (subSelector in selectors) {
            if (subSelector === targetSelectorElement) {
                if (include) {
                    builder.append(subSelector.getSelectorString(true))
                }
                break
            } else {
                builder.append(ObjJMethodPsiUtils.getSelectorString(subSelector, true))
            }
        }
        return builder.toString()
    }

    /**
     * Gets all selector sibling selector strings after the given index
     * @param selector base selector
     * @param selectorIndex selector index
     * @return list of trailing sibling selectors as strings
     */
    fun getTrailingSelectorStrings(selector: ObjJSelector, selectorIndex: Int): List<String> {
        val methodHeaderDeclaration = ObjJTreeUtil.getParentOfType(selector, ObjJMethodHeaderDeclaration<*>::class.java)
        val temporarySelectorsList = methodHeaderDeclaration?.selectorStrings ?: EMPTY_STRING_ARRAY
        val numSelectors = temporarySelectorsList.size
        return if (numSelectors > selectorIndex) temporarySelectorsList.subList(selectorIndex + 1, numSelectors) else Collections.EMPTY_LIST
    }


    fun getSelectorLiteralReference(hasSelectorElement: ObjJHasMethodSelector): ObjJSelectorLiteral? {
        val containingClassName = hasSelectorElement.containingClassName
        //ProgressIndicatorProvider.checkCanceled();
        if (DumbService.getInstance(hasSelectorElement.project).isDumb) {
            return null
        }
        for (selectorLiteral in ObjJSelectorInferredMethodIndex.instance.get(containingClassName, hasSelectorElement.project)) {
            if (selectorLiteral.containingClassName == containingClassName) {
                return selectorLiteral
            }
        }
        return null
    }

    // ============================== //
    // ======== Return Type ========= //
    // ============================== //


    fun getReturnType(methodHeader: ObjJMethodHeader, follow: Boolean): String {
        if (methodHeader.stub != null) {
            return methodHeader.stub.returnTypeAsString
        }
        val returnTypeElement = methodHeader.methodHeaderReturnTypeElement ?: return UNDETERMINED
        if (returnTypeElement.atAction != null) {
            return AT_ACTION
        }
        if (returnTypeElement.formalVariableType != null) {
            val formalVariableType = returnTypeElement.formalVariableType
            if (formalVariableType!!.varTypeId != null) {
                if (follow) {
//LOGGER.log(Level.INFO, "Found return type id to be: <"+returnType+">");
                    return formalVariableType.varTypeId!!.getIdType(false)
                }
            }
            return formalVariableType.text
        }
        return if (returnTypeElement.void != null) {
            VOID_CLASS_NAME
        } else UNDETERMINED
    }

    @JvmOverloads
    fun getIdReturnType(varTypeId: ObjJVarTypeId, follow: Boolean = true): String {
        if (varTypeId.stub != null) {
            val stub = varTypeId.stub
            if (!ObjJMethodCallPsiUtil.isUniversalMethodCaller(stub.idType) && stub.idType != null && stub.idType != "id") {
                //return stub.getIdType();
            }
        }
        if (varTypeId.className != null) {
            return varTypeId.className!!.text
        }
        val declaration = varTypeId.getParentOfType(ObjJMethodDeclaration::class.java)
                ?: //LOGGER.log(Level.INFO, "VarTypeId: Not Contained in a method declaration");
                return ObjJClassType.ID
        var returnType: String?
        try {
            returnType = getReturnTypeFromReturnStatements(declaration, follow)
        } catch (e: ObjJExpressionReturnTypeUtil.MixedReturnTypeException) {
            returnType = e.returnTypesList[0]
        }

        if (returnType == UNDETERMINED) {
            returnType = null
        }
        /*
        if (returnType != null) {
            LOGGER.log(Level.INFO, !returnType.equals("id") ? "VarTypeId: id <" + returnType + ">" : "VarTypeId: failed to infer var type");
        } else {
            LOGGER.log(Level.INFO, "VarTypeId: getTypeFromReturnStatements returned null");
        }*/
        return returnType ?: varTypeId.text
    }

    @Throws(ObjJExpressionReturnTypeUtil.MixedReturnTypeException::class)
    private fun getReturnTypeFromReturnStatements(declaration: ObjJMethodDeclaration, follow: Boolean): String? {
        var returnType: String?
        val returnTypes = ArrayList<String>()
        val returnStatements = ObjJBlockPsiUtil.getBlockChildrenOfType(declaration.block, ObjJReturnStatement::class.java, true)
        if (returnStatements.isEmpty()) {
            //LOGGER.log(Level.INFO, "Cannot get return type from return statements, as no return statements exist");
            return null
        } else {
            //LOGGER.log(Level.INFO, "Found <"+returnStatements.size()+"> return statements");
        }
        for (returnStatement in returnStatements) {
            if (returnStatement.expr == null) {
                continue
            }
            returnType = ObjJExpressionReturnTypeUtil.getReturnType(returnStatement.expr, follow)
            if (returnType == null) {
                continue
            }
            if (returnType == ObjJClassType.UNDETERMINED) {
                continue
            }
            if (returnTypes.contains(returnType)) {
                return returnType
            }
            returnTypes.add(returnType)
        }
        if (returnTypes.size == 1) {
            return returnTypes[0]
        }
        return if (returnTypes.size > 1) {
            //LOGGER.log(Level.INFO, "Found more than one possible return type");
            ArrayUtils.join(returnTypes)
        } else UNDETERMINED
    }

    fun getReturnType(accessorProperty: ObjJAccessorProperty): String {
        if (accessorProperty.stub != null) {
            return accessorProperty.stub.returnTypeAsString
        }
        val variableType = accessorProperty.varType
        return variableType ?: UNDETERMINED
    }

    fun getReturnType(
            methodHeader: ObjJSelectorLiteral): String {
        return UNDETERMINED
    }

    // ============================== //
    // ===== Selector Functions ===== //
    // ============================== //
    fun getSelectorStrings(methodHeader: ObjJMethodHeader): List<String> {
        return if (methodHeader.stub != null) {
            methodHeader.stub.selectorStrings
        } else ObjJMethodPsiUtils.getSelectorStringsFromMethodDeclarationSelectorList(methodHeader.methodDeclarationSelectorList)
    }


    fun findSelectorMatching(method: ObjJHasMethodSelector, selectorString: String): ObjJSelector? {
        for (selectorOb in method.selectorList) {
            if (selectorOb !is ObjJSelector) {
                continue
            }
            if (selectorOb.getSelectorString(false) == selectorString || selectorOb.getSelectorString(true) == selectorString) {
                return selectorOb
            }
        }
        return null
    }

    fun getSelectorString(methodHeader: ObjJMethodHeader): String {
        return if (methodHeader.stub != null) {
            methodHeader.stub.selectorString
        } else ObjJMethodPsiUtils.getSelectorStringFromSelectorStrings(getSelectorStrings(methodHeader))
    }

    fun setName(selectorElement: ObjJSelector, newSelectorValue: String): PsiElement {
        val newSelector = ObjJElementFactory.createSelector(selectorElement.project, newSelectorValue)
                ?: return selectorElement
        return selectorElement.replace(newSelector)
    }

    fun getName(methodHeader: ObjJMethodHeader): String {
        return getSelectorString(methodHeader)
    }

    @Throws(IncorrectOperationException::class)
    fun setName(header: ObjJHasMethodSelector, name: String): PsiElement {
        val copy = header.copy() as ObjJHasMethodSelector
        val newSelectors = name.split(ObjJMethodPsiUtils.SELECTOR_SYMBOL.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val selectorElements = copy.selectorList
        if (newSelectors.size != selectorElements.size) {
            throw AssertionError("Selector lists invalid for rename")
        }
        for (i in newSelectors.indices) {
            val selectorString = newSelectors[i]
            val selectorElement = selectorElements[i]
            if (selectorString == selectorElement.text) {
                continue
            }
            setName(selectorElement, selectorString)
        }
        return copy
    }


    fun getNameIdentifier(selector: ObjJSelector): PsiElement {
        return selector
    }

    fun getRangeInElement(selector: ObjJSelector): TextRange {
        //LOGGER.log(Level.INFO,"Getting selector range for full selector text of <"+selector.getText()+">");
        return selector.textRange
    }

    fun getName(selector: ObjJSelector): String {
        return selector.text
    }

    fun getVarType(selector: ObjJMethodDeclarationSelector): ObjJFormalVariableType? {
        return selector.formalVariableType
    }

    fun getMethodScope(methodHeader: ObjJMethodHeader): MethodScope {
        return if (methodHeader.stub != null) {
            if (methodHeader.stub.isStatic) MethodScope.STATIC else MethodScope.INSTANCE
        } else MethodScope.getScope(methodHeader.methodScopeMarker.text)
    }

    fun getMethodScope(
            accessorProperty: ObjJAccessorProperty): MethodScope {
        return MethodScope.INSTANCE
    }

    fun getMethodScope(
            literal: ObjJSelectorLiteral): MethodScope {
        return MethodScope.INSTANCE
    }

    fun isStatic(hasMethodSelector: ObjJHasMethodSelector): Boolean {
        return if (hasMethodSelector is ObjJMethodHeader) {
            if (hasMethodSelector.stub != null) hasMethodSelector.stub.isStatic else getMethodScope(hasMethodSelector) == MethodScope.STATIC
        } else false
    }


    fun isClassMethod(methodHeader: ObjJMethodHeaderDeclaration<*>, possibleClasses: List<String>): Boolean {
        if (possibleClasses.isEmpty() || possibleClasses.contains(ObjJClassType.UNDETERMINED)) {
            return true
        }
        val methodContainingClass = methodHeader.containingClassName
        for (className in possibleClasses) {
            if (possibleClasses.contains(methodContainingClass) || ObjJHasContainingClassPsiUtil.isSimilarClass(methodContainingClass, className)) {
                return true
            }
        }
        return false
    }


    fun methodRequired(methodHeader: ObjJMethodHeader): Boolean {
        val scopedBlock = ObjJTreeUtil.getParentOfType(methodHeader, ObjJProtocolScopedBlock::class.java)
        return scopedBlock == null || scopedBlock.atOptional == null
    }

    fun getHeaderVariableNameMatching(methodHeader: ObjJMethodHeader?, variableName: String): ObjJVariableName? {
        if (methodHeader == null) {
            return null
        }
        for (selector in methodHeader.methodDeclarationSelectorList) {
            if (selector.variableName != null && selector.variableName!!.text == variableName) {
                return selector.variableName
            }
        }
        return null
    }


    /**
     * Method scope enum.
     * Flags method as either static or instance
     */
    enum class MethodScope private constructor(private val scopeMarker: String) {
        STATIC("+"),
        INSTANCE("-"),
        INVALID(null);


        companion object {

            fun getScope(scopeMarker: String): MethodScope {
                return if (scopeMarker == STATIC.scopeMarker) {
                    STATIC
                } else if (scopeMarker == INSTANCE.scopeMarker) {
                    INSTANCE
                } else {
                    INVALID
                }
            }
        }

    }

}
