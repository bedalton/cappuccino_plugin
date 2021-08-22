package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalPsiElement
import com.intellij.psi.PsiElement
import java.util.logging.Logger

object ObjJQualifiedReferenceUtil {

    val LOGGER: Logger by lazy {
        Logger.getLogger("#" + ObjJQualifiedReferenceUtil::class.java.canonicalName)
    }

    /**
     * Gets the last variableName element in a fully qualified name.
     * @param qualifiedReference qualified variable name
     * @return last var name element.
     */
    fun getLastVariableName(qualifiedReference: ObjJQualifiedReference): ObjJVariableName? {
        val variableNames = qualifiedReference.variableNameList
        val lastIndex = variableNames.size - 1
        return if (variableNames.isNotEmpty()) variableNames[lastIndex] else null
    }

    fun getQualifiedNameText(functionName: ObjJFunctionName): String? {
        return functionName.text
    }

    @JvmOverloads
    fun getQualifiedNameAsString(variableName: ObjJVariableName, stopBeforeIndex: Int = -1): String {
        val qualifiedReference = variableName.getParentOfType(ObjJQualifiedReference::class.java)
        return getQualifiedNameAsString(qualifiedReference, variableName.text, stopBeforeIndex) ?: ""
    }

    @JvmOverloads
    fun getQualifiedNameAsString(
        qualifiedReference: ObjJQualifiedReference?,
        defaultValue: String?,
        stopBeforeIndex: Int = -1,
    ): String? {
        if (qualifiedReference == null) {
            return defaultValue
        }
        val variableNames = qualifiedReference.variableNameList
        if (variableNames.isEmpty()) {
            return defaultValue
        }
        val numVariableNames =
            if (stopBeforeIndex != -1 && variableNames.size > stopBeforeIndex) stopBeforeIndex else variableNames.size
        val builder = StringBuilder(variableNames[0].text)
        for (i in 1 until numVariableNames) {
            builder.append(".").append(variableNames[i].text)
        }

        return builder.toString()
    }

    fun resolveQualifiedReferenceVariable(variableName: ObjJUniversalQualifiedReferenceComponent): ObjJVariableName? {
        val formalVariableTypeInstanceVariableList =
            ObjJVariableNameAggregatorUtil.getFormalVariableInstanceVariables(variableName)
                ?: return null
        return formalVariableTypeInstanceVariableList.firstOrNull { variable ->
            variable.text == variableName.text
        }
    }


    /*fun getIndexInQualifiedNameParent(element: PsiElement?): Int {

        if (element == null)
            return 0
        var firstParent:PsiElement? = element.parent
        while (firstParent != null
                && firstParent !is ObjJQualifiedReferencePrime
                && firstParent !is ObjJQualifiedReference
                && firstParent !is ObjJExpr
        ) {
            firstParent = firstParent.parent
        }
        if (firstParent == null || firstParent is ObjJExpr) {
            return 0
        }
        val components = (firstParent as? ObjJQualifiedReference)?.qualifiedNameParts
                ?: (firstParent as? ObjJQualifiedReferencePrime)?.qualifiedNameParts
                ?: emptyList()
        if (components.isEmpty()) {
            return 0
        }

        // Find if element who's parent is this qualified reference.
        // Good for elements like function name, who's direct parent is not qualified reference
        var variableName:PsiElement? = element
        while (variableName != null && variableName !in components) {
            variableName = variableName.parent
        }

        // If qualified reference cannot be found, something has gone wrong.
        // THIS SHOULD NOT HAPPEN
        assert(variableName != null) {
            "Qualified name component failed to find its own parent: ${element.elementType}(${element.text}) in $components"
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
    }*/

    fun getIndexInQualifiedNameParent(elementIn: PsiElement?): Int {
        val element = elementIn?.getSelfOrParentOfType(ObjJUniversalPsiElement::class.java)
            ?: return 0
        val parent: HasQualifiedName<*> = element.getParentOfType(HasQualifiedName::class.java)
            ?: return 0
        val parts = parent.qualifiedNameParts
        val index = if (parts.indexOf(element) >= 0)
            parts.indexOf(element)
        else if (element.parent is ObjJFunctionCall)
            parts.indexOf(element.parent as ObjJFunctionCall)
        else
            0
        return if (index < 0)
            0
        else
            index
    }


    fun getQualifiedNameParts(qualifiedName: ObjJQualifiedReference): List<ObjJUniversalQualifiedReferenceComponent> {
        return qualifiedName.getChildrenOfType(ObjJUniversalQualifiedReferenceComponent::class.java)
    }

    fun getQualifiedNameParts(expr: ObjJExpr): List<ObjJUniversalQualifiedReferenceComponent> {
        if (expr.expr != null)
            return getQualifiedNameParts(expr.expr!!)
        val leftExpr = expr.leftExpr ?: return emptyList()
        val qualifiedReferenceComponents: MutableList<ObjJUniversalQualifiedReferenceComponent> =
            leftExpr.qualifiedReference?.qualifiedNameParts.orEmpty().toMutableList()
        if (qualifiedReferenceComponents.isEmpty()) {
            val first: ObjJUniversalQualifiedReferenceComponent =
                leftExpr.functionCall as? ObjJUniversalQualifiedReferenceComponent
                    ?: leftExpr.methodCall as? ObjJUniversalQualifiedReferenceComponent
                    ?: return emptyList()
            qualifiedReferenceComponents.add(first)
        }
        for (rightExpr in expr.rightExprList) {
            val qualifiedReferencePrime = rightExpr.qualifiedReferencePrime
                ?: return qualifiedReferenceComponents
            qualifiedReferenceComponents.addAll(qualifiedReferencePrime.qualifiedNameParts)
        }
        return qualifiedReferenceComponents
    }

    fun getQualifiedNamePath(expr: ObjJExpr): String {
        return getQualifiedNameParts(expr).joinToString(".")
    }

    fun getQualifiedNameParts(qualifiedName: ObjJQualifiedReferencePrime): List<ObjJUniversalQualifiedReferenceComponent> {
        val leftExpr = qualifiedName.getParentOfType(ObjJExpr::class.java)?.leftExpr
        val leftComponent = leftExpr
            ?.functionCall
            ?: leftExpr?.methodCall
        val rightChildren: List<ObjJUniversalQualifiedReferenceComponent> =
            qualifiedName.getChildrenOfType(ObjJUniversalQualifiedReferenceComponent::class.java)
        if (leftComponent != null)
            return listOf(leftComponent) + rightChildren
        val qualifiedReference = leftExpr?.qualifiedReference?.qualifiedNameParts ?: return emptyList()
        return qualifiedReference + rightChildren
    }

}