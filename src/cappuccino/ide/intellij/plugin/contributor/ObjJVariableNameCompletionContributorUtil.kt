package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.utils.ObjJQualifiedReferenceUtil.getIndexInQualifiedNameParent
import cappuccino.ide.intellij.plugin.psi.utils.ObjJQualifiedReferenceUtil.getQualifiedNameAsString
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameAggregatorUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameAggregatorUtil.getPrecedingVariableAssignmentNameElements
import com.intellij.psi.PsiElement
import java.util.regex.Pattern

object ObjJVariableNameCompletionContributorUtil {

    fun getVariableNameCompletions(variableName: ObjJVariableName?): List<ObjJVariableName> {
        if (variableName?.text.isNullOrBlank()) {
            return emptyList()
        }

        //Initialize variable name array
        val out = getInstanceVariableCompletion(variableName).orEmpty()
        return out + addAllVariableNameElementsByName(variableName)
    }

    private fun getInstanceVariableCompletion(variableName: ObjJVariableName?) : List<ObjJVariableName>? {
        if (variableName == null) {
            return null
        }
        return ObjJVariableNameAggregatorUtil.getFormalVariableInstanceVariables(variableName)
    }

    private fun addAllVariableNameElementsByName(variableName: ObjJVariableName?) : List<ObjJVariableName> {
        if (variableName == null) {
            return emptyList()
        }
        //Get variable name regex pattern
        val variableNamePattern = getQualifiedNameAsString(variableName).toIndexPatternString()
        val pattern = Pattern.compile(variableNamePattern)

        //Get Qualified name reference for completion
        val qualifiedNameIndex = getIndexInQualifiedNameParent(variableName)
        val rawCompletionElements = getPrecedingVariableAssignmentNameElements(variableName, qualifiedNameIndex)

        //Parse variable names to string
        return rawCompletionElements.filter {variable ->
            pattern.matcher(getQualifiedNameAsString(variable)).matches()
        }
    }


    fun getAllVariableNameElementsByName(variableName:PsiElement?) : List<ObjJVariableName> {
        if (variableName == null) {
            return emptyList()
        }
        //Get variable name regex pattern
        val variableNamePattern = variableName.text.toIndexPatternString()
        val pattern = Pattern.compile(variableNamePattern)

        //Get Qualified name reference for completion
        val qualifiedNameIndex = (variableName as? ObjJQualifiedReferenceComponent)?.indexInQualifiedReference ?: 0
        val rawCompletionElements = getPrecedingVariableAssignmentNameElements(variableName, qualifiedNameIndex)

        //Parse variable names to string
        return rawCompletionElements.filter {variable ->
            pattern.matcher(getQualifiedNameAsString(variable)).matches()
        }
    }

}
