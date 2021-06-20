package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.Tag

interface ObjJFunctionNameElement : ObjJCompositeElement {
    val cachedParentFunctionDeclaration:ObjJUniversalFunctionElement?
    fun getCachedReturnType(tag: Tag):InferenceResult?
}