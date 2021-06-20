package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.Tag

interface ObjJVariableNameElement {

    val cachedParentFunctionDeclaration:ObjJUniversalFunctionElement?

    fun getCachedMethods(tag: Tag):List<ObjJMethodHeaderDeclaration<*>>

    fun getClassTypes(tag: Tag):InferenceResult?
}