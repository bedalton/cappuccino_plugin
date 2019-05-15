package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.ObjJIgnoreEvaluatorUtil
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import cappuccino.ide.intellij.plugin.utils.orFalse
import cappuccino.ide.intellij.plugin.utils.orTrue
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement


object ObjJVariableTypeResolver {

    fun resolveVariableType(variableName: ObjJVariableName, recurse:Boolean = true): Set<String> {
        if (ObjJPluginSettings.resolveCallTargetFromAssignments && !ObjJIgnoreEvaluatorUtil.isInferDisabled(variableName, variableName.text)) {
            return resolveVariableTypeWithoutMethodParse(variableName, recurse)
        }
        return setOf()
    }

    private fun resolveVariableTypeWithoutMethodParse(variableName: ObjJVariableName, recurse: Boolean = true) : Set<String> {

        val project = variableName.project
        var containingClass: String? = ObjJPsiImplUtil.getContainingClassName(variableName)

        // Get if simple reference
        when (variableName.text) {
            "super" -> {
                containingClass = ObjJHasContainingClassPsiUtil.getContainingSuperClassName(variableName)
                if (containingClass != null && !DumbService.isDumb(project)) {
                    return ObjJInheritanceUtil.getAllInheritedClasses(containingClass, project).toSet()
                }
            }
            "this", "self" -> if (containingClass != null && !DumbService.isDumb(project)) {
                return ObjJInheritanceUtil.getAllInheritedClasses(containingClass, project).toSet()
            }
        }

        // Get annotation based variable type
        val annotationBasedVariableType = ObjJIgnoreEvaluatorUtil.getVariableTypesInParent(variableName)
        if (annotationBasedVariableType != null) {
            return ObjJInheritanceUtil.getAllInheritedClasses(annotationBasedVariableType, project).toSet()
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

        if (!DumbService.isDumb(project) && !ObjJImplementationDeclarationsIndex.instance.getKeysByPattern(variableName.text, project).isEmpty()) {
            return ObjJInheritanceUtil.getAllInheritedClasses(variableName.text, project).toSet()
        }
        val out = mutableSetOf<String>()
        val varNameResults = getVariableTypeFromAssignments(variableName, recurse)?.flatMap { ObjJInheritanceUtil.getAllInheritedClasses(it, project) }?.toSet()
        if (varNameResults != null && varNameResults.isNotEmpty()) {
            out.addAll(varNameResults)
        }
        if (varNameResults == null) {
            out.addAll(resolveQualifiedReference(variableName.getParentOfType(ObjJQualifiedReference::class.java)))
        }
        return if (out.isNotEmpty()) out else setOf()
    }

    private fun getVariableTypeFromAssignments(variableName:ObjJVariableName, recurse: Boolean) : Set<String>? {
        val fromBodyVariableAssignments = getVariableTypeFromBodyVariableAssignments(variableName, recurse)
        if (fromBodyVariableAssignments?.isNotEmpty().orFalse()) {
            return fromBodyVariableAssignments
        }
        val fromExpressionAssignments = getVariableTypeFromExpressionAssignments(variableName, recurse)
        if (fromExpressionAssignments?.isNotEmpty().orFalse()) {
            return fromExpressionAssignments
        }
        return null
    }

    private fun getVariableTypeFromBodyVariableAssignments(variableName: ObjJVariableName, recurse: Boolean) : Set<String>? {
        if (variableName.indexInQualifiedReference != 0) {
            return null
        }
        val variableNameText = variableName.text
        val assignmentsRaw =variableName.getParentBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true)
                .flatMap { it.variableDeclarationList?.variableDeclarationList ?: emptyList() }
                .filter { it.qualifiedReferenceList.any { q -> q.text == variableNameText } }

        val out = mutableSetOf<String>()
        for (variableDeclaration in assignmentsRaw) {
            val set = getVariableTypeFromAssignment(variableDeclaration, recurse) ?: continue
            out.addAll(set)
        }
        val project = variableName.project
        return out.flatMap {
            ObjJInheritanceUtil.getAllInheritedClasses(it, project)
        }.toSet()
    }

    private fun getVariableTypeFromExpressionAssignments(variableName: ObjJVariableName, recurse: Boolean) : Set<String>? {
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
            val set = getVariableTypeFromAssignment(it, recurse) ?: return@forEach
            out.addAll(set)
        }
        return out
    }

    private fun getVariableTypeFromAssignment(variableDeclaration:ObjJVariableDeclaration, recurse: Boolean) : Set<String>? {
        val expression = variableDeclaration.expr
        val leftExpr = expression.leftExpr ?: return null
        var out:Set<String>? = getTypeFromMethodCall(leftExpr)
        if (out != null) {
            return out
        }
        out = getTypeFromQualifiedReferenceAssignment(leftExpr, recurse)
        if (out != null) {
            return out
        }
        return null
    }

    private fun getTypeFromMethodCall(leftExpression: ObjJLeftExpr) : Set<String>? {
        val methodCall = leftExpression.methodCall ?: return null
        val selector = methodCall.selectorString
        val out = mutableSetOf<String>()
        val skipIf = listOf(
                "null",
                "nil"
        )
        ObjJUnifiedMethodIndex.instance[selector, methodCall.project].forEach {
            val returnType = it.returnType
            if (returnType.toLowerCase() in skipIf)
                return@forEach
            if (returnType == "id" || returnType.contains("<")) {
                return null
            }
            out.add(returnType)
        }
        return if (out.isNotEmpty()) out else null
    }

    private fun getTypeFromQualifiedReferenceAssignment(leftExpression: ObjJLeftExpr, recurse:Boolean) : Set<String>? {
        if (!recurse) {
            return null
        }
        val qualifiedAssignment = leftExpression.qualifiedReference ?: return null
        val lastVar = qualifiedAssignment.lastVar ?: return null
        return resolveVariableType(lastVar, false)
    }

    private fun getPossibleCallTargetTypesFromFormalVariableTypes(callTargetVariableName:ObjJVariableName): Set<String>? {
        val formalVariableType = getPossibleCallTargetTypesFromFormalVariableTypesRaw(callTargetVariableName) ?: return null
        val project = callTargetVariableName.project
        return if (formalVariableType.varTypeId != null && formalVariableType.varTypeId!!.className != null) {
            ObjJInheritanceUtil.getAllInheritedClasses(formalVariableType.varTypeId!!.className!!.text, project).toSet()
        } else {
            ObjJInheritanceUtil.getAllInheritedClasses(formalVariableType.text, project).toSet()
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


private fun resolveExpressionType(expr:ObjJExpr) : List<String> {

    if (expr.leftExpr.qualifiedReference != null)
    return listOf()
}

private fun getIfExprHasRight(expr:ObjJExpr) : List<String>? {
    val left = expr.leftExpr ?: return null
    val rightExpressions = expr.rightExprList
    if (rightExpressions.isEmpty())
        return null
    val out = mutableListOf<String>()
    if (expr.isString)
        out.add(ObjJClassType.STRING)
    if (expr.isBool)
        out.add(ObjJClassType.BOOL)
    out.addAll(expr.numberTypes)
    return out
}

private val ObjJExpr.isString get() {
    if (this.leftExpr?.primary?.stringLiteral != null)
        return true
    val rightExpressions = this.rightExprList
    if (rightExpressions.isEmpty())
        return false
    for(rightExpr in rightExpressions) {
        if (rightExpr.mathExprPrime?.expr?.leftExpr?.primary?.stringLiteral != null)
            return true
    }
    return false
}

private val ObjJExpr.hasMath:Boolean get() {
    val rightExpressions = this.rightExprList
    if (rightExpressions.isEmpty())
        return false
    for(rightExpr in rightExpressions) {
        if (rightExpr.mathExprPrime != null)
            return true
    }
    return false
}

private val ObjJExpr.isBool get() {
    val rightExpressions = this.rightExprList
    if (rightExpressions.isEmpty())
        return false
    for(rightExpr in rightExpressions) {
        if (rightExpr?.logicExprPrime)
            return true
    }
}


private fun resolveQualifiedReference(qualifiedReference:ObjJQualifiedReference?) : List<String> {
    if (qualifiedReference == null) return listOf()
    val parts = qualifiedReference.qualifiedNameParts
    if (parts.isEmpty())
        return emptyList()
    var currentClasses = listOf<String>()
    var skipNext = false
    for (index in 0 until parts.size) {
        if (skipNext) {
            skipNext = false
            continue
        }
        val currentClass = getJsClassUnion(currentClasses, qualifiedReference.project)
        if (currentClass == null) {

        }
        var rawClassList:List<String> = listOf()
        val part = parts[index]
        val variableName = if (part is ObjJVariableName) part.text else if (part is ObjJFunctionCall) part.functionName?.text else null

        if (part is ObjJVariableName) {
            if (currentClass?.properties?.isEmpty().orTrue()) {
                rawClassList = ObjJVariableTypeResolver.resolveVariableType(part).toList()
            }
        } else if (part is ObjJFunctionCall) {
            val functionName = part.functionName ?: ""
            rawClassList = (currentClass?.functions ?: allJSClassesFunctions).filter { it.name == functionName }.mapNotNull { it.returns }
            if (rawClassList.isEmpty()) {
                val callbacks = currentClass?.properties?.mapNotNull { it.callback } ?: listOf()
                if (callbacks.isNotEmpty())
                    rawClassList = callbacks.mapNotNull { it.returns }
            }
        } else if (part is ObjJArrayIndexSelector) {
            if (currentClasses.contains("String") || currentClasses.contains("string") || currentClasses.contains("CPString")) {
                rawClassList = listOf("CPString")
            } else
                rawClassList = listOf("?")
        }

        if (rawClassList.isEmpty() && variableName != null) {
            rawClassList = allJSClassesProperties.filter { it.name == part.text }.map { it.type }
        }

        currentClasses = rawClassList
                .flatMap {
                    it.split("Array|\\s*\\|\\s*".toRegex())
                }
                .map { it.trim() }
        val arrayTypes = currentClasses.filter { it.startsWith("<")}.flatMap {
            it.substring(1, it.length - 2).split("\\s*|\\s*".toRegex())
        }.map { it.trim() }

        if (arrayTypes.isNotEmpty() && parts.getOrNull(index + 1) is ObjJArrayIndexSelector) {
            skipNext = true
            currentClasses = arrayTypes
        }
    }
    return currentClasses
}