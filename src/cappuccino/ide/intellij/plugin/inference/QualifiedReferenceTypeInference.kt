package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListArrayType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListFunctionType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.getClassDefinition
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefPropertiesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefClassElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunction
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.JsTypeDefPsiImplUtil
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariablePsiUtil
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.stubs.types.TYPES_DELIM
import cappuccino.ide.intellij.plugin.utils.isNotNullOrBlank
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import com.intellij.openapi.project.Project

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
    for (i in parts.indices) {
        //ProgressManager.checkCanceled()
        val part = parts[i]
        val thisParentTypes = parentTypes
        parentTypes = part.getCachedInferredTypes(tag) {
            if (part.tagged(tag, false))
                return@getCachedInferredTypes null

            if (parts.size == 1 && parts[0] is ObjJVariableName) {
                val variableName = parts[0] as ObjJVariableName
                val simpleType = simpleVariableInference(variableName)
                if (simpleType?.withoutAnyType().orEmpty().isNotEmpty())
                    return@getCachedInferredTypes simpleType
            }
            if (i == parts.lastIndex && (part.parent is ObjJVariableDeclaration || part.parent.parent is ObjJVariableDeclaration)) {
                val variableDeclarationExpr =
                        (part.parent as? ObjJVariableDeclaration ?: part.parent.parent as ObjJVariableDeclaration).expr
                                ?: return@getCachedInferredTypes null
                inferExpressionType(variableDeclarationExpr, tag)
            } else if (i == 0) {
                getPartTypes(part, thisParentTypes, false, tag)
            } else {
                getPartTypes(part, thisParentTypes, isStatic, tag)
            }
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

private fun simpleVariableInference(variableName: ObjJVariableName) : InferenceResult? {
    val varDefTypeSimple = ObjJCommentEvaluatorUtil.getVariableTypesInParent(variableName)
    if (varDefTypeSimple.isNotNullOrBlank() && varDefTypeSimple !in anyTypes) {
        return InferenceResult(
                types = setOf(varDefTypeSimple!!).toJsTypeList()
        )
    }
    if (variableName.parent is ObjJCatchProduction)
        return setOf("Error").toInferenceResult()
    return null
}

internal fun getPartTypes(part: ObjJQualifiedReferenceComponent, parentTypes: InferenceResult?, static: Boolean, tag: Long): InferenceResult? {
    return when (part) {
        is ObjJVariableName -> getVariableNameComponentTypes(part, parentTypes, static, tag)
        is ObjJFunctionCall -> getFunctionComponentTypes(part.functionName, parentTypes, static, tag)
        is ObjJFunctionName -> getFunctionComponentTypes(part, parentTypes, static, tag)
        is ObjJArrayIndexSelector -> getArrayTypes(parentTypes)
        is ObjJMethodCall -> inferMethodCallType(part, tag)
        is ObjJParenEnclosedExpr -> if (part.expr != null) inferExpressionType(part.expr!!, tag) else null
        is ObjJStringLiteral -> return STRING_TYPE_INFERENCE_RESULT
        else -> return null
    }
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
    if (functionNameString == "apply") {
        return parentTypes.functionTypes.mapNotNull { it.returnType }.combine()
    }
    val classNames = (parentTypes.classes + (if (parentTypes.types.any { it is JsTypeListArrayType}) "Array" else null)).filterNotNull()
    val classes = classNames.mapNotNull {
        getClassDefinition(project, it)
    }
    val functions = classes.flatMap { jsClass ->
        (if (static)
            jsClass.staticFunctions.filter {
                it.name == functionNameString
            }  + jsClass.staticProperties.filter {
                it.name == functionNameString
            }.flatMap { property ->
                property.types.types.mapNotNull { it as? JsTypeListFunctionType }
            }
        else
            jsClass.functions.filter {
                it.name == functionNameString
            } + jsClass.properties.filter {
            it.name == functionNameString
        }.flatMap { property ->
                property.types.types.mapNotNull { it as? JsTypeListFunctionType }
        })
    }

    var returnTypes = functions.flatMap {
        it.returnType?.types.orEmpty()
    }.toSet()

    if (true || returnTypes.any { it is JsTypeListType.JsTypeListValueOfKeyType }) {
        val functionDeclaration = functionName.reference.resolve()?.parentFunctionDeclaration
        val functionCall = functionName.getParentOfType(ObjJFunctionCall::class.java)
        if (functionDeclaration is JsTypeDefFunction && functionCall != null) {
            val parameters = functionCall.arguments.exprList.map {
                (it?.leftExpr?.qualifiedReference?.stringLiteral ?: it?.leftExpr?.primary?.stringLiteral)?.stringValue
            }.orEmpty()
            val typeMapTypes = JsTypeDefPsiImplUtil.resolveForMapType(functionDeclaration, parameters)
            if (typeMapTypes?.types.isNotNullOrEmpty())
                returnTypes = returnTypes + typeMapTypes!!.types
        }
    }
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
            return functionType?.functionTypes?.mapNotNull {
                it.returnType
            }?.combine()
        }
    }
    var basicReturnTypes = functionDeclaration?.getReturnTypes(tag)?.toClassList(null)
    if (functionDeclaration is JsTypeDefFunction) {
        val parameters = functionCall?.arguments?.exprList?.map {
            (it?.leftExpr?.qualifiedReference?.stringLiteral ?: it?.leftExpr?.primary?.stringLiteral)?.stringValue
        }.orEmpty()
        val typeMapTypes = JsTypeDefPsiImplUtil.resolveForMapType(functionDeclaration, parameters)
        if (typeMapTypes?.types.isNotNullOrEmpty())
            basicReturnTypes = basicReturnTypes.orEmpty() + typeMapTypes!!.toClassList(null)
    }
    if (JsTypeDefClassesByNamespaceIndex.instance.containsKey(functionNameString, project))
        basicReturnTypes = basicReturnTypes.orEmpty() + functionNameString
    val functionTypes = JsTypeDefFunctionsByNameIndex.instance[functionNameString, project].map {
        //ProgressManager.checkCanceled()
        it.toJsFunctionType(tag)
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
        return inferMethodCallType(part, tag)
    } else if (name == null)
        return INFERRED_ANY_TYPE
    val firstMatches: MutableList<JsTypeListType> = mutableListOf()

    val functions = JsTypeDefFunctionsByNameIndex.instance[name, project].map {
        //ProgressManager.checkCanceled()
        it.toJsFunctionType()
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

fun ObjJVariableName.getAssignmentExprOrNull(): ObjJExpr? {
    return (this.parent as? ObjJGlobalVariableDeclaration)?.expr
            ?: (this.parent.parent as? ObjJVariableDeclaration)?.expr
}

private val STRING_TYPE_INFERENCE_RESULT = InferenceResult(types = setOf("String").toJsTypeList());