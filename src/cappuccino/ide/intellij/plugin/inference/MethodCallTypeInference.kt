package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.ObjJCallTarget
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.ObjJMethodDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJReturnStatement
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import cappuccino.ide.intellij.plugin.utils.substringFromEnd

internal fun inferMethodCallType(methodCall:ObjJMethodCall, level:Int) : InferenceResult? {
    val project = methodCall.project
    val selector = methodCall.selectorString
    if (selector == "alloc") {
        val classes = ObjJInheritanceUtil.getAllInheritedClasses(methodCall.callTarget.text, project)
        if (classes.isNotEmpty()) {
            return InferenceResult(
                    classes = classes
            )
        }
    }
    val callTargetTypes = inferCallTargetType(methodCall.callTarget, level)?.classes
            ?: emptySet()

    val methods = if (callTargetTypes.isNotEmpty()) {
        val classes = callTargetTypes.flatMap { ObjJInheritanceUtil.getAllInheritedClasses(it, project) }
        ObjJUnifiedMethodIndex.instance[selector, project].filter {
            it.containingClassName in classes
        }
    } else {
        ObjJUnifiedMethodIndex.instance[selector, project]
    }

    val methodDeclarations = methods.mapNotNull { it.getParentOfType(ObjJMethodDeclaration::class.java) }
    val returnTypes = methodDeclarations.flatMap { methodDeclaration ->
        val simpleReturnType = methodDeclaration.methodHeader.returnType
        if (simpleReturnType != "id") {
            val type = when {
                simpleReturnType.endsWith("Pointer") -> simpleReturnType.substringFromEnd(0, "Pointer".length)
                simpleReturnType.endsWith("Reference") -> simpleReturnType.substringFromEnd(0, "Reference".length)
                simpleReturnType.endsWith("Ptr") -> simpleReturnType.substringFromEnd(0, "Ptr".length)
                simpleReturnType.endsWith("Ref") -> simpleReturnType.substringFromEnd(0, "Ref".length)
                else -> simpleReturnType
            }
            listOf(type)
        } else {
            var out = InferenceResult()
            methodDeclaration.methodBlock.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).forEach {
                val type = if (it.expr != null)
                    inferExpressionType(it.expr!!, level - 1)
                else
                    null
                if (type != null)
                    out += type
            }
            out.classes
        }
    }
    return InferenceResult(classes = returnTypes.toSet())
}

private fun inferCallTargetType(callTarget:ObjJCallTarget, level:Int) : InferenceResult? {
    if (callTarget.expr != null)
        return inferExpressionType(callTarget.expr!!, level - 1)

    if (callTarget.functionCall != null) {
        return inferFunctionCallReturnType(callTarget.functionCall!!, level - 1)
    }
    if (callTarget.qualifiedReference != null) {
        return inferQualifiedReferenceType(callTarget.qualifiedReference!!, false,level - 1)
    }
    return null
}