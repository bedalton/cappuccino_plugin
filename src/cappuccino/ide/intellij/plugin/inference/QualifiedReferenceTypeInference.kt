package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.ObjJVariableTypeResolver
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.jstypedef.contributor.*
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListFunctionType
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefPropertiesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefTypeAliasIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.*
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariablePsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.getParentBlockChildrenOfType
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.stubs.types.TYPES_DELIM
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.orFalse
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListArrayType as JsTypeListArrayType

internal fun inferQualifiedReferenceType(parts: List<ObjJQualifiedReferenceComponent>, tag: Long): InferenceResult? {
    /*val lastChild = parts.lastOrNull() ?: return null
    return lastChild.getCachedInferredTypes(null) {
        addStatusFileChangeListener(parts[0].project)
        internalInferQualifiedReferenceType(parts, tag)
    }*/
    if (parts.isEmpty())
        return null
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
        parentTypes = part.getCachedInferredTypes(tag) {
            if (part.tagged(tag, false))
                return@getCachedInferredTypes null

            if (parts.size == 1 && parts[0] is ObjJVariableName) {
                val varDefTypeSimple = ObjJCommentEvaluatorUtil.getVariableTypesInParent(parts[0] as ObjJVariableName)
                if (varDefTypeSimple.isNotNullOrBlank() && varDefTypeSimple !in anyTypes) {
                    return@getCachedInferredTypes InferenceResult(
                            types = setOf(varDefTypeSimple!!).toJsTypeList()
                    )
                }
            }
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
                it is JsTypeDefClassElement
            }
        }
    }
    if (parentTypes == null && parts.size == 1 && !ObjJVariablePsiUtil.isNewVarDec(parts[0])) {
        return getFirstMatchesInGlobals(parts[0], tag)
    }
    return parentTypes
}

val SPLIT_JS_CLASS_TYPES_LIST_REGEX = """\s*\$TYPES_DELIM\s*""".toRegex()

internal fun getPartTypes(part: ObjJQualifiedReferenceComponent, parentTypes: InferenceResult?, static: Boolean, tag: Long): InferenceResult? {
    return when (part) {
        is ObjJVariableName -> getVariableNameComponentTypes(part, parentTypes, static, tag)
        is ObjJFunctionCall -> getFunctionComponentTypes(part.functionName, parentTypes, static, tag)
        is ObjJFunctionName -> getFunctionComponentTypes(part, parentTypes, static, tag)
        is ObjJArrayIndexSelector -> getArrayTypes(parentTypes)
        is ObjJMethodCall -> inferMethodCallType(part, tag)
        is ObjJParenEnclosedExpr -> if (part.expr != null) inferExpressionType(part.expr!!, tag) else null
        else -> return null
    }
}


fun getVariableNameComponentTypes(variableName: ObjJVariableName, parentTypes: InferenceResult?, static:Boolean, tag: Long): InferenceResult? {
    //ProgressManager.checkCanceled()
    if (variableName.tagged(tag, false))
        return null

    if (variableName.indexInQualifiedReference == 0) {
        return inferVariableNameType(variableName, tag)
    }

    if (parentTypes == null)
        return null

    val project = variableName.project
    val variableNameString = variableName.text

    if (static) {
        val types = parentTypes.classes.flatMap { className ->
            if (className.contains("&")) {
                className.split("\\s*&\\s*".toRegex()).flatMap {
                    JsTypeDefClassesByNameIndex.instance[className, project].filterIsInstance<JsTypeDefClassElement>().flatMap { classElement ->
                        classElement.propertyList.toNamedPropertiesList() + classElement.functionList.map { it.toJsTypeListType()}
                    }
                }
            } else {
                JsTypeDefClassesByNameIndex.instance[className, project].filterIsInstance<JsTypeDefClassElement>().flatMap { classElement ->
                    classElement.propertyList.toNamedPropertiesList() + classElement.functionList.map { it.toJsTypeListType() }
                } + (if (parentTypes.types.any { it is JsTypeListArrayType }) JsTypeDefClassesByNameIndex.instance["Array", project].flatMap { arrayClass ->
                    arrayClass.propertyList.toNamedPropertiesList() + arrayClass.functionList.map { it.toJsTypeListType() }
                } else emptyList()) + (JsTypeDefTypeAliasIndex.instance[className, project].flatMap { typeAlias ->
                    typeAlias.typesList.functionTypes + typeAlias.typesList.interfaceTypes.flatMap { it.staticProperties } + typeAlias.typesList.interfaceTypes.flatMap { it.staticFunctions }
                })
            }
        }.filter { it.name == variableNameString && it.static == static }
                .flatMap {
                    (it as? JsTypeDefNamedProperty)?.types?.types ?: listOfNotNull( it as? JsTypeListFunctionType )
                }.toSet()
        return InferenceResult(types = types)
    }



    val others:List<JsTypeListType> = parentTypes.types.mapNotNull{ it as? JsTypeListType.JsTypeListBasicType }.flatMap { type ->
        (JsTypeDefTypeAliasIndex.instance[type.typeName, project].flatMap { typeAlias ->
            typeAlias.typesList.functionTypes + typeAlias.typesList.interfaceTypes.flatMap { it.staticProperties } + typeAlias.typesList.interfaceTypes.flatMap { it.staticFunctions }
        })
    }.filter {
        it.name == variableNameString
    }.flatMap {
        if (it is JsTypeDefNamedProperty)
            it.types.types
        if (it is JsTypeListType)
            listOf(it as JsTypeListType)
        else
            emptyList()
    }

    val classes = parentTypes.classes.mapNotNull {
        ProgressManager.checkCanceled()
        getClassDefinition(project, it)
    }.collapseWithSuperType(project)


    val classNames = classes.properties.filter {
            it.name == variableNameString
        }.flatMap { it.types.types }.toSet()
    var functions: List<JsTypeListFunctionType> = classes.functions.filter {
        it.name == variableNameString
    }
    val child = parentTypes.propertyForKey(variableNameString)
    if (child is JsTypeListFunctionType)
        functions = functions + child
    val outTypes = if (child is JsTypeDefNamedProperty)
        classNames + child.types.types
    else
        classNames
    val out = InferenceResult(
            types = outTypes + others,
            nullable = true
    )

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
    if (containingClass != null)
        return InferenceResult(types = setOf(containingClass).toJsTypeList())


    if ((variableName.parent.parent as? ObjJVariableDeclaration)?.hasVarKeyword().orFalse() || variableName.parent is ObjJGlobalVariableDeclaration) {
        return internalInferVariableTypeAtIndexZero(variableName, variableName, containingClass, tag, true)
    }

    val referencedVariable = if (!DumbService.isDumb(variableName.project))
        variableName.reference.resolve(nullIfSelfReferencing = false)
    else
        null

    return referencedVariable?.getCachedInferredTypes(tag) {
        return@getCachedInferredTypes internalInferVariableTypeAtIndexZero(variableName, referencedVariable, containingClass, tag, false)
    }
}

private fun internalInferVariableTypeAtIndexZero(variableName: ObjJVariableName, referencedVariable: PsiElement, containingClass: String?, tag: Long, isVarDec:Boolean): InferenceResult? {
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

    if (referencedVariable.isEquivalentTo(variableName)) {
        val expr = (referencedVariable.parent.parent as? ObjJVariableDeclaration)?.expr
                ?: (referencedVariable.parent as? ObjJGlobalVariableDeclaration)?.expr
        val result = inferExpressionType(expr, tag)
        if (result?.classes?.withoutAnyType().isNotNullOrEmpty())
            return result
    }

    if (referencedVariable is ObjJVariableName) {
        val outTemp = ObjJVariableTypeResolver.resolveVariableType(
                variableName = referencedVariable,
                recurse = false,
                withInheritance = false,
                tag = tag,
                withGeneric = true
        )
        if (outTemp.isNotEmpty()) {
            val out = outTemp.map {
                val parts = it.split("<")
                when {
                    parts.size == 1 -> JsTypeListType.JsTypeListBasicType(it)
                    parts[0].toLowerCase() == "jsobject" -> JsTypeListType.JsTypeListBasicType(parts[1])
                    parts[0].toLowerCase() in arrayTypes -> {
                        val className = if (parts[1].endsWith(">"))
                            parts[1].substringFromEnd(0,1)
                        else
                            parts[1]
                        JsTypeListArrayType(types = setOf(className).toJsTypeList())
                    }
                    else -> JsTypeListType.JsTypeListBasicType(it)
                }
            }
            return InferenceResult(types = out.toSet())
        }
        if (!referencedVariable.isEquivalentTo(variableName)) {
            return inferQualifiedReferenceType(listOf(referencedVariable), tag)
        }
    } else if (referencedVariable is JsTypeDefPropertyName) {
        val result = (referencedVariable.parent as? JsTypeDefProperty)?.toJsNamedProperty()?.types
        if (result != null)
            return result
    } else if (referencedVariable is JsTypeDefFunctionName) {
        val result = (referencedVariable.parent as? JsTypeDefFunction)?.toJsTypeListType()
        if (result != null)
            return InferenceResult(types = setOf(result))
    } else if (referencedVariable is JsTypeDefTypeName) {
        return InferenceResult(types = setOf(referencedVariable.text).toJsTypeList())
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

    if (!DumbService.isDumb(project)) {
        if (JsTypeDefClassesByNameIndex.instance.containsKey(variableNameString, project)) {
            return listOf(variableNameString).toInferenceResult()
        }
    }

    var staticVariableTypes:Set<JsTypeListType>? = null
    if (!isVarDec) {

        val staticVariablesUnfiltered = JsTypeDefPropertiesByNameIndex.instance[variableNameString, project]
        val staticVariables = staticVariablesUnfiltered.filter {
            it.enclosingNamespaceComponents.isEmpty()
        }
        staticVariableTypes = staticVariables.flatMap {
            val namedProperty = it.toJsNamedProperty()
            namedProperty.types.types
        }.toSet()

        if (staticVariableTypes.isNotEmpty()) {
            return InferenceResult(types = staticVariableTypes)
        }

        val className = variableName.text
        if (JsTypeDefClassesByNameIndex.instance.containsKey(className, project)) {
            return InferenceResult(types = setOf(className).toJsTypeList())
        }
    }
    val assignedExpressions = getAllVariableNameAssignmentExpressions(variableName)
    val out = getInferredTypeFromExpressionArray(assignedExpressions, tag)
    return if (staticVariableTypes.isNotNullOrEmpty())
        out.copy(types = out.types + staticVariableTypes!!)
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
    val classNames = (parentTypes.classes + (if (parentTypes.types.any { it is JsTypeListArrayType}) "Array" else null)).filterNotNull()
    val classes = classNames.mapNotNull {
        getClassDefinition(project, it)
    }
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
        it.returnType?.types.orEmpty()
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
    }
    val functionNameString = functionName.text
    val resolved = functionName.reference.resolve()
    val functionDeclaration = resolved?.parentFunctionDeclaration
    if (functionDeclaration == null && resolved is ObjJVariableName) {
        val expr = resolved.getAssignmentExprOrNull()
        if (expr != null) {
            val functionType = inferExpressionType(expr, tag)
            functionType?.functionTypes?.mapNotNull {
                it.returnType
            }?.combine()
        }
    }
    var basicReturnTypes = functionDeclaration
            ?.getReturnTypes(tag)

    val functionTypes = JsTypeDefFunctionsByNameIndex.instance[functionNameString, project].map {
        //ProgressManager.checkCanceled()
        it.toJsTypeListType()
    }.toMutableList()
    val functionDeclarationAsJsFunctionType = functionDeclaration?.toJsFunctionType(tag)
    if (functionDeclarationAsJsFunctionType != null) {
        functionTypes.add(functionDeclarationAsJsFunctionType)
    }

    if (basicReturnTypes.isNullOrEmpty() && functionDeclaration != null) {
        basicReturnTypes = inferFunctionDeclarationReturnType(functionDeclaration, tag)?.classes ?: emptySet()
    }
    val types:MutableSet<JsTypeListType> = basicReturnTypes.orEmpty().toJsTypeList().toMutableSet()
    val returnTypes = functionTypes.flatMap {
        it.returnType?.types ?: emptySet()
    }
    types.addAll(returnTypes)
    types.addAll(functionTypes.map { it })

    return InferenceResult(
            types = types
    )
}

private fun getArrayTypes(parentTypes: InferenceResult?): InferenceResult? {
    if (parentTypes == null) {
        return INFERRED_ANY_TYPE
    }

    var types =  parentTypes.types.flatMap {
        (it as? JsTypeListArrayType)?.types.orEmpty()
    }.toSet()
    if (types.isNotNullOrEmpty()) {
        return InferenceResult(types = types)
    }
    types = parentTypes.arrayTypes.types
    if (types.isNotEmpty()) {
        return InferenceResult(types = types)
    }
    return INFERRED_ANY_TYPE
}

private fun getFirstMatchesInGlobals(part: ObjJQualifiedReferenceComponent, tag: Long): InferenceResult? {
    val project = part.project
    //ProgressManager.checkCanceled()
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