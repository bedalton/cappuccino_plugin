package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.ObjJIgnoreEvaluatorUtil
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement


object ObjJVariableTypeResolver {

    fun resolveVariableType(variableName: ObjJVariableName, recurse:Boolean = true, level:Int, tag:Long,  withInheritance:Boolean = false): Set<String> {
        if (level < 0)
            return emptySet()
        if (ObjJPluginSettings.resolveCallTargetFromAssignments && !ObjJIgnoreEvaluatorUtil.isInferDisabled(variableName, variableName.text)) {
            return resolveVariableTypeWithoutMethodParse(variableName, recurse, level - 1, tag, withInheritance)
        }
        return setOf()
    }

    private fun resolveVariableTypeWithoutMethodParse(variableName: ObjJVariableName, recurse: Boolean = true, level:Int, tag:Long, withInheritance:Boolean = false) : Set<String> {
        if (level < 0)
            return emptySet()
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
        val annotationBasedVariableType = ObjJIgnoreEvaluatorUtil.getVariableTypesInParent(variableName)
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
        val classNames = getPossibleCallTargetTypesFromFormalVariableTypes(variableName)
        if (classNames != null && classNames.isNotEmpty()) {
            return classNames
        }

        if (!DumbService.isDumb(project) && ObjJImplementationDeclarationsIndex.instance.getKeysByPattern(variableName.text, project).isNotEmpty()) {
            return if (withInheritance)
                ObjJInheritanceUtil.getAllInheritedClasses(variableName.text, project).toSet()
            else
                setOf(variableName.text)
        }
        val out = mutableSetOf<String>()
        val varNameResults = getVariableTypeFromAssignments(variableName, recurse, level, tag)//
        if (varNameResults != null) {
            if (withInheritance){
                out.addAll(varNameResults.flatMap { ObjJInheritanceUtil.getAllInheritedClasses(it, project) })
            } else
                out.addAll(varNameResults)
        }
        return if (out.isNotEmpty()) out else setOf()
    }

    private fun getVariableTypeFromAssignments(variableName:ObjJVariableName, recurse: Boolean, level:Int, tag:Long) : Set<String>? {
        val fromBodyVariableAssignments = getVariableTypeFromBodyVariableAssignments(variableName, recurse, level, tag)
        if (fromBodyVariableAssignments?.isNotEmpty().orFalse()) {
            return fromBodyVariableAssignments
        }
        val fromExpressionAssignments = getVariableTypeFromExpressionAssignments(variableName, recurse, level, tag)
        if (fromExpressionAssignments?.isNotEmpty().orFalse()) {
            return fromExpressionAssignments
        }
        return null
    }

    private fun getVariableTypeFromBodyVariableAssignments(variableName: ObjJVariableName, recurse: Boolean, level: Int, tag: Long) : Set<String>? {
        if (variableName.indexInQualifiedReference != 0) {
            return null
        }
        val variableNameText = variableName.text
        val assignmentsRaw = variableName.getParentBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true)
                .flatMap { it.variableDeclarationList?.variableDeclarationList ?: emptyList() }
                .filter { it.qualifiedReferenceList.any { q -> q.text == variableNameText } }

        val out = mutableSetOf<String>()
        for (variableDeclaration in assignmentsRaw) {
            val set = getVariableTypeFromAssignment(variableDeclaration, recurse, level, tag) ?: continue
            out.addAll(set)
        }
        return out.toSet()
    }

    private fun getVariableTypeFromExpressionAssignments(variableName: ObjJVariableName, recurse: Boolean, level: Int, tag:Long) : Set<String>? {
        val assignmentsRaw:List<ObjJVariableDeclaration> = variableName
                .getParentBlockChildrenOfType(ObjJExpr::class.java, true)
                .mapNotNull {expr ->  expr.leftExpr?.variableDeclaration }
                // Convoluted check to ensure that any of the qualified references listed in a compound expression
                // contain a variable with this name
                .filter {
                    it.qualifiedReferenceList.any { qRef ->
                        qRef.variableNameList.size == 1 && qRef.variableNameList.getOrNull(0) is ObjJVariableName
                    }
                }

        val out = mutableSetOf<String>()
        assignmentsRaw.forEach {
            val set = getVariableTypeFromAssignment(it, recurse, level, tag) ?: return@forEach
            out.addAll(set)
        }
        return out
    }

    private fun getVariableTypeFromAssignment(variableDeclaration:ObjJVariableDeclaration, recurse: Boolean, level:Int, tag:Long) : Set<String>? {
        val expression = variableDeclaration.expr
        val leftExpr = expression.leftExpr ?: return null
        var out:Set<String>? = getTypeFromMethodCall(leftExpr, level, tag)
        if (out != null) {
            return out
        }
        out = getTypeFromQualifiedReferenceAssignment(leftExpr, recurse, level, tag)
        if (out != null) {
            return out
        }
        return null
    }

    private fun getTypeFromMethodCall(leftExpression: ObjJLeftExpr, level:Int, tag:Long) : Set<String>? {
        val methodCall = leftExpression.methodCall ?: return null
        val selector = methodCall.selectorString
        val skipIf = listOf(
                "null",
                "nil"
        )
        val out = ObjJUnifiedMethodIndex.instance[selector, methodCall.project].flatMap { it.getReturnTypes(level - 1, tag) }.toSet().filterNot{ returnType ->
            returnType.toLowerCase() in skipIf ||returnType == "id" || returnType.contains("<")
        }.toSet()
        return if (out.isNotEmpty()) out else null
    }

    private fun getTypeFromQualifiedReferenceAssignment(leftExpression: ObjJLeftExpr, recurse:Boolean, level: Int, tag:Long) : Set<String>? {
        if (!recurse) {
            return null
        }
        val qualifiedAssignment = leftExpression.qualifiedReference ?: return null
        val lastVar = qualifiedAssignment.lastVar ?: return null
        return resolveVariableType(lastVar, false, level - 1, tag)
    }

    private fun getPossibleCallTargetTypesFromFormalVariableTypes(callTargetVariableName:ObjJVariableName): Set<String>? {
        val formalVariableType = getPossibleCallTargetTypesFromFormalVariableTypesRaw(callTargetVariableName) ?: return null
        return if (formalVariableType.varTypeId != null && formalVariableType.varTypeId!!.className != null) {
            setOf(formalVariableType.varTypeId!!.className!!.text)
        } else {
            setOf(formalVariableType.text)
        }
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

    // @todo implement checks for call assignments
    /*
    fun getCallTargetTypeIfAllocStatement(callTarget:ObjJCallTarget): String {
        val subMethodCall = callTarget.qualifiedReference?.methodCall ?: return callTarget.text
        val subMethodCallSelectorString = subMethodCall.selectorString
        if (subMethodCallSelectorString == "alloc:" || subMethodCallSelectorString == "new:") {
            return subMethodCall.getCallTargetText()
        }
        return callTarget.text
    }*/
}