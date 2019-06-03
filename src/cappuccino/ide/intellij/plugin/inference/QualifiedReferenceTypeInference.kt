package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.*
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.interfaces.getReturnTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.orElse
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch

internal fun inferQualifiedReferenceType(parts:List<ObjJQualifiedReferenceComponent>, tag:Long): InferenceResult? {
    val lastChild = parts.lastOrNull() ?: return null
    return lastChild.getCachedInferredTypes {
        addStatusFileChangeListener(parts[0].project)
        internalInferQualifiedReferenceType(parts, tag)
    }
}

internal fun internalInferQualifiedReferenceType(parts:List<ObjJQualifiedReferenceComponent>, tag:Long): InferenceResult? {
    if (parts.isEmpty()) {
        LOGGER.info("Cannot infer qualified reference type without parts")
        return null
    }
    ProgressManager.checkCanceled()
    var parentTypes: InferenceResult? = null
    var isStatic = false
    for (i in 0 until parts.size) {
        ProgressManager.checkCanceled()
        val part = parts[i]
        val thisParentTypes = parentTypes
        parentTypes = part.getCachedInferredTypes {
            //LOGGER.info("QNC <${part.text}> was not cached")
            if (i == parts.size - 1 && (part.parent is ObjJVariableDeclaration ||part.parent.parent is ObjJVariableDeclaration)) {
                val variableDeclarationExpr =
                        (part.parent as? ObjJVariableDeclaration ?: part.parent.parent as ObjJVariableDeclaration).expr
                                ?: return@getCachedInferredTypes null
                LOGGER.info("Parent is Variable Declaration")
                inferExpressionType(variableDeclarationExpr, tag)
            }
            else if (i == 0)
                getPartTypes(part, thisParentTypes, false, tag)
            else
                getPartTypes(part, thisParentTypes, isStatic, tag)
        }
        if (isStatic) {
            isStatic = false
        }
        if (i == 0) {
            isStatic = part.text in globalJSClassNames
        }
    }
    if (parentTypes == null && parts.size == 1) {
        return getFirstMatchesInGlobals(parts[0], tag)
    }
    LOGGER.info("Qualified Name <${parts.joinToString(".")}> resolves to types: [${parentTypes?.toClassListString()?:""}]")
    return parentTypes
}

val SPLIT_JS_CLASS_TYPES_LIST_REGEX = "\\s*\\|\\s*".toRegex()

internal fun getPartTypes(part: ObjJQualifiedReferenceComponent, parentTypes: InferenceResult?, static: Boolean, tag:Long): InferenceResult? {
    return when (part) {
        is ObjJVariableName -> getVariableNameComponentTypes(part, parentTypes, tag)
        is ObjJFunctionCall -> getFunctionComponentTypes(part.functionName, parentTypes, static, tag)
        is ObjJFunctionName -> getFunctionComponentTypes(part, parentTypes, static, tag)
        is ObjJArrayIndexSelector -> getArrayTypes(parentTypes)
        is ObjJMethodCall -> inferMethodCallType(part, tag)
        is ObjJParenEnclosedExpr -> if (part.expr != null) inferExpressionType(part.expr!!, tag) else null
        else -> return null
    }
}


fun getVariableNameComponentTypes(variableName: ObjJVariableName, parentTypes: InferenceResult?, tag:Long): InferenceResult? {
    /*if (level < 0) {
        LOGGER.info("Cannot get variable name part for variable <${variableName.text}> as level < 0")
        return null
    }*/
    if (variableName.tagged(tag)) {
        return null
    }
    ProgressManager.checkCanceled()
    if (variableName.indexInQualifiedReference == 0) {
        //LOGGER.info("Inferring type for variable <${variableName.text}> at index 0")
        return inferVariableNameType(variableName, tag)
    }
    if (parentTypes == null)
        return null

    val project = variableName.project
    val variableNameString = variableName.text
    val classes = if (parentTypes.anyType) {
        globalJSClasses
    } else
        parentTypes.classes.mapNotNull {
            ProgressManager.checkCanceled()
            getJsClassObject(project, it)
        }
    val classNames = classes.flatMap { jsClass ->
        jsClass.properties.firstOrNull {
            it.name == variableNameString
        }?.types ?: emptySet()
    }.toSet()
    val functions = classes.mapNotNull { jsClass ->
        val function = jsClass.functions.firstOrNull {
            it.name == variableNameString
        } ?: return@mapNotNull null
        JsFunctionType(function.parameters.toMap(), function.returns?.types?.toInferenceResult() ?: INFERRED_ANY_TYPE)
    }
    if (functions.isNotEmpty()) {
        classNames.toInferenceResult().copy(
                functionTypes = functions
        )
    }
    return classNames.toInferenceResult()
}

fun List<JsNamedProperty>.toMap() : Map<String, InferenceResult> {
    val out = mutableMapOf<String, InferenceResult>()
    this.forEach {
        out[it.name] = it.types.toInferenceResult()
    }
    return out
}

internal fun inferVariableNameType(variableName: ObjJVariableName, tag:Long): InferenceResult? {
    /*if (level < 0) {
        return null
    }*/
    if (variableName.indexInQualifiedReference != 0) {
        LOGGER.info("Inferrernce failed. 0 indexed variable is not actually at index 0")
        return null
    }
    val variableNameString = variableName.text
    val containingClass = when (variableNameString) {
        "self" -> variableName.containingClassName
        "super" -> variableName.getContainingSuperClass()?.name
        else -> null
    }
    val referencedVariable = if (!DumbService.isDumb(variableName.project))
        variableName.reference.resolve()
    else
        null

    val functionDeclaration = when (referencedVariable) {
        is ObjJFunctionName -> referencedVariable.parentFunctionDeclaration
        is ObjJVariableDeclaration -> referencedVariable.parentFunctionDeclaration
        else -> null
    }

    if (functionDeclaration != null)
        return functionDeclaration.toJsFunctionTypeResult(tag)

    if (referencedVariable is ObjJVariableName) {
        val out = ObjJVariableTypeResolver.resolveVariableType(
                variableName = referencedVariable,
                recurse = false,
                withInheritance = false,
                tag = tag
        )
        if (out.isNotEmpty()) {
            return InferenceResult(classes = out)
        }
    }
    if (containingClass != null)
        return InferenceResult(
                classes = setOf(containingClass)
        )
    val assignedExpressions = getAllVariableNameAssignmentExpressions(variableName)
    LOGGER.info ("Found ${assignedExpressions.size} expressions possibly related to variable name: <$variableNameString>")
    val staticVariableNameTypes = ObjJGlobalJSVariables.filter {
        it.name == variableNameString
    }.flatMap { it.types }
    val out = getInferredTypeFromExpressionArray(assignedExpressions, tag)
    return if (staticVariableNameTypes.isNotEmpty())
        out.copy(classes = out.classes + staticVariableNameTypes)
    else
        out
}

fun getAllVariableNameAssignmentExpressions(variableName: ObjJVariableName) : List<ObjJExpr> {
    val referencedVariable = if (!DumbService.isDumb(variableName.project))
        variableName.reference.resolve()
    else
        null
    val thisAssignedExpression = getAssignedExpressions(variableName)
    var assignedExpressions = if (referencedVariable is ObjJVariableName) {
        ReferencesSearch.search(referencedVariable)
                .findAll()
                .mapNotNull { getAssignedExpressions(it.element) }
    } else emptyList()
    if (assignedExpressions.isEmpty()) {
        assignedExpressions = getAllVariableAssignmentsWithName(variableName)
    }
    if (thisAssignedExpression != null)
        return assignedExpressions + thisAssignedExpression
    return assignedExpressions
}

private fun getAssignedExpressions(element: PsiElement?, variableName:String? = null): ObjJExpr? {
    ProgressManager.checkCanceled()
    return if (element == null || (variableName != null && element.text != variableName))
        null
    else if (element.parent is ObjJGlobalVariableDeclaration)
        (element.parent as ObjJGlobalVariableDeclaration).expr
    else if (element.parent !is ObjJQualifiedReference)
        null
    else if (element.parent.parent is ObjJVariableDeclaration)
        (element.parent.parent as ObjJVariableDeclaration).expr
    else
        null
}

private fun getFunctionComponentTypes(functionName: ObjJFunctionName?, parentTypes: InferenceResult?, static:Boolean, tag:Long): InferenceResult? {
    if (functionName == null)
        return null
    if (functionName.indexInQualifiedReference == 0) {
        return findFunctionReturnTypesIfFirst(functionName, tag)
    }
    if (parentTypes == null) {
        return null
    }
    ProgressManager.checkCanceled()
    val functionNameString = functionName.text
    val classes = if (parentTypes.anyType) {
        globalJSClasses
    } else
        parentTypes.classes.mapNotNull{getJsClassObject(functionName.project, it)}

    val functions = classes.mapNotNull { jsClass ->
        ProgressManager.checkCanceled()
        (if (static)
            jsClass.staticFunctions.firstOrNull {
                it.name == functionNameString
            }
        else
            jsClass.functions.firstOrNull {
                it.name == functionNameString
            })
    }
    val returnTypes = functions.flatMap {
        it.returnTypes
    }
    return InferenceResult (
            classes = returnTypes.toSet(),
            functionTypes = functions.map {
                val returnType = InferenceResult (
                        classes = it.returnTypes.toSet()
                )
                JsFunctionType(it.parameters.toMap(), returnType)
            }
    )

}

private fun findFunctionReturnTypesIfFirst(functionName: ObjJFunctionName, tag:Long): InferenceResult? {
    if (functionName.indexInQualifiedReference != 0) {
        return null
    }
    val functionNameString = functionName.text
    val resolved =  functionName.reference.resolve()
    val functionDeclaration = resolved?.parentFunctionDeclaration
    if (functionDeclaration == null && resolved is ObjJVariableName) {
        val expr = resolved.getAssignmentExprOrNull()
        if (expr != null) {
            val functionType = inferExpressionType(expr, tag)
            if (functionType != null) {
                return functionType.functionTypes?.map {
                    it.returnType
                }?.collapse() ?: functionType
            }
        }
    }
    var basicReturnTypes = functionDeclaration
            ?.getReturnTypes(tag)

    val functions = globalJsFunctions.filter {
        ProgressManager.checkCanceled()
        it.name == functionNameString
    }
    val functionTypes = functions.map {
        val returnType = InferenceResult(
                classes = it.returnTypes
        )
        JsFunctionType(it.parameters.toMap(), returnType)
    }.toMutableSet()

    val functionDeclarationAsJsFunctionType = functionDeclaration?.toJsFunctionType(tag)
    if (functionDeclarationAsJsFunctionType != null) {
        functionTypes.add(functionDeclarationAsJsFunctionType)
    }

    if (basicReturnTypes.isNullOrEmpty() && functionDeclaration != null) {
        basicReturnTypes = inferFunctionDeclarationReturnType(functionDeclaration, tag)?.classes ?: emptySet()
    }
    val returnTypes = functions.flatMap {
        it.returnTypes
    } + basicReturnTypes.orEmpty()

    return InferenceResult (
            classes = returnTypes.toSet(),
            functionTypes = functionTypes.toList()
    )
}

private fun getArrayTypes(parentTypes: InferenceResult?): InferenceResult? {
    if (parentTypes == null) {
        return INFERRED_ANY_TYPE
    }
    val types = (parentTypes.arrayTypes?.toMutableList() ?: mutableListOf()).filterNot {
        val value = it.toLowerCase()
        value == "null" || value == "nil" || value == "undefined"
    }
    if (parentTypes.arrayTypes?.size.orElse(0) < 1
            && parentTypes.classes.size == 1
            && parentTypes.classes.iterator().next() in stringTypes
    ) {
        return InferenceResult(
                isString = true,
                classes = setOf(JS_STRING.className),
                arrayTypes = setOf(JS_STRING.className)
        )
    }
    if (types.isNotEmpty()) {
        return types.toInferenceResult()
    }
    return INFERRED_ANY_TYPE
}

private fun getFirstMatchesInGlobals(part:ObjJQualifiedReferenceComponent, tag:Long) : InferenceResult? {
    ProgressManager.checkCanceled()
    //LOGGER.info("Parts has length of one. Part is ${part.elementType}")
    val name = (part as? ObjJVariableName)?.text ?: (part as? ObjJFunctionName)?.text ?: (part as? ObjJFunctionCall)?.functionName?.text
    if (name == null && part is ObjJMethodCall) {
        //LOGGER.info("Part is a method call")
        return inferMethodCallType(part, tag)
    } else if (name == null)
        return INFERRED_ANY_TYPE
    val firstMatches:MutableList<String> = mutableListOf()
    val functions = globalJsFunctions.filter { it.name == name }.flatMap { it.returnTypes }
    firstMatches.addAll(functions)
    val properties = ObjJGlobalJSVariables.filter { it.name == name }.flatMap { it.types }
    firstMatches.addAll(properties)
    //LOGGER.info("First matches are of types: [${firstMatches.joinToString("|")}]")
    if (firstMatches.isEmpty())
        return null
    return InferenceResult(
            classes = firstMatches.toSet()
    )
}

private fun getAllVariableAssignmentsWithName(variableName:ObjJVariableName) : List<ObjJExpr> {
    ProgressManager.checkCanceled()
    val variableNameString = variableName.text
    val fromBodyAssignments = variableName.getParentBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true)
            .flatMap { assignment ->
                ProgressManager.checkCanceled()
                listOf(assignment.variableAssignmentLogical?.qualifiedReference?.qualifiedNameParts?.firstOrNull()) +
                    assignment.variableDeclarationList?.variableNameList.orEmpty() +
                    assignment.variableDeclarationList?.variableDeclarationList?.flatMap { objJVariableDeclaration ->
                        objJVariableDeclaration.qualifiedReferenceList.mapNotNull {
                            it.qualifiedNameParts.firstOrNull()
                        }
                    }.orEmpty()
            }
            .mapNotNull { getAssignedExpressions(it, variableNameString) }

    val fromGlobals = if (!DumbService.isDumb(variableName.project))
        ObjJGlobalVariableNamesIndex.instance[variableNameString, variableName.project].mapNotNull { it.expr }
    else
        variableName.containingObjJFile?.getFileChildrenOfType(ObjJGlobalVariableDeclaration::class.java, false)
                ?.filter { it.variableName.text == variableNameString }
                ?.mapNotNull { it.expr }.orEmpty()
    val fromVariableDeclarations =
            variableName.getParentBlockChildrenOfType(ObjJExpr::class.java, true)
                    .flatMap { expr ->
                        ProgressManager.checkCanceled()
                        expr.leftExpr?.variableDeclaration?.qualifiedReferenceList?.mapNotNull {
                        it.qualifiedNameParts.firstOrNull()
                    } ?: emptyList()
            }.mapNotNull { getAssignedExpressions(it, variableNameString) }
    return fromBodyAssignments + fromGlobals + fromVariableDeclarations
}

fun ObjJVariableName.getAssignmentExprOrNull() : ObjJExpr? {
    return (this.parent as? ObjJGlobalVariableDeclaration)?.expr ?: (this.parent.parent as? ObjJVariableDeclaration)?.expr
}