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

    fun getVariableNameCompletions(variableName: ObjJVariableName?): List<String> {
        if (variableName == null || variableName.text.isEmpty()) {
            return emptyList()
        }

        //Initialize variable name array
        val out = getInstanceVariableCompletion(variableName) ?: ArrayList()
        addAllVariableNameElementsByName(out, variableName)
        return out
    }

    private fun getInstanceVariableCompletion(variableName: ObjJVariableName?) : ArrayList<String>? {
        if (variableName == null) {
            return null
        }
        val completions = ObjJVariableNameAggregatorUtil.getFormalVariableInstanceVariables(variableName) ?: return null
        val out:ArrayList<String> = ArrayList()
        for (variableNameInLoop in completions) {
            out.add(variableNameInLoop.text)
        }
        return out
    }

    private fun addAllVariableNameElementsByName(out:MutableList<String>, variableName: ObjJVariableName?) {
        if (variableName == null) {
            return
        }
        //Get variable name regex pattern
        val variableNamePattern = getQualifiedNameAsString(variableName).replace(CARET_INDICATOR, "(.*)")
        val pattern = Pattern.compile(variableNamePattern)

        //Get Qualified name reference for completion
        val qualifiedNameIndex = getIndexInQualifiedNameParent(variableName)
        val rawCompletionElements = getPrecedingVariableAssignmentNameElements(variableName, qualifiedNameIndex)

        //Parse variable names to string
        for (currentVariableName in ArrayUtils.filter(rawCompletionElements) { variable ->
            pattern.matcher(getQualifiedNameAsString(variable)).matches()
        }) {
            out.add(currentVariableName.name)
        }
    }

}
