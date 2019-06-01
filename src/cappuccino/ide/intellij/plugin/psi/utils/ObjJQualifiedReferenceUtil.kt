package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import com.intellij.psi.PsiElement
import java.util.logging.Logger

object ObjJQualifiedReferenceUtil {

    val LOGGER:Logger by lazy {
        Logger.getLogger("#"+ObjJQualifiedReferenceUtil::class.java.canonicalName)
    }

    /**
     * Gets the last variableName element in a fully qualified name.
     * @param qualifiedReference qualified variable name
     * @return last var name element.
     */
    fun getLastVariableName(qualifiedReference: ObjJQualifiedReference): ObjJVariableName? {
        val variableNames = qualifiedReference.variableNameList
        val lastIndex = variableNames.size - 1
        return if (!variableNames.isEmpty()) variableNames[lastIndex] else null
    }

    fun getQualifiedNameText(functionName:ObjJFunctionName) : String? {
        return functionName.text
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
        return builder.toString()
    }

    fun resolveQualifiedReferenceVariable(variableName:ObjJVariableName) : ObjJVariableName? {
        val formalVariableTypeInstanceVariableList = ObjJVariableNameAggregatorUtil.getFormalVariableInstanceVariables(variableName)
                ?: return null
        return formalVariableTypeInstanceVariableList.firstOrNull { variable ->
            variable.text == variableName.text
        }
    }

    fun getIndexInQualifiedNameParent(variableNameIn: PsiElement?): Int {

        if (variableNameIn == null)
            return 0

        val qualifiedReferencePrime = variableNameIn.getParentOfType(ObjJQualifiedReferencePrime::class.java)
        val components = if (qualifiedReferencePrime != null)
            qualifiedReferencePrime.qualifiedNameParts
        else
            variableNameIn.getParentOfType(ObjJQualifiedReference::class.java)?.qualifiedNameParts ?: return 0

        if (components.isEmpty()) {
            return 0
        }

        // Find if element who's parent is this qualified reference.
        // Good for elements like function name, who's direct parent is not qualified reference
        var variableName:PsiElement? = variableNameIn
        while (variableName != null && variableName !in components) {
            variableName = variableName.parent
        }

        // If qualified reference cannot be found, something has gone wrong.
        // THIS SHOULD NOT HAPPEN
        assert(variableName != null) {
            "Qualified name component failed to find its own parent: ${variableNameIn.elementType}(${variableNameIn.text}) in ${components}"
        }
        if (variableName == null) {
            LOGGER.severe("Qualified name component failed to find its own parent")
            return -1
        }
        var qualifiedNameIndex:Int = -1
        val numParts = components.size
        for (i in 0 until numParts) {
            val part = components[i]
            if (variableName.isEquivalentTo(part)) {
                qualifiedNameIndex = i
                break
            }
        }
        if (qualifiedNameIndex < 0) {
            //LOGGER.info("Failed to qualified variable ${variableName.text} in file ${variableName.containingFile?.name?:"UNDEF"} with $numParts parts in qualified reference")
        }
        if (qualifiedNameIndex > 1) {
            val firstVariable = (components.first() as? ObjJVariableName) ?: return qualifiedNameIndex
            if (firstVariable.text == "self" || firstVariable.text == "super") {
                qualifiedNameIndex -= 1
            }
        }
        return qualifiedNameIndex
    }


    fun getQualifiedNameParts(qualifiedName:ObjJQualifiedReference) : List<ObjJQualifiedReferenceComponent> {
        return qualifiedName.getChildrenOfType(ObjJQualifiedReferenceComponent::class.java)
    }

    fun getQualifiedNameParts(qualifiedName:ObjJQualifiedReferencePrime) : List<ObjJQualifiedReferenceComponent> {
        val leftExpr = qualifiedName.getParentOfType(ObjJExpr::class.java)?.leftExpr
        val first:List<ObjJQualifiedReferenceComponent> = if (leftExpr?.functionCall != null) listOf(leftExpr.functionCall!!) else leftExpr?.qualifiedReference?.qualifiedNameParts ?: return emptyList()
        val after = qualifiedName.getChildrenOfType(ObjJQualifiedReferenceComponent::class.java)
        return first + after
    }

}