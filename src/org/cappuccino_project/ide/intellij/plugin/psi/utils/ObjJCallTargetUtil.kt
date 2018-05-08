package org.cappuccino_project.ide.intellij.plugin.psi.utils

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJExpressionReturnTypeUtil.ExpressionReturnTypeReference
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJExpressionReturnTypeUtil.ExpressionReturnTypeResults
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil
import java.util.ArrayList
import java.util.Collections

object ObjJCallTargetUtil {

    private val UNDETERMINED = listOf(ObjJClassType.UNDETERMINED)

    fun getPossibleCallTargetTypes(callTarget: ObjJCallTarget?): List<String> {
        if (callTarget == null || DumbService.isDumb(callTarget.project)) {
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
        val varNameResults = getCallTargetTypeFromVarName(callTarget)
        if (varNameResults != null) {
            out.addAll(varNameResults)
        }
        return out
    }

    fun getCallTargetTypeFromVarName(callTarget: ObjJCallTarget): List<String>? {
        val results = ExpressionReturnTypeResults(callTarget.project)
        if (callTarget.qualifiedReference != null) {
            if (callTarget.qualifiedReference!!.variableNameList.size == 1) {
                for (variableName in ObjJVariableNameUtil.getMatchingPrecedingVariableAssignmentNameElements(callTarget.qualifiedReference!!.variableNameList[0], callTarget.qualifiedReference!!.variableNameList.size - 1)) {
                    val declaration = variableName.getParentOfType(ObjJVariableDeclaration::class.java) ?: continue
                    val currentResults = ObjJExpressionReturnTypeUtil.getReturnTypes(declaration.expr)
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
        return null

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
        val containingSelector = ObjJTreeUtil.getParentOfType(resolvedVariableName, ObjJMethodDeclarationSelector::class.java)
        if (containingSelector != null) {
            formalVariableType = containingSelector.formalVariableType
        }
        val instanceVariableDeclaration = ObjJTreeUtil.getParentOfType(resolvedVariableName, ObjJInstanceVariableDeclaration::class.java)
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

    fun getPossibleCallTargetTypesFromMethodCall(methodCall: ObjJMethodCall): List<String> {

        val classConstraints: MutableList<String>
        val callTarget = methodCall.callTarget
        val callTargetText = getCallTargetTypeIfAllocStatement(callTarget)
        when (callTargetText) {
            "self" -> classConstraints = ObjJInheritanceUtil.getAllInheritedClasses(methodCall.containingClassName, methodCall.project)
            "super" -> {
                val containingClass = methodCall.containingClassName
                classConstraints = ObjJInheritanceUtil.getAllInheritedClasses(containingClass, methodCall.project)
                if (classConstraints.size > 1) {
                    classConstraints.remove(containingClass)
                }
            }
            else -> classConstraints = ObjJInheritanceUtil.getAllInheritedClasses(callTargetText, methodCall.project)
        }
        return classConstraints
    }

    fun getCallTargetTypeIfAllocStatement(callTarget: ObjJCallTarget): String {
        if (callTarget.qualifiedReference != null && callTarget.qualifiedReference!!.methodCall != null) {
            val subMethodCall = callTarget.qualifiedReference!!.methodCall
            val subMethodCallSelectorString = subMethodCall!!.selectorString
            if (subMethodCallSelectorString == "alloc:" || subMethodCallSelectorString == "new:") {
                return subMethodCall.callTargetText
            }
        }
        return callTarget.text
    }


}
