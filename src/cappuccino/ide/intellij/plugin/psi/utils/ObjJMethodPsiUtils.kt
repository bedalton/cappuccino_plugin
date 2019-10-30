package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.contributor.ObjJBlanketCompletionProvider
import cappuccino.ide.intellij.plugin.indices.ObjJSelectorInferredMethodIndex
import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.AT_ACTION
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.UNDETERMINED
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.VOID_CLASS_NAME
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJSelectorStruct
import cappuccino.ide.intellij.plugin.stubs.stucts.toSelectorStruct
import cappuccino.ide.intellij.plugin.utils.ArrayUtils.EMPTY_STRING_ARRAY
import cappuccino.ide.intellij.plugin.utils.stripRefSuffixes
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import org.jetbrains.annotations.Contract
import java.util.regex.Pattern

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

    private fun getSelectorIndex(methodHeader: ObjJMethodHeaderDeclaration<*>?, selector:ObjJSelector) : Int? {
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
        ////LOGGER.info("Getting thisOrPreviousNonNullSelector: from element of type: <"+hasMethodSelector.getNode().getElementType().toString() + "> with text: <"+hasMethodSelector.getText()+"> ");//declared in <" + getFileNameString(hasMethodSelector)+">");
        val selectorList = hasMethodSelector.selectorList
        ////LOGGER.info("Got selector list.");
        if (selectorList.isEmpty()) {
            ////LOGGER.warning("Cannot get this or previous non null selector when selector list is empty");
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
        ////LOGGER.warning("Failed to find selector matching <"+subSelector+"> or any selector before foldingDescriptors of <"+selectorList.size()+"> selectors");
        return null
    }

    // ============================== //
    // ======== Return Type ========= //
    // ============================== //

    fun getExplicitReturnType(methodHeader: ObjJMethodHeader, follow:Boolean) : String {
        val stubHeaderType = methodHeader.stub?.explicitReturnType
        if (stubHeaderType != null)
            return stubHeaderType
        return methodHeader.methodHeaderReturnTypeElement?.formalVariableType?.varTypeId?.className?.text
                ?: methodHeader.methodHeaderReturnTypeElement?.text
                ?: UNDETERMINED
    }

    fun getReturnTypes(methodHeader: ObjJMethodHeader, follow: Boolean, tag:Long): Set<String> {
        return methodHeader.getCachedInferredTypes(tag) {
            if (methodHeader.tagged(tag))
                return@getCachedInferredTypes null
            val commentReturnTypes = methodHeader.docComment?.getReturnTypes(methodHeader.project).orEmpty().withoutAnyType()
            if (commentReturnTypes.isNotEmpty()) {
                return@getCachedInferredTypes commentReturnTypes.toInferenceResult()
            }
            val returnTypes = internalGetReturnTypes(methodHeader, follow, tag)
            if (returnTypes.isEmpty())
                return@getCachedInferredTypes null
            InferenceResult(types = returnTypes.toJsTypeList())
        }?.toClassList().orEmpty()
    }

    private fun internalGetReturnTypes(methodHeader: ObjJMethodHeader, follow: Boolean, tag:Long): Set<String> {
        val returnTypeElement = methodHeader.methodHeaderReturnTypeElement ?: return setOf(UNDETERMINED)
        if (returnTypeElement.formalVariableType.atAction != null) {
            return setOf(AT_ACTION)
        }
        if (returnTypeElement.formalVariableType.void != null) {
            return setOf(VOID_CLASS_NAME)
        }
        val formalVariableType = returnTypeElement.formalVariableType
        if (formalVariableType.varTypeId != null) {
            if (formalVariableType.varTypeId?.className != null)
                return setOf(formalVariableType.varTypeId?.className!!.text)
            if (follow) {
                return getReturnTypesFromStatements(methodHeader, tag)
            }
        }
        return setOf(formalVariableType.text.stripRefSuffixes())
    }

    private fun getReturnTypesFromStatements(methodHeader: ObjJMethodHeader, tag:Long) : Set<String> {
        val expressions = methodHeader
                .getParentOfType(ObjJMethodDeclaration::class.java)
                ?.methodBlock
                ?.getBlockChildrenOfType(ObjJReturnStatement::class.java, true)
                ?.mapNotNull { it.expr } ?: emptyList()
        val selfExpressionTypes = expressions.filter { it.text == "self"}.mapNotNull { (it.getParentOfType(ObjJHasContainingClass::class.java)?.containingClassName)}
        val superExpressionTypes = expressions.filter { it.text == "super"}.mapNotNull { (it.getParentOfType(ObjJHasContainingClass::class.java)?.getContainingSuperClass()?.text)}
        val simpleOut = selfExpressionTypes + superExpressionTypes
        if (simpleOut.isNotEmpty()) {
            return InferenceResult(types = simpleOut.toJsTypeList()).toClassList()
        }
        var out = INFERRED_EMPTY_TYPE
        expressions.forEach {
            //LOGGER.info("Checking return statement <${it.text ?: "_"}> for method call : <${methodHeader.text}>")
            val type = inferExpressionType(it, tag)
            if (type != null)
                out += type
        }
        return out.toClassList()
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

        if (returnType == UNDETERMINED) {
            returnType = null
        }
        /*
        if (returnType != null) {
            //LOGGER.info(!returnType.equals("id") ? "VarTypeId: id <" + returnType + ">" : "VarTypeId: failed to infer var type");
        } else {
            //LOGGER.info("VarTypeId: getTypeFromReturnStatements returned null");
        }*/
        return returnType ?: varTypeId.text
    }


    fun getExplicitReturnType(accessorProperty: ObjJAccessorProperty): String {
        val stubReturnType = accessorProperty.stub?.varType
        if (stubReturnType != null) {
            return stubReturnType
        }
        val variableType = accessorProperty.varType
        return variableType ?: UNDETERMINED
    }

    fun getExplicitReturnType(
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
        val newSelectors = name.split(SELECTOR_SYMBOL.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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
        ////LOGGER.info("Getting selector range for full selector text of <"+selector.getText()+">");
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

    // ============================== //
    // ========== Structs =========== //
    // ============================== //
    fun getSelectorStructs(header:ObjJMethodHeader) : List<ObjJSelectorStruct> {
        return header.stub?.selectorStructs ?: header.methodDeclarationSelectorList.map {
            it.toSelectorStruct()
        }
    }

    fun getSelectorStructs(selectorLiteral:ObjJSelectorLiteral) : List<ObjJSelectorStruct> {
        return selectorLiteral.stub?.selectorStructs ?: selectorLiteral.selectorList.map {
            ObjJSelectorStruct(
                    selector = it.getSelectorString(false),
                    variableType = null,
                    variableName = null,
                    hasColon = it.getNextNonEmptyNode(true)?.elementType == ObjJTypes.ObjJ_COLON,
                    containerName = selectorLiteral.containingFileName,
                    isContainerAClass = false
            )
        }
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
     * Determines whether two getMethods in the same class are truly different.
     * This is due to overlaps of static and instnace method selectors
     * And also with single selector getMethods where one has a parameter and the other does not
     */
    fun hasSimilarDisposition(thisHeader: ObjJMethodHeader, otherHeader:ObjJMethodHeader?) : Boolean
    {
        // If one method is static, while another is an instance method, ignore
        if (thisHeader.methodScope != otherHeader?.methodScope) {
            return false
        }
        // If Selector lengths are greater than one, then they are indeed overriding duplicated
        // Only single selector method headers can be different with same selectors
        // If one has a parameter and the other does not
        if (thisHeader.selectorList.size > 1) {
            return true
        }
        val thisSelector = thisHeader.methodDeclarationSelectorList.getOrNull(0) ?: return false
        val otherSelector = otherHeader.methodDeclarationSelectorList.getOrNull(0) ?: return false

        // Return different if one selector has a parameter, and the other does not
        return  (thisSelector.methodHeaderSelectorFormalVariableType == null && otherSelector.methodHeaderSelectorFormalVariableType == null) ||
                (thisSelector.methodHeaderSelectorFormalVariableType != null && otherSelector.methodHeaderSelectorFormalVariableType != null)
    }


    /**
     * Method scope enum.
     * Flags method as either static or instance
     */
    enum class MethodScope(val scopeMarker: String?) {
        STATIC("+"),
        INSTANCE("-"),
        SELECTOR_LITERAL("::"),
        ANY(null);

        companion object {

            fun getScope(scopeMarker: String): MethodScope {
                return when (scopeMarker) {
                    STATIC.scopeMarker -> STATIC
                    INSTANCE.scopeMarker -> INSTANCE
                    SELECTOR_LITERAL.scopeMarker -> SELECTOR_LITERAL
                    else -> ANY
                }
            }
        }

        val hasLocalScope
            get() = this != STATIC

    }

}
