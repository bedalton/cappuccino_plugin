package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import cappuccino.ide.intellij.plugin.utils.ifEmptyNull
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement


object ObjJVariableTypeResolver {

    fun resolveVariableType(variableName: ObjJVariableName, withGeneric:Boolean, recurse:Boolean = true, tag:Long,  withInheritance:Boolean = false): Set<String> {
        if (ObjJPluginSettings.resolveCallTargetFromAssignments && !ObjJCommentEvaluatorUtil.isInferDisabled(variableName, variableName.text)) {
            return resolveVariableTypeWithoutMethodParse(variableName, withGeneric, recurse, tag, withInheritance)
        }
        return emptySet()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun resolveVariableTypeWithoutMethodParse(variableName: ObjJVariableName, withGeneric:Boolean, recurse: Boolean = true, tag:Long, withInheritance:Boolean = false) : Set<String> {
        val project = variableName.project
        var containingClass: String? = ObjJPsiImplUtil.getContainingClassName(variableName)

        // Get if simple reference
        when (variableName.text) {
            "super" -> {
                containingClass = ObjJHasContainingClassPsiUtil.getContainingSuperClassName(variableName)
                if (containingClass != null && !DumbService.isDumb(project)) {
                    return if (withInheritance)
                        ObjJInheritanceUtil.getAllInheritedClasses(containingClass, project).toSet()
                    else setOf(containingClass)
                }
            }
            "this", "self" -> if (containingClass != null && !DumbService.isDumb(project)) {
                return if (withInheritance)
                    ObjJInheritanceUtil.getAllInheritedClasses(containingClass, project).toSet()
                else setOf(containingClass)
            }
        }

        // Get annotation based variable type
        val annotationBasedVariableType = ObjJCommentEvaluatorUtil.getVariableTypesInParent(variableName)
        if (annotationBasedVariableType != null) {
            return if (withInheritance)
                ObjJInheritanceUtil.getAllInheritedClasses(annotationBasedVariableType, project).toSet()
            else setOf(annotationBasedVariableType)
        }

        // Ensure indexes are not dumb before querying them
        if (DumbService.isDumb(project)) {
            return emptySet()
        }

        // Get call target from formal variable types
        val className = getPossibleCallTargetTypesFromFormalVariableTypes(variableName, withGeneric)
        if (className.isNotNullOrEmpty()) {
            return if (withInheritance)
                ObjJInheritanceUtil.getAllInheritedClasses(className!!, project).toSet()
            else
                setOf(className!!)
        }

        if (!DumbService.isDumb(project) && ObjJImplementationDeclarationsIndex.instance.containsKey(variableName.text, project)) {
            return if (withInheritance)
                ObjJInheritanceUtil.getAllInheritedClasses(variableName.text, project).toSet()
            else
                setOf(variableName.text)
        }
        val out = mutableSetOf<String>()
        /*val varNameResults = getVariableTypeFromAssignments(variableName, recurse, tag)//
        if (varNameResults != null) {
            if (withInheritance){
                out.addAll(varNameResults.flatMap { ObjJInheritanceUtil.getAllInheritedClasses(it, project) })
            } else
                out.addAll(varNameResults)
        }*/
        return if (out.isNotEmpty()) out else setOf()
    }

    private fun getPossibleCallTargetTypesFromFormalVariableTypes(callTargetVariableName:ObjJVariableName, withGeneric: Boolean):String? {
        val formalVariableType = getPossibleCallTargetTypesFromFormalVariableTypesRaw(callTargetVariableName) ?: return null
        var genericClass = if (withGeneric) formalVariableType.classNameGeneric?.classNameString.orEmpty() else ""
        if (genericClass.isNotNullOrEmpty()) {
            genericClass = "<$genericClass>"
        }
        return formalVariableType.varTypeId?.className?.text ?: formalVariableType.text + genericClass
    }

    private fun getPossibleCallTargetTypesFromFormalVariableTypesRaw(callTargetVariableName:ObjJVariableName): ObjJFormalVariableType? {
        val resolvedVariableName = ObjJVariableNameResolveUtil.getVariableDeclarationElement(callTargetVariableName)
                ?: return null

        return  getVariableTypeIfDefinedInMethodDeclaration(resolvedVariableName) ?:
                getVariableTypeIfInstanceVariable(resolvedVariableName)
    }

    private fun getVariableTypeIfDefinedInMethodDeclaration(resolvedVariableName:PsiElement) : ObjJFormalVariableType? {
        val containingSelector = resolvedVariableName.getParentOfType(ObjJMethodDeclarationSelector::class.java)
        if (containingSelector != null) {
            return containingSelector.formalVariableType
        }
        return null
    }

    private fun getVariableTypeIfInstanceVariable(resolvedVariableName:PsiElement) : ObjJFormalVariableType? {
        val instanceVariableDeclaration = resolvedVariableName.getParentOfType(ObjJInstanceVariableDeclaration::class.java)
        if (instanceVariableDeclaration != null) {
            return instanceVariableDeclaration.formalVariableType
            //Logger.getAnonymousLogger().log(Level.INFO, "Call Target <"+callTargetVariableName.getText()+"> is an instance variable with type: <"+formalVariableType+">");
        }
        return null
    }
}
