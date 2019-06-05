package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.ObjJCompletionContributor.Companion.CARET_INDICATOR
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameAggregatorUtil
import cappuccino.ide.intellij.plugin.utils.*

import java.util.ArrayList
import java.util.regex.Pattern

import cappuccino.ide.intellij.plugin.psi.utils.ObjJQualifiedReferenceUtil.getIndexInQualifiedNameParent
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameAggregatorUtil.getPrecedingVariableAssignmentNameElements
import cappuccino.ide.intellij.plugin.psi.utils.ObjJQualifiedReferenceUtil.getQualifiedNameAsString

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
        val variableNamePattern = getQualifiedNameAsString(variableName).replace(CARET_INDICATOR, "(.*)")
        val pattern = Pattern.compile(variableNamePattern)

        //Get Qualified name reference for completion
        val qualifiedNameIndex = getIndexInQualifiedNameParent(variableName)
        val rawCompletionElements = getPrecedingVariableAssignmentNameElements(variableName, qualifiedNameIndex)

        //Parse variable names to string
        return rawCompletionElements.filter {variable ->
            pattern.matcher(getQualifiedNameAsString(variable)).matches()
        }
    }

}
