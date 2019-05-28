package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.psi.ObjJCallTarget
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall

internal fun inferMethodCallType(methodCall:ObjJMethodCall, level:Int) : InferenceResult {

}


private fun inferCallTargetType(callTargetType:ObjJCallTarget, level:Int) : InferenceResult {

    if (callTargetType.expr != null)
        return inferenceExpressionType(callTargetType.expr!!)
    if (callTargetType.functionCall) {

    }
}