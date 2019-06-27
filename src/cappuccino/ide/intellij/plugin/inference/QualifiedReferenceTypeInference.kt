package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.ObjJVariableTypeResolver
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.jstypedef.contributor.*
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListFunctionType
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefPropertiesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefVariableDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNamedElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.interfaces.getReturnTypes
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.getParentBlockChildrenOfType
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.stubs.types.TYPES_DELIM
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch

internal fun inferQualifiedReferenceType(parts: List<ObjJQualifiedReferenceComponent>, tag: Long): InferenceResult? {
    /*val lastChild = parts.lastOrNull() ?: return null
    return lastChild.getCachedInferredTypes(null) {
        addStatusFileChangeListener(parts[0].project)
        internalInferQualifiedReferenceType(parts, tag)
    }*/
    addStatusFileChangeListener(parts[0].project)
    return internalInferQualifiedReferenceType(parts, tag)
}

internal fun internalInferQualifiedReferenceType(parts: List<ObjJQualifiedReferenceComponent>, tag: Long): InferenceResult? {
    if (parts.isEmpty()) {
        return null
    }
    val project: Project = parts[0].project
    //ProgressManager.checkCanceled()
    var parentTypes: InferenceResult? = null
    var isStatic = false
    for (i in 0 until parts.size) {
        //ProgressManager.checkCanceled()
        val part = parts[i]
        val thisParentTypes = parentTypes
        parentTypes = part.getCachedInferredTypes(tag, false) {
            if (parts.size == 1 && parts[0] is ObjJVariableName) {
                val varDefTypeSimple = ObjJCommentEvaluatorUtil.getVariableTypesInParent(parts[0] as ObjJVariableName)
                if (varDefTypeSimple.isNotNullOrBlank() && varDefTypeSimple !in anyTypes) {
                    return@getCachedInferredTypes InferenceResult(
                            types = setOf(varDefTypeSimple!!).toJsTypeList()
                    )
                }
            }
            //LOGGER.info("QNC <${part.text}> was not cached")
            if (i == parts.size - 1 && (part.parent is ObjJVariableDeclaration || part.parent.parent is ObjJVariableDeclaration)) {
                val variableDeclarationExpr =
                        (part.parent as? ObjJVariableDeclaration ?: part.parent.parent as ObjJVariableDeclaration).expr
                                ?: return@getCachedInferredTypes null
                inferExpressionType(variableDeclarationExpr, tag)
            } else if (i == 0)
                getPartTypes(part, thisParentTypes, false, tag)
            else
                getPartTypes(part, thisParentTypes, isStatic, tag)
        }
        if (isStatic) {
            isStatic = false
        }
        if (i == 0) {
            isStatic = JsTypeDefClassesByNameIndex.instance[part.text, project].any {
                it.isStatic
            }
        }
    }
    if (parentTypes == null && parts.size == 1) {
        return getFirstMatchesInGlobals(parts[0], tag)
    }
    return parentTypes
}

val SPLIT_JS_CLASS_TYPES_LIST_REGEX = """\s*\$TYPES_DELIM\s*""".toRegex()

internal fun getPartTypes(part: ObjJQualifiedReferenceComponent, parentTypes: InferenceResult?, static: Boolean, tag: Long): InferenceResult? {
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


fun getVariableNameComponentTypes(variableName: ObjJVariableName, parentTypes: InferenceResult?, tag: Long): InferenceResult? {
    /*if (level < 0) {
        LOGGER.info("Cannot get variable name part for variable <${variableName.text}> as level < 0")
        return null
    }*/

    //ProgressManager.checkCanceled()
    if (variableName.indexInQualifiedReference == 0) {
        return inferVariableNameType(variableName, tag)
    }
    if (parentTypes == null)
        return null

    val project = variableName.project
    val variableNameString = variableName.text
    val classes = parentTypes.classes.mapNotNull {
        //ProgressManager.checkCanceled()
        getClassDefinition(project, it)
    }
    val classNames = classes.flatMap { jsClass ->
        jsClass.properties.firstOrNull {
            it.name == variableNameString
        }?.types ?: emptySet()
    }.toSet()
    var functions: List<JsTypeListFunctionType> = classes.mapNotNull { jsClass ->
        jsClass.functions.firstOrNull {
            it.name == variableNameString
        }?.toJsTypeListType() ?: return@mapNotNull null
    }


    val child = parentTypes.propertyForKey(variableNameString)
    if (child is JsFunction)
        functions = functions + child.toJsTypeListType()
    val out = if (child is JsTypeDefNamedProperty)
        classNames.toInferenceResult() + child.types
    else
        classNames.toInferenceResult()

    if (functions.isNotEmpty()) {
        return out.copy(
                types = out.types + functions
        )
    }
    return out
}

internal fun inferVariableNameType(variableName: ObjJVariableName, tag: Long): InferenceResult? {
    /*if (level < 0) {
        return null
    }*/
    if (variableName.indexInQualifiedReference != 0) {
        return null
    }
    val containingClass = when (variableName.text) {
        "self" -> variableName.containingClassName
        "super" -> variableName.getContainingSuperClass()?.name
        else -> null
    }
    if ((variableName.parent.parent as? ObjJVariableDeclaration)?.hasVarKeyword().orFalse()) {
        internalInferVariableTypeAtIndexZero(variableName, variableName, containingClass, tag)
    }

    val referencedVariable = if (!DumbService.isDumb(variableName.project))
        variableName.reference.resolve() as? ObjJCompositeElement
    else
        null

    return referencedVariable?.getCachedInferredTypes(tag) {
        return@getCachedInferredTypes internalInferVariableTypeAtIndexZero(variableName, referencedVariable, containingClass, tag)
    }
}

private fun internalInferVariableTypeAtIndexZero(variableName: ObjJVariableName, referencedVariable: ObjJCompositeElement, containingClass: String?, tag: Long): InferenceResult? {
    val project = variableName.project
    val variableNameString: String = variableName.text
    val varDefType = if (referencedVariable is ObjJNamedElement)
        ObjJCommentEvaluatorUtil.getVariableTypesInParent(referencedVariable)
    else
        null

    if (varDefType.isNotNullOrBlank() && varDefType !in anyTypes) {
        return InferenceResult(
                types = setOf(varDefType!!).toJsTypeList()
        )
    }

    if (referencedVariable is ObjJVariableName) {
        val out = ObjJVariableTypeResolver.resolveVariableType(
                variableName = referencedVariable,
                recurse = false,
                withInheritance = false,
                tag = tag
        )
        if (out.isNotEmpty()) {
            return InferenceResult(types = out.toJsTypeList())
        }
    }
    val functionDeclaration = when (referencedVariable) {
        is ObjJFunctionName -> referencedVariable.parentFunctionDeclaration
        is ObjJVariableDeclaration -> referencedVariable.parentFunctionDeclaration
        else -> null
    }

    if (functionDeclaration != null)
        return functionDeclaration.toJsFunctionTypeResult(tag)

    if (containingClass != null)
        return InferenceResult(
                types = setOf(containingClass).toJsTypeList()
        )

    variableName.tagged(tag)
    if (referencedVariable.tagged(tag))
        return null
    val assignedExpressions = getAllVariableNameAssignmentExpressions(variableName)
    val staticVariableNameTypes = JsTypeDefPropertiesByNameIndex.instance[variableNameString, project].filter {
        it.staticKeyword != null &&  it.parent is JsTypeDefVariableDeclaration
    }.flatMap { it.toJsNamedProperty().types }.toJsTypeList()
    val out = getInferredTypeFromExpressionArray(assignedExpressions, tag)
    return if (staticVariableNameTypes.isNotEmpty())
        out.copy(types = out.types + staticVariableNameTypes)
    else
        out
}

fun getAllVariableNameAssignmentExpressions(variableName: ObjJVariableName): List<ObjJExpr> {
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

private fun getAssignedExpressions(element: PsiElement?, variableName: String? = null): ObjJExpr? {
    //ProgressManager.checkCanceled()
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

private fun getFunctionComponentTypes(functionName: ObjJFunctionName?, parentTypes: InferenceResult?, static: Boolean, tag: Long): InferenceResult? {
    if (functionName == null)
        return null
    if (functionName.indexInQualifiedReference == 0) {
        return findFunctionReturnTypesIfFirst(functionName, tag)
    }
    if (parentTypes == null) {
        return null
    }
    val project = functionName.project
    //ProgressManager.checkCanceled()
    val functionNameString = functionName.text
    val classes = parentTypes.classes.mapNotNull {
        getClassDefinition(project, it)
    }

    val functions = classes.mapNotNull { jsClass ->
        //ProgressManager.checkCanceled()
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
        it.returnType.types
    }.toSet()
    return InferenceResult(
            types = returnTypes
    )

}

private fun findFunctionReturnTypesIfFirst(functionName: ObjJFunctionName, tag: Long): InferenceResult? {
    val project:Project = functionName.project
    if (functionName.indexInQualifiedReference != 0) {
        return null
    }
    val functionCall = functionName.getParentOfType(ObjJFunctionCall::class.java)
    if (functionCall != null) {
        val returnType = inferFunctionCallReturnType(functionCall, tag)
        if (returnType != null)
            return returnType
        else
            LOGGER.info("Failed to find quick function call results")
    }
    val functionNameString = functionName.text
    val resolved = functionName.reference.resolve()
    val functionDeclaration = resolved?.parentFunctionDeclaration
    if (functionDeclaration == null && resolved is ObjJVariableName) {
        val expr = resolved.getAssignmentExprOrNull()
        if (expr != null) {
            val functionType = inferExpressionType(expr, tag)
            if (functionType != null) {
                functionType.functionTypes.mapNotNull {
                    it.returnType
                }.combine()
            }
        }
    }
    var basicReturnTypes = functionDeclaration
            ?.getReturnTypes(tag)

    val functionTypes = JsTypeDefFunctionsByNameIndex.instance[functionNameString, project].map {
        //ProgressManager.checkCanceled()
        it.toJsFunctionType()
    }.toMutableList()
    val functionDeclarationAsJsFunctionType = functionDeclaration?.toJsFunctionType(tag)
    if (functionDeclarationAsJsFunctionType != null) {
        functionTypes.add(functionDeclarationAsJsFunctionType)
    }

    if (basicReturnTypes.isNullOrEmpty() && functionDeclaration != null) {
        basicReturnTypes = inferFunctionDeclarationReturnType(functionDeclaration, tag)?.classes ?: emptySet()
    }
    val types:MutableSet<JsTypeListType> = basicReturnTypes?.toJsTypeList()?.toMutableSet() ?: mutableSetOf()
    types.addAll(functionTypes.flatMap {
        it.returnType.types
    })
    types.addAll(functionTypes.map { it.toJsTypeListType() })

    return InferenceResult(
            types = types
    )
}

private fun getArrayTypes(parentTypes: InferenceResult?): InferenceResult? {
    if (parentTypes == null) {
        return INFERRED_ANY_TYPE
    }
    val types = parentTypes.arrayTypes.types
    if (types.isNotEmpty()) {
        return InferenceResult(types = types)
    }
    return INFERRED_ANY_TYPE
}

private fun getFirstMatchesInGlobals(part: ObjJQualifiedReferenceComponent, tag: Long): InferenceResult? {
    val project = part.project
    //ProgressManager.checkCanceled()
    //LOGGER.info("Parts has length of one. Part is ${part.elementType}")
    val name = (part as? ObjJVariableName)?.text ?: (part as? ObjJFunctionName)?.text
    ?: (part as? ObjJFunctionCall)?.functionName?.text
    if (name == null && part is ObjJMethodCall) {
        //LOGGER.info("Part is a method call")
        return inferMethodCallType(part, tag)
    } else if (name == null)
        return INFERRED_ANY_TYPE
    val firstMatches: MutableList<JsTypeListType> = mutableListOf()

    val functions = JsTypeDefFunctionsByNameIndex.instance[name, project].map {
        //ProgressManager.checkCanceled()
        it.toJsTypeListType()
    }.toMutableList()
    firstMatches.addAll(functions)
    val properties = JsTypeDefPropertiesByNameIndex.instance[name, project].flatMap { it.typeList.toJsTypeDefTypeListTypes() }
    firstMatches.addAll(properties)
    //LOGGER.info("First matches are of types: [${firstMatches.joinToString("|")}]")
    if (firstMatches.isEmpty())
        return null
    return InferenceResult(
            types = firstMatches.toSet()
    )
}

private fun getAllVariableAssignmentsWithName(variableName: ObjJVariableName): List<ObjJExpr> {
    //ProgressManager.checkCanceled()
    val variableNameString = variableName.text
    val fromBodyAssignments = variableName.getParentBlockChildrenOfType(ObjJBodyVariableAssignment::class.java, true)
            .flatMap { assignment ->
                //ProgressManager.checkCanceled()
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
                        //ProgressManager.checkCanceled()
                        expr.leftExpr?.variableDeclaration?.qualifiedReferenceList?.mapNotNull {
                            it.qualifiedNameParts.firstOrNull()
                        } ?: emptyList()
                    }.mapNotNull { getAssignedExpressions(it, variableNameString) }
    return fromBodyAssignments + fromGlobals + fromVariableDeclarations
}

fun ObjJVariableName.getAssignmentExprOrNull(): ObjJExpr? {
    return (this.parent as? ObjJGlobalVariableDeclaration)?.expr
            ?: (this.parent.parent as? ObjJVariableDeclaration)?.expr
}