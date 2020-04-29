package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.ObjJVariableTypeResolver
import cappuccino.ide.intellij.plugin.indices.ObjJAssignedVariableNamesByBlockIndex
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListFunctionType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.toJsNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefPropertiesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefVariableDeclarationsByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJNamedElement
import cappuccino.ide.intellij.plugin.psi.utils.ReferencedInScope
import cappuccino.ide.intellij.plugin.psi.utils.docComment
import cappuccino.ide.intellij.plugin.psi.utils.getParentBlockChildrenOfType
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch


fun getVariableNameComponentTypes(variableName: ObjJVariableName, parentTypes: InferenceResult?, static: Boolean, tag: Tag): InferenceResult? {
    //ProgressManager.checkCanceled()
    if (variableName.tagged(tag, false)) {
        return null
    }
    ProgressIndicatorProvider.checkCanceled()
    if (variableName.indexInQualifiedReference == 0) {
        return inferVariableNameTypeAtIndexZero(variableName, tag)
    }

    if (parentTypes == null) {
        return null
    }
    val project = variableName.project
    val variableNameString = variableName.text
    val basicTypes: Set<JsTypeListType> = getAllPropertyTypesWithNameInParentTypes(variableNameString, parentTypes, static, project)

    // If static, stop here
    if (static) {
        return InferenceResult(types = basicTypes)
    }
    val properties = parentTypes.properties
            .filter {
                it.static == static && it.name == variableNameString
            }
    val functions: List<JsTypeListFunctionType> = parentTypes.functionTypes +
            properties.filterIsInstance(JsTypeListFunctionType::class.java)

    val propertyTypes = properties
            .filterIsInstance(JsTypeDefNamedProperty::class.java)
            .flatMap { it.types.types }

    val variableDecs = JsTypeDefVariableDeclarationsByNamespaceIndex.instance[variableNameString, project].flatMap {
        it.typeListTypes
    }.toSet()

    val outTypes = basicTypes + functions + propertyTypes + variableDecs
    return if (outTypes.isEmpty()) {
        null
    } else {
        InferenceResult(
                types = outTypes,
                nullable = true
        )
    }
}

/**
 * Infers a variable name type, wherever it is in a qualified name
 */
internal fun inferVariableNameTypeAtIndexZero(variableName: ObjJVariableName, tag: Tag): InferenceResult? {
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

    (variableName.parent as? ObjJGlobalVariableDeclaration)?.let {
        return inferExpressionType(it.expr, tag)
    }
    (variableName.parent?.parent as? ObjJVariableDeclaration)?.let {
        if (it.hasVarKeyword()) {
            return inferExpressionType(it.expr, tag)
        }
    }

    if (variableName.getParentOfType(ObjJQualifiedReference::class.java)?.qualifiedNameParts?.size == 1) {
        getPrevDeclarationType(variableName, tag)?.let {
            if (it.withoutAnyType().isNotEmpty())
                return it
        }
    }

    val referencedVariable = if (!DumbService.isDumb(variableName.project))
        variableName.reference.resolve(nullIfSelfReferencing = false)
    else
        null

    return referencedVariable?.getCachedInferredTypes(tag) {
        return@getCachedInferredTypes internalInferVariableTypeAtIndexZero(variableName, referencedVariable, containingClass, tag, false)
    }
}

private fun getPrevDeclarationType(variableName: ObjJVariableName, tag: Tag): InferenceResult? {
    var block: ObjJBlock? = variableName.getParentOfType(ObjJBlock::class.java)
            ?: return null
    val variableNameStartOffset = variableName.textRange.startOffset
    val project = variableName.project
    val file = variableName.containingFile
    val variableNameString = variableName.text
    while (block != null) {
        val expressions = ObjJAssignedVariableNamesByBlockIndex.instance
                .getInRangeFuzzy(file, variableNameString, block.textRange, project)
                .filter { it.getParentOfType(ObjJQualifiedReference::class.java)?.qualifiedNameParts?.size == 1 }
                .mapNotNull { it.getAssignmentExprOrNull() }
                .filter {
                    it.commonScope(variableName) != ReferencedInScope.UNDETERMINED
                            && variableNameStartOffset > it.textRange.startOffset
                }
                .sortedBy {
                    it.startOffsetInParent
                }
                .reversed()
        for (expr in expressions) {
            inferExpressionType(expr, tag)?.let {
                return it
            }
        }
        block = block.getParentOfType(ObjJBlock::class.java)
    }
    return null
}

/**
 * Infers the type for a variable name element at qualified name index zero
 */
private fun internalInferVariableTypeAtIndexZero(variableName: ObjJVariableName, referencedVariable: PsiElement, containingClass: String?, tag: Tag, isVariableDec: Boolean): InferenceResult? {
    val project = variableName.project
    val variableNameString: String = variableName.text
    val variableDefType = if (referencedVariable is ObjJNamedElement)
        ObjJCommentEvaluatorUtil.getVariableTypesInParent(referencedVariable)
    else
        null

    // If var def type is not null, return it
    if (variableDefType.isNotNullOrBlank() && variableDefType !in anyTypes) {
        return InferenceResult(
                types = setOf(variableDefType!!).toJsTypeList()
        )
    }

    // If variable resolved to self,
    // Get assigned expression type if any
    if (referencedVariable == variableName) {

        val docCommentTypes = variableName.docComment
                ?.getParameterComment(variableNameString)
                ?.types
        if (docCommentTypes != null && docCommentTypes.types.isNotEmpty())
            return docCommentTypes
        val expr = (referencedVariable.parent.parent as? ObjJVariableDeclaration)?.expr
                ?: (referencedVariable.parent as? ObjJGlobalVariableDeclaration)?.expr
        val result = inferExpressionType(expr, tag)
        if (result?.classes?.withoutAnyType().isNotNullOrEmpty())
            return result
    }

    // Resolved variable is not self referencing, so resolve it
    val referencedVariableInferenceResult = inferReferencedElementTypeAtIndexZero(variableName, referencedVariable, tag)
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

    if (!isVariableDec) {
        val result = inferVariableTypeIfNotVariableDeclaration(variableNameString, project)
        if (result != null)
            return result
    }
    val assignedExpressions = getAllVariableNameAssignmentExpressions(variableName)
    return getInferredTypeFromExpressionArray(assignedExpressions, tag)
}

/**
 * Infers the type of a referenced PSI element if at index zero
 */
private fun inferReferencedElementTypeAtIndexZero(variableName: ObjJVariableName, referencedVariable: PsiElement, tag: Tag): InferenceResult? {
    // If reference resolved to a variable name (as opposed to function)
    return when (referencedVariable) {
        is ObjJVariableName -> inferReferencedVariableNameAtIndexZero(variableName, referencedVariable, tag)
        is JsTypeDefPropertyName -> (referencedVariable.parent as? JsTypeDefProperty)?.toJsNamedProperty()?.types
        is JsTypeDefFunctionName -> {
            val result = (referencedVariable.parent as? JsTypeDefFunction)?.toJsFunctionType()
            result?.let { InferenceResult(types = setOf(result)) }
        }
        is JsTypeDefTypeName -> InferenceResult(types = setOf(referencedVariable.text).toJsTypeList())
        else -> inferIfIsReferenceToFunctionDeclaration(referencedVariable, tag)
    }
}

/**
 * Infers a referenced variable's type if it is at index zero
 */
private fun inferReferencedVariableNameAtIndexZero(variableName: ObjJVariableName, referencedVariable: ObjJVariableName, tag: Tag): InferenceResult? {
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
private fun inferIfIsReferenceToFunctionDeclaration(referencedVariable: PsiElement, tag: Tag): InferenceResult? {
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
private fun inferVariableTypeIfNotVariableDeclaration(variableNameString: String, project: Project): InferenceResult? {
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
                ProgressIndicatorProvider.checkCanceled()
                listOf(assignment.variableAssignmentLogical?.qualifiedReference?.qualifiedNameParts?.firstOrNull()) +
                        assignment.variableDeclarationList?.variableNameList.orEmpty() +
                        assignment.variableDeclarationList?.variableDeclarationList?.flatMap { objJVariableDeclaration ->
                            objJVariableDeclaration.qualifiedReferenceList.mapNotNull {
                                it.qualifiedNameParts.firstOrNull()
                            }
                        }.orEmpty()
            }
            .mapNotNull {
                ProgressIndicatorProvider.checkCanceled()
                getAssignedExpressions(it, variableNameString)
            }

    val fromGlobals = if (!DumbService.isDumb(variableName.project))
        ObjJGlobalVariableNamesIndex.instance[variableNameString, variableName.project].mapNotNull { it.expr }
    else
        variableName.containingObjJFile?.getFileChildrenOfType(ObjJGlobalVariableDeclaration::class.java, false)
                ?.filter { it.variableName.text == variableNameString }
                ?.mapNotNull { it.expr }.orEmpty()
    val fromVariableDeclarations =
            variableName.getParentBlockChildrenOfType(ObjJExpr::class.java, true)
                    .flatMap { expr ->
                        ProgressIndicatorProvider.checkCanceled()
                        //ProgressManager.checkCanceled()
                        expr.leftExpr?.variableDeclaration?.qualifiedReferenceList?.mapNotNull {
                            it.qualifiedNameParts.firstOrNull()
                        } ?: emptyList()
                    }.mapNotNull {
                        ProgressIndicatorProvider.checkCanceled()
                        getAssignedExpressions(it, variableNameString)
                    }
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