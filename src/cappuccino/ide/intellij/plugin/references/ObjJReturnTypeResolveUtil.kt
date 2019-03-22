package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.contributor.ObjJVariableTypeResolver
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil


fun getClassConstraints(element: ObjJSelector): List<String> {
    return getClassConstraints(element.getParentOfType( ObjJHasMethodSelector::class.java))
}


private fun getClassConstraints(element: ObjJHasMethodSelector?): List<String> {
    if (element !is ObjJMethodCall) {
        return emptyList()
    }
    val methodCall = element as ObjJMethodCall?
    val callTarget: ObjJCallTarget? = methodCall?.callTarget
    if (callTarget != null) {
        val possibleClasses = getPossibleClassTypesForCallTarget(callTarget)
        if (possibleClasses.isNotEmpty()) {
            return possibleClasses.toList()
        }
    }
    return methodCall?.callTarget?.possibleCallTargetTypes ?: mutableListOf()
}

fun getPossibleClassTypesForCallTarget(callTarget: ObjJCallTarget) : Set<String> {
    val qualifiedReference = callTarget.qualifiedReference ?: return setOf()
    val methodCall = qualifiedReference.methodCall
    if (methodCall != null) {
        if (methodCall.selector?.text == "alloc") {
            return ObjJInheritanceUtil.getAllInheritedClasses(methodCall.callTargetText, methodCall.project, true)
        }
    }
    val variables = qualifiedReference.variableNameList

    if (variables.size != 1) {
        return setOf()
    }
    val variableName = variables[0]
    val variableNameText = variableName.text
    val className = when (variableNameText) {
        "self" -> variableName.containingClassName
        "super" -> variableName.getContainingSuperClass(true)?.text
        else -> {
            ObjJIgnoreEvaluatorUtil.getVariableTypesInParent(variableName) ?: getTypeFromInstanceVariables(variableName)
        }
    } ?: return ObjJVariableTypeResolver.resolveVariableType(variableName)
    return ObjJInheritanceUtil.getAllInheritedClasses(className, callTarget.project, true)
}

/**
 * Attempts to find a variables type, if the variable is declared as an instance variable
 * @return variable type if it is known form an instance variable declaration
 */
private fun getTypeFromInstanceVariables(variableName: ObjJVariableName) : String? {
    val referencedVariable = variableName.reference.resolve() ?: return null
    val instanceVariable = referencedVariable.getParentOfType(ObjJInstanceVariableDeclaration::class.java) ?: return null
    val type = instanceVariable.formalVariableType
    if (type.varTypeId != null) {
        return type.varTypeId?.className?.text ?: ObjJClassType.UNDETERMINED
    }
    return type.text
}
