package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefFunction
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.JsTypeDefPsiImplUtil
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionCall
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionName
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project

internal fun getFunctionComponentTypes(functionName: ObjJFunctionName?, parentTypes: InferenceResult?, static: Boolean, tag: Long): InferenceResult? {
    if (functionName == null)
        return null
    ProgressIndicatorProvider.checkCanceled()
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

    val functions = getAllPropertiesWithNameInParentTypes(functionNameString, parentTypes, static, project)
            .filterIsInstance<JsTypeListType.JsTypeListFunctionType>()

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
    val project: Project = functionName.project
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
        ProgressIndicatorProvider.checkCanceled()
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
