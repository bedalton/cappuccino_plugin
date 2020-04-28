package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJInstanceVariablesByClassIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.docComment
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import cappuccino.ide.intellij.plugin.utils.orFalse
import cappuccino.ide.intellij.plugin.utils.stripRefSuffixes
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project

internal fun inferMethodCallType(methodCall: ObjJMethodCall, tag: Tag): InferenceResult? {
    return methodCall.getCachedInferredTypes(tag) {
        if (methodCall.tagged(tag))
            return@getCachedInferredTypes null
        internalInferMethodCallType(methodCall, tag)
    }
}

private fun internalInferMethodCallType(methodCall: ObjJMethodCall, tag: Tag): InferenceResult? {
    ProgressIndicatorProvider.checkCanceled()
    val project = methodCall.project
    val selector = methodCall.selectorString
    if (selector == "alloc" || selector == "alloc:") {
        return getAllocStatementType(methodCall)
    }
    val callTargetType = inferCallTargetType(methodCall.callTarget, tag)
    val callTargetTypes = callTargetType?.classes.orEmpty().flatMap {
        ObjJInheritanceUtil.getAllInheritedClasses(it, project)
    }.toSet()
    if (selector == "copy" || selector == "copy:" || selector == "new:" || selector == "new:")
        return callTargetType

    if (DumbService.isDumb(project)) {
        return null
    }
    val returnTypes = getReturnTypesFromKnownClasses(project, callTargetTypes, selector, tag)
    if (returnTypes.classes.withoutAnyType().isNotEmpty()) {
        return returnTypes
    }
    val getMethods: List<ObjJMethodHeaderDeclaration<*>> = ObjJUnifiedMethodIndex.instance[selector, project]
    val methodDeclarations = getMethods.mapNotNull { it.getParentOfType(ObjJMethodDeclaration::class.java) }
    val returnTypesFromExpressions = methodDeclarations.flatMap { methodDeclaration ->
        methodDeclaration.getCachedInferredTypes(tag) {
            ProgressIndicatorProvider.checkCanceled()
            if (methodDeclaration.tagged(tag))
                return@getCachedInferredTypes null
            ProgressIndicatorProvider.checkCanceled()
            if (methodDeclaration.methodHeader.explicitReturnType == "instancetype" && methodDeclaration.containingClassName !in anyTypes) {
                return@getCachedInferredTypes setOf(methodDeclaration.containingClassName).toInferenceResult()
            }
            val commentReturnTypes = methodDeclaration.docComment?.getReturnTypes()?.withoutAnyType().orEmpty()
            if (commentReturnTypes.isNotEmpty()) {
                return@getCachedInferredTypes commentReturnTypes.toInferenceResult()
            }
            val thisClasses = getMethodDeclarationReturnTypeFromReturnStatements(methodDeclaration, tag)
            if (thisClasses.isNotEmpty()) {
                InferenceResult(
                        types = thisClasses.toJsTypeList()
                )
            } else
                null
        }?.classes.orEmpty()
    }.toMutableSet()
    if (returnTypesFromExpressions.any { it == "self" }) {
        returnTypesFromExpressions.addAll(callTargetTypes)
    }
    if (returnTypesFromExpressions.any { it == "super" }) {
        val superTypes = callTargetTypes
                .flatMap { targetType ->
                    ObjJImplementationDeclarationsIndex.instance[targetType, project].mapNotNull {
                        it.superClassName
                    }
                }
                .toSet()
        returnTypesFromExpressions.addAll(superTypes)
    }
    val instanceVariableTypes = callTargetTypes.flatMap { className ->
        ObjJInstanceVariablesByClassIndex.instance[className, project].filter { it.variableName?.text == selector }.mapNotNull {
            val type = it.variableType
            if (type.isNotBlank())
                type
            else
                null
        }
    }
    // Accessors are different than instance var names
    val instanceVariableAccessorTypes = getMethods.mapNotNull { it.getParentOfType(ObjJInstanceVariableDeclaration::class.java) }.flatMap { instanceVariable ->
        instanceVariable.getCachedInferredTypes(tag) {
            ProgressIndicatorProvider.checkCanceled()
            if (instanceVariable.tagged(tag))
                return@getCachedInferredTypes null
            ProgressIndicatorProvider.checkCanceled()
            return@getCachedInferredTypes setOf(instanceVariable.variableType).toInferenceResult()
        }?.classes.orEmpty()
    }
    val out = mutableSetOf<String>()
    out.addAll(instanceVariableAccessorTypes)
    out.addAll(instanceVariableTypes)
    out.addAll(returnTypesFromExpressions.filter { it != "self" && it != "super" })
    return InferenceResult(
            types = out.toJsTypeList()
    )
}

private fun getReturnTypesFromKnownClasses(project: Project, callTargetTypes: Set<String>, selector: String, tag: Tag): InferenceResult {
    var nullable = false
    val types = callTargetTypes.flatMap { ObjJClassDeclarationsIndex.instance[it, project] }
            .flatMap { classDeclaration ->
                ProgressIndicatorProvider.checkCanceled()
                val outTemp = classDeclaration.getReturnTypesForSelector(selector, tag)
                if (outTemp?.nullable.orFalse())
                    nullable = true
                val out = outTemp?.types.orEmpty().toMutableSet()
                if (out.any { it.typeName == "self" }) {
                    out.addAll(callTargetTypes.toJsTypeList())
                }
                if (out.any { it.typeName == "super" }) {
                    val superTypes = callTargetTypes
                            .flatMap { targetType ->
                                ObjJImplementationDeclarationsIndex.instance[targetType, project].mapNotNull {
                                    it.superClassName
                                }
                            }
                            .toSet()
                            .toJsTypeList()
                    out.addAll(superTypes)
                }
                out.filterNot { it.typeName == "self" || it.typeName == "super" }
            }
    return InferenceResult(
            types = types.toSet(),
            nullable = nullable
    )
}

private fun ObjJClassDeclarationElement<*>.getReturnTypesForSelector(selector: String, tag: Tag): InferenceResult? {
    var nullable = false
    val types = getMethodStructs(true, tag).filter {
        selector == it.selectorStringWithColon
    }.flatMap {
        if (it.returnType?.nullable.orFalse())
            nullable = true
        it.returnType?.types.orEmpty()
    }
    if (types.isNullOrEmpty()) {
        return null
    }
    return InferenceResult(types = types.toSet(), nullable = nullable)
}

private fun getAllocStatementType(methodCall: ObjJMethodCall): InferenceResult? {
    val className = methodCall.callTargetText
    val isValidClass = ObjJImplementationDeclarationsIndex.instance.containsKey(className, methodCall.project)
    if (!isValidClass && className != "super" && className != "self") {
        return null
    }
    return InferenceResult(
            types = setOf(className).toJsTypeList()
    )
}

fun inferCallTargetType(callTarget: ObjJCallTarget, tag: Tag): InferenceResult? {
    return callTarget.getCachedInferredTypes(tag) {
        //if (callTarget.tagged(tag))
        //   return@getCachedInferredTypes null
        internalInferCallTargetType(callTarget, tag)
    }
}

private fun internalInferCallTargetType(callTarget: ObjJCallTarget, tag: Tag): InferenceResult? {
    ProgressIndicatorProvider.checkCanceled()
    val callTargetText = callTarget.text
    if (ObjJClassDeclarationsIndex.instance.containsKey(callTargetText, callTarget.project))
        return setOf(callTargetText).toInferenceResult()
    if (callTarget.expr != null)
        return inferExpressionType(callTarget.expr!!, tag)

    if (callTarget.qualifiedReference != null) {
        return inferQualifiedReferenceType(callTarget.qualifiedReference!!.qualifiedNameParts, tag)
    }
    return null
}

fun getMethodDeclarationReturnTypeFromReturnStatements(methodDeclaration: ObjJMethodDeclaration, tag: Tag): Set<String> {
    ProgressIndicatorProvider.checkCanceled()
    val simpleReturnType = methodDeclaration.methodHeader.explicitReturnType
    if (simpleReturnType != "id") {
        val type = simpleReturnType.stripRefSuffixes()
        return setOf(type)
    } else {
        var out = INFERRED_EMPTY_TYPE
        val expressions = methodDeclaration.methodBlock.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).mapNotNull { it.expr }
        if (expressions.any { it.text == "self" })
            return setOf("self")
        if (expressions.any { it.text == "super" })
            return setOf("super")
        expressions.forEach {
            val type = inferExpressionType(it, tag)
            if (type != null)
                out += type
        }
        return out.classes
    }
}