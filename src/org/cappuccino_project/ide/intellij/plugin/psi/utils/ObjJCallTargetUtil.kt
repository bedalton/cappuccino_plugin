package org.cappuccino_project.ide.intellij.plugin.psi.utils

import com.intellij.openapi.project.DumbService
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil
import java.util.*

private val UNDETERMINED = listOf(ObjJClassType.UNDETERMINED)

fun ObjJCallTarget?.getPossibleCallTargetTypes(): List<String> {
    val callTarget = this ?: return ArrayUtils.EMPTY_STRING_ARRAY
    if (DumbService.isDumb(callTarget.project)) {
        return ArrayUtils.EMPTY_STRING_ARRAY
    }
    val classNames = getPossibleCallTargetTypesFromFormalVariableTypes(callTarget)
    if (classNames != null && !classNames.isEmpty()) {
        return classNames
    }
    val project = callTarget.project
    var containingClass: String? = ObjJPsiImplUtil.getContainingClassName(callTarget)
    when (callTarget.text) {
        "super" -> {
            containingClass = ObjJHasContainingClassPsiUtil.getContainingSuperClassName(callTarget)
            if (containingClass != null && !DumbService.isDumb(project)) {
                return ObjJInheritanceUtil.getAllInheritedClasses(containingClass, project)
            }
        }
        "this", "self" -> if (containingClass != null && !DumbService.isDumb(project)) {
            return ObjJInheritanceUtil.getAllInheritedClasses(containingClass, project)
        }
    }

    if (!DumbService.isDumb(callTarget.project) && !ObjJImplementationDeclarationsIndex.instance.getKeysByPattern(callTarget.text, project).isEmpty()) {
        return ObjJInheritanceUtil.getAllInheritedClasses(callTarget.text, project)
    }
    val out = ArrayList<String>()
    val varNameResults = callTarget.getCallTargetTypeFromVarName()
    if (varNameResults != null) {
        out.addAll(varNameResults)
    }
    return out
}

fun ObjJCallTarget.getCallTargetTypeFromVarName(): List<String>? {
    val results = ExpressionReturnTypeResults(project)
    val qualifiedReference: ObjJQualifiedReference = qualifiedReference ?: return null
    val variableNameList = qualifiedReference.variableNameList
    if (variableNameList.size == 1) {
        for (variableName in ObjJVariableNameUtil.getMatchingPrecedingVariableAssignmentNameElements(variableNameList[0], variableNameList.size - 1)) {
            val declaration = variableName.getParentOfType(ObjJVariableDeclaration::class.java) ?: continue
            val currentResults = declaration.expr.getReturnTypes()
            if (currentResults == null || currentResults.references.isEmpty()) {
                continue
            }
            results.tick(currentResults)
        }
    }
    val out = ArrayList<String>()
    for (reference in results.references) {
        if (!out.contains(reference.type)) {
            out.add(reference.type)
        }
    }
    return out

}

fun getPossibleCallTargetTypesFromFormalVariableTypes(callTarget: ObjJCallTarget): List<String>? {
    if (callTarget.qualifiedReference == null || callTarget.qualifiedReference!!.variableNameList.isEmpty()) {
        return null
    }
    val callTargetVariableName = callTarget.qualifiedReference!!.variableNameList[0]
    val resolvedVariableName = ObjJVariableNameResolveUtil.getVariableDeclarationElement(callTargetVariableName, true)
            ?: //Logger.getAnonymousLogger().log(Level.INFO, "Failed to find formal variable type for target with value: <"+callTargetVariableName.getText()+">");
            return null
    var formalVariableType: ObjJFormalVariableType? = null
    val containingSelector = resolvedVariableName.getParentOfType(ObjJMethodDeclarationSelector::class.java)
    if (containingSelector != null) {
        formalVariableType = containingSelector.formalVariableType
    }
    val instanceVariableDeclaration = resolvedVariableName.getParentOfType(ObjJInstanceVariableDeclaration::class.java)
    if (instanceVariableDeclaration != null) {
        formalVariableType = instanceVariableDeclaration.formalVariableType
        //Logger.getAnonymousLogger().log(Level.INFO, "Call Target <"+callTargetVariableName.getText()+"> is an instance variable with type: <"+formalVariableType+">");
    }
    if (formalVariableType == null) {
        return null
    }
    if (ObjJClassType.isPrimitive(formalVariableType.text)) {
        return listOf(formalVariableType.text)
    }
    return if (formalVariableType.varTypeId != null && formalVariableType.varTypeId!!.className != null) {
        ObjJInheritanceUtil.getAllInheritedClasses(formalVariableType.varTypeId!!.className!!.text, callTarget.project)
    } else {
        ObjJInheritanceUtil.getAllInheritedClasses(formalVariableType.text, callTarget.project)
    }
}

fun ObjJMethodCall.getPossibleCallTargetTypesFromMethodCall(): List<String> {

    val classConstraints: MutableList<String>
    val callTarget = callTarget
    val callTargetText = callTarget.getCallTargetTypeIfAllocStatement()
    when (callTargetText) {
        "self" -> classConstraints = ObjJInheritanceUtil.getAllInheritedClasses(containingClassName, project) as MutableList<String>
        "super" -> {
            val containingClass = containingClassName
            classConstraints = ObjJInheritanceUtil.getAllInheritedClasses(containingClass, project) as MutableList<String>
            if (classConstraints.size > 1) {
                classConstraints.remove(containingClass)
            }
        }
        else -> classConstraints = ObjJInheritanceUtil.getAllInheritedClasses(callTargetText, project) as MutableList<String>
    }
    return classConstraints
}

fun ObjJCallTarget.getCallTargetTypeIfAllocStatement(): String {
    val subMethodCall = qualifiedReference?.methodCall ?: return text;
    val subMethodCallSelectorString = subMethodCall.selectorString
    if (subMethodCallSelectorString == "alloc:" || subMethodCallSelectorString == "new:") {
        return subMethodCall.getCallTargetText()
    }
    return text
}