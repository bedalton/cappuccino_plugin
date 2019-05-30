package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.contributor.ObjJVariableTypeResolver
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.ObjJHasContainingClassPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameAggregatorUtil
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project


/**
 * Attempts to get possible class types for a call target
 */
fun getPossibleClassTypesForQualifiedReference(qualifiedReference:ObjJQualifiedReference) : Set<String> {
    val project = qualifiedReference.project
    val methodCall = qualifiedReference.methodCall
    if (methodCall != null) {
        return getPossibleCallTargetTypeFromMethodCall(methodCall)
                .flatMap {
                    val out = mutableListOf(it)
                    out.addAll(ObjJInheritanceUtil.getAllInheritedClasses(it, project, true))
                    out
                }.toSet()
    }

    if (qualifiedReference.qualifiedNameParts.size != 1) {
        return setOf()
    }

    // Get if variable name
    val variableName = qualifiedReference.qualifiedNameParts.getOrNull(0) as? ObjJVariableName
            ?: return setOf()
    return getPossibleTypesIfVariableName(variableName)
            .flatMap {
                val out = mutableListOf(it)
                out.addAll(ObjJInheritanceUtil.getAllInheritedClasses(it, project, true))
                out
            }.toSet()
}

fun ObjJVariableName.getPossibleClassTypes() : Set<String> {
    return getPossibleTypesIfVariableName(this)
            .flatMap {
                val out = mutableListOf(it)
                out.addAll(ObjJInheritanceUtil.getAllInheritedClasses(it, project, true))
                out
            }.toSet()
}

/**
 * Attempt to get call call target type if variable name
 */
private fun getPossibleTypesIfVariableName(variableName: ObjJVariableName) : Set<String> {
    val className = when (variableName.text) {
        "self" -> variableName.containingClassName
        "super" -> variableName.getContainingSuperClass(true)?.text
        else -> {
            ObjJIgnoreEvaluatorUtil.getVariableTypesInParent(variableName) ?: getTypeFromInstanceVariables(variableName)
        }
    } ?: return ObjJVariableTypeResolver.resolveVariableType(variableName)
    return ObjJInheritanceUtil.getAllInheritedClasses(className, variableName.project, true)
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


/**
 * Attempts to get possible call target type if method call
 */
private fun getPossibleCallTargetTypeFromMethodCall(methodCall: ObjJMethodCall, follow:Boolean = true) : Set<String> {
    if (methodCall.selector?.text == "alloc") {
        return ObjJInheritanceUtil.getAllInheritedClasses(methodCall.callTargetText, methodCall.project, true)
    }

    if (methodCall.selectorList.size == 1) {
        val out = getSimpleTargetTypesIfAccessor(methodCall, follow)
        if (out.isNotEmpty())
            return out
    }
    return getPossibleCallTargetTypesFromMultiSelectorCall(methodCall)
}

/**
 * Attempts to get target type from simple self or super calls to instance variables
 */
private fun getSimpleTargetTypesIfAccessor(methodCall: ObjJMethodCall, follow:Boolean = true) : Set<String> {

    val selectorVariableName = methodCall.selectorList[0].getSelectorString(false)
    // Attempts to get simple containing class target
    val containingClass = when (methodCall.callTarget.text) {
        "self" -> ObjJHasContainingClassPsiUtil.getContainingClassName(methodCall)
        "super" -> ObjJHasContainingClassPsiUtil.getContainingSuperClassName(methodCall)
        else -> null
    }

    val project: Project = methodCall.project
    if (containingClass == null) {
        // If should not go recursive
        if (!follow) {
            return setOf()
        }
        val callTargetAsMethodCall = methodCall.callTarget.expr?.leftExpr?.methodCall
        if (callTargetAsMethodCall == null || methodCall.callTarget.expr?.rightExprList?.isNotEmpty() == true) {
            return setOf()
        }
        val containingClasses = getPossibleCallTargetTypeFromMethodCall(callTargetAsMethodCall, false)
        val out = mutableListOf<String>()
        containingClasses.forEach{
            out.addAll(getInstanceVariableTypesForClass(it, selectorVariableName, project))
        }
        return out.toSet()
    }

    return getInstanceVariableTypesForClass(containingClass, selectorVariableName, project)
}

private fun getInstanceVariableTypesForClass(containingClass:String, selectorVariableName:String, project: Project) : Set<String> {
    // Tries to get instance variables for tyoe
    val instanceVariables = ObjJVariableNameAggregatorUtil
            .getAllContainingClassInstanceVariables(containingClass, project)
            .filter {
                selectorVariableName in getInstanceVariableAndAccessorsAsStringList(it)
            }

    // If no instance variables found, return
    if (instanceVariables.isEmpty())
        return setOf()

    // Gets all variables types in instance variables
    val out: List<String> = getVariableTypesFromInstanceVariableNames(instanceVariables)
    return out.toSet()
}


/**
 * Gets possible call target types from compound selectors
 * Very simple implementation
 * Most object return types return id, making it near useless
 */
private fun getPossibleCallTargetTypesFromMultiSelectorCall(methodCall: ObjJMethodCall) : Set<String> {
    if (DumbService.isDumb(methodCall.project))
        return setOf()
    val selector = methodCall.selectorString
    return ObjJUnifiedMethodIndex.instance[selector, methodCall.project]
            .flatMap {
                it.returnTypes
            }
            .filterNot {
                it.startsWith("@") || it == "IBAction" || it == "IBAction" || it == "void"
            }.toSet()
}

/**
 * Returns a list containing am instnace variables accessors and name as strings
 */
private fun getInstanceVariableAndAccessorsAsStringList(it: ObjJVariableName) : List<String> {
    val out:MutableList<String> = mutableListOf(it.text)
    val parent = it.getParentOfType(ObjJInstanceVariableDeclaration::class.java) ?: return out
    val accessors = parent.accessorPropertyList.filterNot { it.accessorPropertyType.text == "setter" }.mapNotNull { it.accessor?.text }
    out.addAll(accessors)
    return out
}

/**
 * Attempts to get variable types from a list of instance variables
 */
private fun getVariableTypesFromInstanceVariableNames(instanceVariables:List<ObjJVariableName>) : List<String> {
    return instanceVariables.mapNotNull {
        val formalVariableType = it.getParentOfType(ObjJInstanceVariableDeclaration::class.java)?.formalVariableType
        if (formalVariableType?.varTypeId?.className != null)
            formalVariableType.varTypeId?.className?.text
        else
            formalVariableType?.text
    }
}