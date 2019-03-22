package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.contributor.ObjJBlanketCompletionProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.indices.ObjJSelectorInferredMethodIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import org.jetbrains.annotations.Contract

import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.UNDETERMINED
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.AT_ACTION
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.VOID_CLASS_NAME
import cappuccino.ide.intellij.plugin.utils.ArrayUtils.EMPTY_STRING_ARRAY
import com.intellij.openapi.progress.ProgressIndicatorProvider
import java.util.regex.Pattern
import kotlin.collections.ArrayList

@Suppress("UNUSED_PARAMETER")
object ObjJMethodPsiUtils {
    const val SELECTOR_SYMBOL = ":"
    val EMPTY_SELECTOR = getSelectorString("{EMPTY}")

    @Contract("null -> !null")
    fun getParamTypes(declarationSelectors: List<ObjJMethodDeclarationSelector>?): List<ObjJFormalVariableType?> {
        if (declarationSelectors == null || declarationSelectors.isEmpty()) {
            return emptyList()
        }
        val out = ArrayList<ObjJFormalVariableType?>()
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
            out.add(if (selector.varType != null) selector.varType!!.text else "")
        }
        return out
    }

    fun getSelectorIndex(selector:ObjJSelector) : Int {
        val method : ObjJMethodHeaderDeclaration<*> = selector.getParentOfType(ObjJMethodHeaderDeclaration::class.java)
                ?: return 0
        return getSelectorIndex(method, selector) ?: 0
    }

    private fun getSelectorIndex(methodHeader:ObjJMethodHeaderDeclaration<*>?, selector:ObjJSelector) : Int? {
        if (methodHeader == null) {
            return null
        }
        val selectors = methodHeader.selectorList
        val index = selectors.indexOf(selector)
        if (index >= 0) {
            return index
        }
        val numSelectors = selectors.size
        for (i in 0..numSelectors) {
            if (selector equals selectors[i]) {
                return i
            }
        }
        return null
    }


    @JvmStatic
    fun getSelectorList(methodHeader:ObjJMethodHeader): List<ObjJSelector?> {
        val out:MutableList<ObjJSelector?> = ArrayList()
        methodHeader.methodDeclarationSelectorList.forEach { selector ->
            out.add(selector.selector)
        }
        return out
    }


    fun getSelectorLiteralReference(hasSelectorElement: ObjJHasMethodSelector): ObjJSelectorLiteral? {
        val containingClassName = hasSelectorElement.containingClassName
        //ProgressIndicatorProvider.checkCanceled();
        if (DumbService.getInstance(hasSelectorElement.project).isDumb) {
            return null
        }
        for (selectorLiteral in ObjJSelectorInferredMethodIndex.instance[containingClassName, hasSelectorElement.project]) {
            if (selectorLiteral.containingClassName == containingClassName) {
                return selectorLiteral
            }
        }
        return null
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
        val subSelectorPattern = if (subSelector != null) Pattern.compile(subSelector.replace(ObjJBlanketCompletionProvider.CARET_INDICATOR, "(.*)")) else null
        for (currentSelector in selectorList) {

            ProgressIndicatorProvider.checkCanceled()
            if (subSelectorPattern == null || subSelectorPattern.matcher(currentSelector.getSelectorString(false)).matches()) {
                return currentSelector
            }
        }
        //LOGGER.log(Level.WARNING, "Failed to find selector matching <"+subSelector+"> or any selector before foldingDescriptors of <"+selectorList.size()+"> selectors");
        return null
    }

    // ============================== //
    // ======== Return Type ========= //
    // ============================== //


    fun getReturnType(methodHeader: ObjJMethodHeader, follow: Boolean): String {
        if (methodHeader.stub != null) {
            return methodHeader.stub!!.returnTypeAsString
        }
        val returnTypeElement = methodHeader.methodHeaderReturnTypeElement ?: return ObjJClassType.UNDETERMINED
        if (returnTypeElement.formalVariableType.atAction != null) {
            return AT_ACTION
        }
        if (returnTypeElement.formalVariableType.void != null) {
            return VOID_CLASS_NAME
        }
        val formalVariableType = returnTypeElement.formalVariableType
        if (formalVariableType.varTypeId != null) {
            if (follow) {
            //LOGGER.log(Level.INFO, "Found return type id to be: <"+returnType+">");
                return formalVariableType.varTypeId!!.getIdType(false)
            }
        }
        return formalVariableType.text
    }

    @JvmOverloads
    fun getIdReturnType(varTypeId: ObjJVarTypeId, follow: Boolean = true): String {
        if (varTypeId.stub != null) {
            val stub = varTypeId.stub
            if (!isUniversalMethodCaller(stub.idType) && stub.idType != "id") {
                //return stub.getIdType();
            }
        }
        if (varTypeId.className != null) {
            return varTypeId.className!!.text
        }
        if (varTypeId.getParentOfType(ObjJMethodDeclaration::class.java) == null)
                return ObjJClassType.ID
        var returnType: String?
        returnType = try {
            ObjJClassType.ID//getReturnTypeFromReturnStatements(declaration, follow)
        } catch (e: MixedReturnTypeException) {
            e.returnTypesList[0]
        }

        if (returnType == ObjJClassType.UNDETERMINED) {
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


    fun getReturnType(accessorProperty: ObjJAccessorProperty): String {
        if (accessorProperty.stub != null) {
            return accessorProperty.stub!!.returnTypeAsString
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

    fun findSelectorMatching(method: ObjJHasMethodSelector, selectorString: String): ObjJSelector? {
        for (selectorOb in method.selectorList) {
            @Suppress("USELESS_IS_CHECK")
            if (selectorOb !is ObjJSelector) {
                continue
            }
            if (selectorOb.getSelectorString(false) == selectorString || selectorOb.getSelectorString(true) == selectorString) {
                return selectorOb
            }
        }
        return null
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
        val stub = methodHeader.stub
        return if (stub != null) {
            if (stub.isStatic) MethodScope.STATIC else MethodScope.INSTANCE
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
            hasMethodSelector.stub?.isStatic ?: getMethodScope(hasMethodSelector) == MethodScope.STATIC
        } else false
    }

    fun isRequired(methodHeader: ObjJMethodHeader) =
            methodHeader.getParentOfType(ObjJProtocolScopedMethodBlock::class.java)?.atOptional == null

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
    enum class MethodScope(private val scopeMarker: String?) {
        STATIC("+"),
        INSTANCE("-"),
        INVALID(null);


        companion object {

            fun getScope(scopeMarker: String): MethodScope {
                return when (scopeMarker) {
                    STATIC.scopeMarker -> STATIC
                    INSTANCE.scopeMarker -> INSTANCE
                    else -> INVALID
                }
            }
        }

    }

}
