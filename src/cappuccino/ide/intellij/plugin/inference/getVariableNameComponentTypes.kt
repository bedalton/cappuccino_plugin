package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.ObjJVariableTypeResolver
import cappuccino.ide.intellij.plugin.contributor.objJClassAsJsClass
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.jstypedef.contributor.*
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefPropertiesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefTypeAliasIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNamedElement
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.getParentBlockChildrenOfType
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.orFalse
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch


fun getVariableNameComponentTypes(variableName: ObjJVariableName, parentTypes: InferenceResult?, static: Boolean, tag: Long): InferenceResult? {
    //ProgressManager.checkCanceled()
    if (variableName.tagged(tag, false)) {
        LOGGER.info("TAGGED")
        return null
    }

    if (variableName.indexInQualifiedReference == 0) {
        LOGGER.info("Variable is at index 0")
        return inferVariableNameTypeAtIndexZero(variableName, tag)
    }

    if (parentTypes == null) {
        LOGGER.info("Parent types is null")
        return null
    } else {
        LOGGER.info("Parent types is ${parentTypes.types}")
    }

    val project = variableName.project
    val variableNameString = variableName.text
    val basicTypes = parentTypes.classes.flatMap { className ->
        LOGGER.info("Getting parentType: $className for variable $variableNameString")
        getTypesForClass(variableNameString, className, static, parentTypes, project)
    }.toSet()

    // If static, stop here
    if (static) {
        LOGGER.info("Type is static")
        return InferenceResult(types = basicTypes)
    }
    val properties = parentTypes.properties
            .filter {
                LOGGER.info("Checking property: ${it.name}")
                it.static == static && it.name == variableNameString
            }

    LOGGER.info("Found ${properties.size} properties with name")

    val functions: List<JsTypeListType.JsTypeListFunctionType> = parentTypes.functionTypes +
            properties.filterIsInstance(JsTypeListType.JsTypeListFunctionType::class.java)
    LOGGER.info("Found ${functions.size} functions with name")

    val propertyTypes = properties
            .filterIsInstance(JsTypeDefNamedProperty::class.java)
            .flatMap { it.types.types }
    LOGGER.info("Found ${propertyTypes.size} property types for $variableNameString")
    val outTypes = basicTypes + functions + propertyTypes
    return if (outTypes.isEmpty()) {
        LOGGER.info("Out types for $variableNameString is empty")
        null
    } else {
        LOGGER.info("There are ${outTypes.size} out types for $variableNameString")
        InferenceResult(
                types = outTypes,
                nullable = true
        )
    }
}

private fun getTypesForClass(variableNameString: String, className: String, static: Boolean, parentTypes: InferenceResult, project: Project): Set<JsTypeListType> {
    val allPropertiesRaw: List<JsNamedProperty> =
            if (className.contains("&")) {
                LOGGER.info("ClassName: $className contains &")
                className.split("\\s*&\\s*".toRegex()).flatMap {
                    JsTypeDefClassesByNameIndex.instance[className, project].filterIsInstance<JsTypeDefClassElement>().flatMap { classElement ->
                        classElement.propertyList.toNamedPropertiesList() + classElement.functionList.map { it.toJsFunctionType() }
                    }
                }
            } else {
                LOGGER.info("Getting properties for: $className")
                val objjProperties:Set<JsNamedProperty>? = if (!static) {
                    objJClassAsJsClass(project, className)?.let {
                        val classType = listOf(it).collapseWithSuperType(project)
                        classType.properties
                    }
                } else null
                val allClassPropertiesRaw: List<JsNamedProperty> = JsTypeDefClassesByNameIndex.instance[className, project].flatMap { classElement ->
                    classElement.propertyList.toNamedPropertiesList() + classElement.functionList.map { it.toJsFunctionType() }
                } + objjProperties.orEmpty()
                val arrayPropertiesIfNeededRaw =
                        if (parentTypes.types.any { it is JsTypeListType.JsTypeListArrayType })
                            JsTypeDefClassesByNameIndex.instance["Array", project].flatMap { arrayClass ->
                                arrayClass.propertyList.toNamedPropertiesList() + arrayClass.functionList.map { it.toJsFunctionType() }
                            }
                        else
                            emptyList()
                val otherPropertiesRaw: List<JsNamedProperty> = JsTypeDefTypeAliasIndex.instance[className, project].flatMap { typeAlias ->
                    typeAlias.typesList.functionTypes +
                            typeAlias.typesList.interfaceTypes.flatMap { if (static) it.staticProperties else it.instanceProperties } +
                            typeAlias.typesList.interfaceTypes.flatMap { if (static) it.staticFunctions else it.instanceFunctions }
                }
                allClassPropertiesRaw + arrayPropertiesIfNeededRaw + otherPropertiesRaw
            } + getJsTypeDefAliasProperties(className, static, project)
    LOGGER.info("AllTypes for $variableNameString: $allPropertiesRaw")
    return allPropertiesRaw.filter { it.name == variableNameString && it.static == static }
            .flatMap {
                (it as? JsTypeDefNamedProperty)?.types?.types
                        ?: listOfNotNull(it as? JsTypeListType.JsTypeListFunctionType)
            }.toSet()
}

private fun getJsTypeDefAliasProperties(className:String, static:Boolean, project:Project) : List<JsNamedProperty> {
    return (JsTypeDefTypeAliasIndex.instance[className, project].flatMap { typeAlias ->
        typeAlias.typesList.functionTypes +
                typeAlias.typesList.interfaceTypes.flatMap {
                    if (static) it.staticProperties else it.instanceProperties
                } +
                typeAlias.typesList.interfaceTypes.flatMap {
                    if (static) it.staticFunctions else it.instanceFunctions
                }
    })
}

/**
 * Infers a variable name type, wherever it is in a qualified name
 */
internal fun inferVariableNameTypeAtIndexZero(variableName: ObjJVariableName, tag: Long): InferenceResult? {
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

/**
 * Infers the type for a variable name element at qualified name index zero
 */
private fun internalInferVariableTypeAtIndexZero(variableName: ObjJVariableName, referencedVariable: PsiElement, containingClass: String?, tag: Long, isVarDec: Boolean): InferenceResult? {
    val project = variableName.project
    val variableNameString: String = variableName.text
    val varDefType = if (referencedVariable is ObjJNamedElement)
        ObjJCommentEvaluatorUtil.getVariableTypesInParent(referencedVariable)
    else
        null

    // If var def type is not null, return it
    if (varDefType.isNotNullOrBlank() && varDefType !in anyTypes) {
        return InferenceResult(
                types = setOf(varDefType!!).toJsTypeList()
        )
    }

    // If variable resolved to self,
    // Get assigned expression type if any
    if (referencedVariable == variableName) {
        LOGGER.info("Variable: ${variableName.text} references self in inference")
        val expr = (referencedVariable.parent.parent as? ObjJVariableDeclaration)?.expr
                ?: (referencedVariable.parent as? ObjJGlobalVariableDeclaration)?.expr
        val result = inferExpressionType(expr, tag)
        if (result?.classes?.withoutAnyType().isNotNullOrEmpty())
            return result
    }

    // Resolved variable is not self referencing, so resolve it
    val referencedVariableInferenceResult = inferReferencedElementTypeAtIndexZero(variableName, referencedVariable, tag);
    if (referencedVariableInferenceResult != null)
        return referencedVariableInferenceResult

    if (containingClass != null) {
        return InferenceResult(
                types = setOf(containingClass).toJsTypeList()
        )
    }

    if (!DumbService.isDumb(project)) {
        if (JsTypeDefClassesByNameIndex.instance.containsKey(variableNameString, project)) {
            return listOf(variableNameString).toInferenceResult()
        }
    }

    if (!isVarDec) {
        val result = inferVariableTypeIfNotVarDeclaration(variableNameString, project)
        if (result != null)
            return result
    }
    val assignedExpressions = getAllVariableNameAssignmentExpressions(variableName)
    return getInferredTypeFromExpressionArray(assignedExpressions, tag)
}

/**
 * Infers the type of a referenced PSI element if at index zero
 */
private fun inferReferencedElementTypeAtIndexZero(variableName: ObjJVariableName, referencedVariable: PsiElement, tag: Long): InferenceResult? {
    // If reference resolved to a variable name (as opposed to function)
    return if (referencedVariable is ObjJVariableName) {
        inferReferencedVariableNameAtIndexZero(variableName, referencedVariable, tag)
    } else if (referencedVariable is JsTypeDefPropertyName) {
        (referencedVariable.parent as? JsTypeDefProperty)?.toJsNamedProperty()?.types
    } else if (referencedVariable is JsTypeDefFunctionName) {
        val result = (referencedVariable.parent as? JsTypeDefFunction)?.toJsFunctionType()
        result?.let { InferenceResult(types = setOf(result)) }
    } else if (referencedVariable is JsTypeDefTypeName) {
        InferenceResult(types = setOf(referencedVariable.text).toJsTypeList())
    } else {
        inferIfIsReferenceToFunctionDeclaration(referencedVariable, tag)
    }
}

/**
 * Infers a referenced variable's type if it is at index zero
 */
private fun inferReferencedVariableNameAtIndexZero(variableName: ObjJVariableName, referencedVariable: ObjJVariableName, tag: Long): InferenceResult? {
    // Use old fashioned type resolved
    val outTemp = ObjJVariableTypeResolver.resolveVariableType(
            variableName = referencedVariable,
            recurse = false,
            withInheritance = false,
            tag = tag,
            withGeneric = true
    )
    if (outTemp.isNotEmpty()) {
        val out = outTemp.map {
            buildTypeDefTypeFromGenericParameterIfNecessary(it)
        }
        return InferenceResult(types = out.toSet())
    }
    if (!referencedVariable.isEquivalentTo(variableName)) {
        return inferQualifiedReferenceType(listOf(referencedVariable), tag)
    }
    return null
}

/**
 * Infer referenced variable type, if it is a function declaration
 */
private fun inferIfIsReferenceToFunctionDeclaration(referencedVariable: PsiElement, tag: Long): InferenceResult? {
    val functionDeclaration = when (referencedVariable) {
        is ObjJFunctionName -> referencedVariable.parentFunctionDeclaration
        is ObjJVariableDeclaration -> referencedVariable.parentFunctionDeclaration
        else -> return null
    }

    if (functionDeclaration != null) {
        val result = functionDeclaration.toJsFunctionType(tag)
        return InferenceResult(types = setOf(result))
    }
    return null
}

/**
 * Converts a class string containing a generic <T> param to its generic type
 * returns unaltered string if not containing generic
 */
private fun buildTypeDefTypeFromGenericParameterIfNecessary(it: String): JsTypeListType {
    val parts = it.split("<")
    return when {
        parts.size == 1 -> JsTypeListType.JsTypeListBasicType(it)
        parts[0].toLowerCase() == "jsobject" -> {
            if (parts[1].endsWith(">"))
                JsTypeListType.JsTypeListBasicType(parts[1].substringFromEnd(0, 1))
            else
                JsTypeListType.JsTypeListBasicType(parts[1])
        }
        parts[0].toLowerCase() in arrayTypes -> {
            val className = if (parts[1].endsWith(">"))
                parts[1].substringFromEnd(0, 1)
            else
                parts[1]
            JsTypeListType.JsTypeListArrayType(types = setOf(className).toJsTypeList())
        }
        else -> JsTypeListType.JsTypeListBasicType(it)
    }
}

/**
 * Infers variable type if referenced variable is not a variable declaration
 */
private fun inferVariableTypeIfNotVarDeclaration(variableNameString: String, project: Project): InferenceResult? {
    val staticVariablesUnfiltered = JsTypeDefPropertiesByNameIndex.instance[variableNameString, project]
    val staticVariables = staticVariablesUnfiltered.filter {
        it.enclosingNamespaceComponents.isEmpty()
    }
    val staticVariableTypes = staticVariables.flatMap {
        val namedProperty = it.toJsNamedProperty()
        namedProperty.types.types
    }.toSet()

    if (staticVariableTypes.isNotEmpty()) {
        return InferenceResult(types = staticVariableTypes)
    }

    if (JsTypeDefClassesByNameIndex.instance.containsKey(variableNameString, project)) {
        return InferenceResult(types = setOf(variableNameString).toJsTypeList())
    }
    return null
}

/**
 * Gets all variable assignments related to the given variable name
 */
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


/**
 * Resolved variable name reference, and gets all expressions assigned to it
 */
private fun getAllVariableNameAssignmentExpressions(variableName: ObjJVariableName): List<ObjJExpr> {
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

/**
 * Gets the expression element assigned to the given element
 */
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