package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.indices.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasContainingClass
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.containingSuperClassName
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.docComment
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import cappuccino.ide.intellij.plugin.utils.orFalse
import cappuccino.ide.intellij.plugin.utils.stripRefSuffixes
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project

internal fun inferMethodCallType(methodCall:ObjJMethodCall, tag:Long) : InferenceResult? {
    return methodCall.getCachedInferredTypes(tag) {
        if (methodCall.tagged(tag))
            return@getCachedInferredTypes null
        internalInferMethodCallType(methodCall, tag)
    }
}

// MUST BE FALSE
// Causes infinite recursion
//private val CLASS_METHOD_BASED_RESOLVE = false
/*
private fun internalInferMethodCallType(methodCall:ObjJMethodCall, tag:Long) : InferenceResult? {
    //ProgressManager.checkCanceled()
    /*if (level < 0)
        return null*/
    val project = methodCall.project
    val selector = methodCall.selectorString
    if (selector == "alloc" || selector == "alloc:") {
        return getAllocStatementType(methodCall)
    }
    if (selector == "respondsToSelector:" || selector == "respondsToSelector")
        return listOf("BOOL").toInferenceResult()

    val callTargetType = inferCallTargetType(methodCall.callTarget, tag)
    val callTargetTypes = callTargetType?.toClassList(null)?.withoutAnyType().orEmpty()

    if (selector == "copy" || selector == "copy:")
        return callTargetType

    if (callTargetTypes.isEmpty())
        return INFERRED_ANY_TYPE

    if (CLASS_METHOD_BASED_RESOLVE) {
        var out = callTargetTypes.mapNotNull {className ->
            ObjJImplementationDeclarationsIndex.instance[className, project].firstOrNull { !it.isCategory} ?.getMethodReturnType(selector, tag)
                    ?:ObjJImplementationDeclarationsIndex.instance[className, project].firstOrNull()?.getMethodReturnType(selector, tag)
        }
        if (out.isEmpty()) {
            out = callTargetTypes.mapNotNull {
                ObjJProtocolDeclarationsIndex.instance[it, project].firstOrNull()?.getMethodReturnType(selector, tag)
            }
        }
        return out.collapse()
    } else {

        val methods: List<ObjJMethodHeaderDeclaration<*>> = if (!DumbService.isDumb(project)) {
            if (callTargetTypes.isNotEmpty()) {
                return callTargetTypes.flatMap { className ->
                    ObjJClassAndSelectorMethodIndex.instance.getByClassAndSelector(className, selector, project).mapNotNull {
                        it.getCachedReturnType(tag)
                    }
                }.collapse()
            }
            ObjJUnifiedMethodIndex.instance[selector, project]
        } else
            emptyList()
        return methods.toSet().mapNotNull {
            it.getCachedReturnType(tag)
        }.collapse()
    }
}
*/
private fun internalInferMethodCallType(methodCall:ObjJMethodCall, tag:Long) : InferenceResult? {
    //ProgressManager.checkCanceled()
    /*if (level < 0)
        return null*/
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
    val getReturnType = methodDeclarations.flatMap { methodDeclaration ->
        methodDeclaration.getCachedInferredTypes(tag) {
            if (methodDeclaration.tagged(tag))
                return@getCachedInferredTypes null
            if (methodDeclaration.methodHeader.explicitReturnType == "instancetype" && methodDeclaration.containingClassName !in anyTypes) {
                return@getCachedInferredTypes setOf(methodDeclaration.containingClassName).toInferenceResult()
            }
            val commentReturnTypes = methodDeclaration.docComment?.getReturnTypes(methodDeclaration.project).orEmpty().withoutAnyType()
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
    }
    val instanceVariableTypes = callTargetTypes.flatMap {className ->
        ObjJInstanceVariablesByClassIndex.instance[className, project].filter{ it.variableName?.text == selector}.mapNotNull {
            val type = it.variableType
            if (type.isNotBlank())
                type
            else
                null
        }
    }
    // Accessors are different than instance var names
    val instanceVariableAccessorTypes = getMethods.mapNotNull { it.getParentOfType(ObjJInstanceVariableDeclaration::class.java) }.flatMap {
        instanceVariable ->
        instanceVariable.getCachedInferredTypes(tag) {
            if (instanceVariable.tagged(tag))
                return@getCachedInferredTypes null
            return@getCachedInferredTypes  setOf(instanceVariable.variableType).toInferenceResult()
        }?.classes.orEmpty()
    }
    val out = mutableSetOf<String>()
    out.addAll(instanceVariableAccessorTypes)
    out.addAll(instanceVariableTypes)
    out.addAll(getReturnType)
    return InferenceResult(
            types = out.toJsTypeList()
    )
}

private fun getReturnTypesFromKnownClasses(project:Project, callTargetTypes:Set<String>, selector:String, tag:Long) :InferenceResult {
    var nullable = false
    val types = callTargetTypes.flatMap { ObjJClassDeclarationsIndex.instance[it, project] }
            .flatMap { classDeclaration ->
                val out = classDeclaration.getReturnTypesForSelector(selector, tag)
                if (out?.nullable.orFalse())
                    nullable = true
                out?.types.orEmpty()
            }
    return InferenceResult(
            types = types.toSet(),
            nullable = nullable
    )
}

private fun ObjJClassDeclarationElement<*>.getReturnTypesForSelector(selector: String, tag: Long) : InferenceResult? {
    var nullable = false
    val types = getMethodStructs(true, tag).filter {
        LOGGER.info("<$selector> == <${it.selectorStringWithColon}>")
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

/*
private fun internalInferMethodCallType(methodCall:ObjJMethodCall, tag:Long) : InferenceResult? {
    //ProgressManager.checkCanceled()
    /*if (level < 0)
        return null*/
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
    val getMethods: List<ObjJMethodHeaderDeclaration<*>> = if (!DumbService.isDumb(project)) {
        if (callTargetTypes.isNotEmpty()) {
            ObjJUnifiedMethodIndex.instance[selector, project].filter {
                it.containingClassName in callTargetTypes
            }
        } else {
            ObjJUnifiedMethodIndex.instance[selector, project]
        }
    } else
        emptyList()

    val methodDeclarations = getMethods.mapNotNull { it.getParentOfType(ObjJMethodDeclaration::class.java) }
    val getReturnType = methodDeclarations.flatMap { methodDeclaration ->
        methodDeclaration.getCachedInferredTypes(tag) {
            if (methodDeclaration.tagged(tag))
                return@getCachedInferredTypes null
            val commentReturnTypes = methodDeclaration.docComment?.getReturnTypes(methodDeclaration.project).orEmpty().withoutAnyType()
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
    }
    val instanceVariableTypes = callTargetTypes.flatMap {className ->
        ObjJInstanceVariablesByClassIndex.instance[className, project].filter{ it.variableName?.text == selector}.mapNotNull {
            val type = it.variableType
            if (type.isNotBlank())
                type
            else
                null
        }
    }
    // Accessors are different than instance var names
    val instanceVariableAccessorTypes = getMethods.mapNotNull { it.getParentOfType(ObjJInstanceVariableDeclaration::class.java) }.flatMap {
        instanceVariable ->
        instanceVariable.getCachedInferredTypes(tag) {
            LOGGER.info("Getting instance variable type information from <${instanceVariable.text}>")
            return@getCachedInferredTypes  setOf(instanceVariable.variableType).toInferenceResult()
        }?.classes.orEmpty()
    }
    val out = mutableSetOf<String>()
    out.addAll(instanceVariableAccessorTypes)
    out.addAll(instanceVariableTypes)
    out.addAll(getReturnType)
    return InferenceResult(
            types = out.toJsTypeList()
    )
}*/

private fun getAllocStatementType(methodCall: ObjJMethodCall) : InferenceResult? {
    val className = when (val callTargetText = methodCall.callTargetText) {
        "super" -> methodCall
                .getParentOfType(ObjJHasContainingClass::class.java)
                ?.containingSuperClassName
                ?: callTargetText
        "self" -> methodCall
                .getParentOfType(ObjJHasContainingClass::class.java)
                ?.containingClassName
                ?: callTargetText
        else -> callTargetText
    }
    val isValidClass = ObjJImplementationDeclarationsIndex.instance.containsKey(className, methodCall.project)
    if (!isValidClass) {
        return null
    }
    return InferenceResult(
            types = setOf(className).toJsTypeList()
    )
}

fun inferCallTargetType(callTarget: ObjJCallTarget, tag:Long) : InferenceResult? {
    /*if (level < 0)
        return emptySet()*/
    return callTarget.getCachedInferredTypes(tag) {
        //if (callTarget.tagged(tag))
         //   return@getCachedInferredTypes null
        internalInferCallTargetType(callTarget, tag)
    }
}

private fun internalInferCallTargetType(callTarget:ObjJCallTarget, tag:Long) : InferenceResult? {
    val callTargetText = callTarget.text
    if (ObjJClassDeclarationsIndex.instance.containsKey(callTargetText, callTarget.project))
        return setOf(callTargetText).toInferenceResult()
    if (callTarget.expr != null)
        return inferExpressionType(callTarget.expr!!, tag)

    if (callTarget.functionCall != null) {
        return inferFunctionCallReturnType(callTarget.functionCall!!, tag)
    }
    if (callTarget.qualifiedReference != null) {
        return inferQualifiedReferenceType(callTarget.qualifiedReference!!.qualifiedNameParts, tag)
    }
    return null
}

private fun getMethodDeclarationReturnTypeFromReturnStatements(methodDeclaration:ObjJMethodDeclaration, tag:Long) : Set<String> {
    //ProgressManager.checkCanceled()
    val simpleReturnType = methodDeclaration.methodHeader.explicitReturnType
    if (simpleReturnType != "id") {
        val type = simpleReturnType.stripRefSuffixes()
        return setOf(type)
    } else {
        //var out = methodDeclaration.methodHeader.getCachedReturnType(tag)
        //if (out != null)
         //   return out.classes
        var out = INFERRED_EMPTY_TYPE
        val expressions = methodDeclaration.methodBlock.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).mapNotNull { it.expr }
        val selfExpressionTypes = expressions.filter { it.text == "self"}.mapNotNull { (it.getParentOfType(ObjJHasContainingClass::class.java)?.containingClassName)}
        val superExpressionTypes = expressions.filter { it.text == "super"}.mapNotNull { (it.getParentOfType(ObjJHasContainingClass::class.java)?.getContainingSuperClass()?.text)}
        val simpleOut = selfExpressionTypes + superExpressionTypes
        if (simpleOut.isNotEmpty()) {
            return simpleOut.toSet()
        }
        expressions.forEach {
            val type = inferExpressionType(it, tag)
            if (type != null)
                out += type
        }
        return out.classes
    }
}