package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.ObjJIgnoreEvaluatorUtil
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.orFalse
import cappuccino.ide.intellij.plugin.utils.orTrue
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement


object ObjJVariableTypeResolver {

    fun resolveVariableType(variableName: ObjJVariableName, recurse:Boolean = true, tag:Long,  withInheritance:Boolean = false): Set<String> {
        if (ObjJPluginSettings.resolveCallTargetFromAssignments && !ObjJIgnoreEvaluatorUtil.isInferDisabled(variableName, variableName.text)) {
            return resolveVariableTypeWithoutMethodParse(variableName, recurse, tag, withInheritance)
        }
        return setOf()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun resolveVariableTypeWithoutMethodParse(variableName: ObjJVariableName, recurse: Boolean = true, tag:Long, withInheritance:Boolean = false) : Set<String> {
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
        if (classNames.isNotNullOrEmpty()) {
            return classNames!!
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

    private fun getVariableTypeFromAssignments(variableName:ObjJVariableName, recurse: Boolean, tag:Long) : Set<String>? {
        val fromBodyVariableAssignments = getVariableTypeFromBodyVariableAssignments(variableName, recurse, tag)
        if (fromBodyVariableAssignments?.isNotEmpty().orFalse()) {
            return fromBodyVariableAssignments
        }
        val fromExpressionAssignments = getVariableTypeFromExpressionAssignments(variableName, recurse, tag)
        if (fromExpressionAssignments?.isNotEmpty().orFalse()) {
            return fromExpressionAssignments
        }
        return null
    }

    private fun getVariableTypeFromBodyVariableAssignments(variableName: ObjJVariableName, recurse: Boolean, tag: Long) : Set<String>? {
        if (variableName.indexInQualifiedReference != 0) {
            return null
        }
        val variableNameText = variableName.text
        val assignmentsRaw = variableName.getParentBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true)
                .flatMap { it.variableDeclarationList?.variableDeclarationList ?: emptyList() }
                .filter { it.qualifiedReferenceList.any { q -> q.text == variableNameText } }

        val out = mutableSetOf<String>()
        for (variableDeclaration in assignmentsRaw) {
            val set = getVariableTypeFromAssignment(variableDeclaration, recurse, tag) ?: continue
            out.addAll(set)
        }
        return out.toSet()
    }

    private fun getVariableTypeFromExpressionAssignments(variableName: ObjJVariableName, recurse: Boolean, tag:Long) : Set<String>? {
        val variableNameString = variableName.text
        val assignmentsRaw:List<ObjJVariableDeclaration> = variableName
                .getParentBlockChildrenOfType(ObjJExpr::class.java, true)
                .mapNotNull {expr ->  expr.leftExpr?.variableDeclaration }
                // Convoluted check to ensure that any of the qualified references listed in a compound expression
                // contain a variable with this name
                .filter {
                    it.qualifiedReferenceList.any { qRef ->
                        qRef.variableNameList.size == 1 && qRef.variableNameList.getOrNull(0)?.text == variableNameString
                    }
                }

        val out = mutableSetOf<String>()
        assignmentsRaw.forEach {
            val set = getVariableTypeFromAssignment(it, recurse, tag) ?: return@forEach
            out.addAll(set)
        }
        return out
    }

    private fun getVariableTypeFromAssignment(variableDeclaration:ObjJVariableDeclaration, recurse: Boolean, tag:Long) : Set<String>? {
        val expression = variableDeclaration.expr
        val leftExpr = expression?.leftExpr ?: return null
        var out:Set<String>? = getTypeFromMethodCall(leftExpr, tag)
        if (out != null) {
            return out
        }
        out = getTypeFromQualifiedReferenceAssignment(leftExpr, recurse, tag)
        if (out != null) {
            return out
        }
        return null
    }

    private fun getTypeFromMethodCall(leftExpression: ObjJLeftExpr, tag:Long) : Set<String>? {
        val methodCall = leftExpression.methodCall ?: return null
        val selector = methodCall.selectorString
        val skipIf = listOf(
                "null",
                "nil"
        )
        val out = ObjJUnifiedMethodIndex.instance[selector, methodCall.project].flatMap { it.getReturnTypes(tag) }.toSet().filterNot{ returnType ->
            returnType.toLowerCase() in skipIf ||returnType == "id" || returnType.contains("<")
        }.toSet()
        return if (out.isNotEmpty()) out else null
    }

    private fun getTypeFromQualifiedReferenceAssignment(leftExpression: ObjJLeftExpr, recurse:Boolean, tag:Long) : Set<String>? {
        if (!recurse) {
            return null
        }
        val qualifiedAssignment = leftExpression.qualifiedReference ?: return null
        val lastVar = qualifiedAssignment.lastVar ?: return null
        return resolveVariableType(lastVar, false, tag)
    }

    private fun getPossibleCallTargetTypesFromFormalVariableTypes(callTargetVariableName:ObjJVariableName): Set<String>? {
        val formalVariableType = getPossibleCallTargetTypesFromFormalVariableTypesRaw(callTargetVariableName) ?: return null
        return if (formalVariableType.varTypeId?.className != null) {
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


private fun resolveExpressionType(expr:ObjJExpr) : List<String> {

    //if (expr.leftExpr?.qualifiedReference != null)
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
    //out.addAll(expr.numberTypes)
    return out
}

private val ObjJExpr.isString:Boolean get() {
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

private val ObjJExpr.isBool:Boolean get() {
    val rightExpressions = this.rightExprList
    if (rightExpressions.isEmpty())
        return false
    for(rightExpr in rightExpressions) {
        if (rightExpr?.logicExprPrime != null)
            return true
    }
    return false
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