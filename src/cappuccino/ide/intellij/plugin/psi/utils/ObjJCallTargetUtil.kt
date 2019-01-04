package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.openapi.project.DumbService
import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import java.util.*

private val UNDETERMINED = listOf(ObjJClassType.UNDETERMINED)
object ObjJCallTargetUtil {
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
        val resolvedVariableName = ObjJVariableNameResolveUtil.getVariableDeclarationElement(callTargetVariableName)
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

    fun ObjJMethodCall.getPossibleCallTargetTypes() = getPossibleCallTargetTypesFromMethodCall(this)

    fun getPossibleCallTargetTypesFromMethodCall(methodCall: ObjJMethodCall): List<String> {

        val classConstraints: MutableList<String>
        val callTarget = methodCall.callTarget
        val callTargetText = callTarget.getCallTargetTypeIfAllocStatement()
        val project = methodCall.project
        when (callTargetText) {
            "self" -> classConstraints = ObjJInheritanceUtil.getAllInheritedClasses(methodCall.containingClassName, project) as MutableList<String>
            "super" -> {
                val containingClass = methodCall.containingClassName
                classConstraints = ObjJInheritanceUtil.getAllInheritedClasses(containingClass, project) as MutableList<String>
                if (classConstraints.size > 1) {
                    classConstraints.remove(containingClass)
                }
            }
            else -> {
                val referencedVariableSelectorParent: ObjJMethodDeclarationSelector? = callTarget.qualifiedReference?.getLastVar()?.reference?.resolve()?.getParentOfType(ObjJMethodDeclarationSelector::class.java)
                classConstraints = if (referencedVariableSelectorParent != null) {
                    val varType = referencedVariableSelectorParent.varType
                    val varTypeId = varType?.varTypeId
                    if (varTypeId != null) {
                        ObjJInheritanceUtil.getAllInheritedClasses(varTypeId.idType ?: varTypeId.text, project)
                    } else {
                        ObjJInheritanceUtil.getAllInheritedClasses(varType?.className?.text
                                ?: ObjJClassType.UNDETERMINED, project);
                    }
                } else {

                    ObjJInheritanceUtil.getAllInheritedClasses(callTargetText, project) as MutableList<String>
                }
            }
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
}