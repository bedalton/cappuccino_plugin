package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.inference.InferenceResult

interface ObjJVariableNameElement {

    val cachedParentFunctionDeclaration:ObjJFunctionDeclarationElement<*>?

    fun getCachedMethods(tag:Long):List<ObjJMethodHeaderDeclaration<*>>

    fun getClassTypes(tag:Long):InferenceResult?
}