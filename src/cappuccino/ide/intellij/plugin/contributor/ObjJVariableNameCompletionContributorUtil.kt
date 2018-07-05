package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil
import cappuccino.ide.intellij.plugin.utils.*

import java.util.ArrayList
import java.util.logging.Logger
import java.util.regex.Pattern

import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil.getIndexInQualifiedNameParent
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil.getPrecedingVariableAssignmentNameElements
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil.getQualifiedNameAsString
import java.util.logging.Level

object ObjJVariableNameCompletionContributorUtil {

    private val LOGGER = Logger.getLogger(ObjJVariableNameCompletionContributorUtil::class.java.name)
    val CARET_INDICATOR = ObjJCompletionContributor.CARET_INDICATOR

    fun getVariableNameCompletions(variableName: ObjJVariableName?): List<String> {
        if (variableName == null || variableName.text.isEmpty()) {
            LOGGER.log(Level.INFO, "Variable Name is null")
            return emptyList()
        }

        //Initialize variable name array
        val out = getInstanceVariableCompletion(variableName) ?: ArrayList()
        LOGGER.log(Level.INFO, "Found: ${out.size} instance variables");
        addAllVariableNameElementsByName(out, variableName)
        //LOGGER.log(Level.INFO, String.format("NameFilter:<%s>; Raw Completion Elements: <%d>; Num after filter by name: <%d>", variableName, rawCompletionElements.size(), foldingDescriptors.size()));
        return out
    }

    fun getInstanceVariableCompletion(variableName: ObjJVariableName?) : ArrayList<String>? {
        if (variableName == null) {
            LOGGER.log(Level.INFO, "getInstanceVariablesCompletion failed with null variable names")
            return null
        }
        val completions = ObjJVariableNameUtil.getFormalVariableInstanceVariables(variableName) ?: return null
        val out:ArrayList<String> = ArrayList()
        for (variableNameInLoop in completions) {
            LOGGER.log(Level.INFO, "Has Instance variable completion: "+variableNameInLoop.text)
            out.add(variableNameInLoop.text)
        }
        LOGGER.log(Level.INFO, "VariableName has <${out.size}> instance variable completions")
        return out
    }

    /*
    fun getInstanceVariableCompletion(variableName: ObjJVariableName?) : ArrayList<String>? {
        val qualifiedReference:ObjJQualifiedReference = variableName?.getParentOfType(ObjJQualifiedReference::class.java) ?: return null
        val variableNames:List<ObjJVariableName> = qualifiedReference.variableNameList
        val index:Int = variableNames.indexOf(variableName)
        if (index < 1) {
            return null
        }
        val resolvedReference = (variableNames.get(index - 1).reference.resolve() ?: return null) as? ObjJVariableName
                ?: return null
        val instanceVariableList = ObjJVariableNameUtil.getFormalVariableInstanceVariables(resolvedReference) ?: return null
        val out:ArrayList<String> = ArrayList()
        for (instanceVariable in instanceVariableList) {
            out.add(instanceVariable.name)
        }
        LOGGER.log(Level.INFO, "Resolved Instance variable completion with ${out.size} variable names")
        return out
    }*/

    fun addAllVariableNameElementsByName(out:MutableList<String>, variableName: ObjJVariableName?) {
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
        for (currentVariableName in ArrayUtils.filter(rawCompletionElements) { `var` ->
            pattern.matcher(getQualifiedNameAsString(`var`)).matches()
        }) {
            out.add(currentVariableName.name)
        }
    }

}
