package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.inference.InferenceResult

interface ObjJFunctionNameElement : ObjJCompositeElement {
    val cachedParentFunctionDeclaration:ObjJUniversalFunctionElement?
    fun getCachedReturnType(tag:Long):InferenceResult?
}