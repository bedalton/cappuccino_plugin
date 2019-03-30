package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.ObjJQualifiedReference
import cappuccino.ide.intellij.plugin.psi.ObjJRightExpr
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
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

    fun getIndexInQualifiedNameParent(variableName: PsiElement?): Int {
        if (variableName == null) {
            return -1
        }
        val qualifiedReferenceParent = variableName.parent as? ObjJQualifiedReference ?: return if (variableName.getParentOfType(ObjJRightExpr::class.java) != null) -1 else 0
        var qualifiedNameIndex:Int = -1
        val parts = qualifiedReferenceParent.variableNameList
        val numParts = parts.size
        for (i in 0..(numParts-1)) {
            val part = parts[i]
            if (variableName.isEquivalentTo(part)) {
                qualifiedNameIndex = i
                break
            }
        }
        if (qualifiedNameIndex < 0) {
            LOGGER.info("Failed to qualified variable $variableName in file ${variableName.containingFile?.name?:"UNDEF"} with $numParts parts in qualified reference")
        }
        if (qualifiedNameIndex > 1) {
            val firstVariable = qualifiedReferenceParent.primaryVar ?: return qualifiedNameIndex
            if (firstVariable.text == "self" || firstVariable.text == "super") {
                qualifiedNameIndex -= 1
            }
        }
        return qualifiedNameIndex
    }


    fun getQualifiedNameParts(qualifiedName:ObjJQualifiedReference) : List<ObjJQualifiedReferenceComponent> {
        return qualifiedName.getChildrenOfType(ObjJQualifiedReferenceComponent::class.java)
    }

}