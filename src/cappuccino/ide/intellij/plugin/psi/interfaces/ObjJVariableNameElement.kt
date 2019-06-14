package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.inference.InferenceResult

interface ObjJVariableNameElement {

    val cachedParentFunctionDeclaration:ObjJFunctionDeclarationElement<*>?

    val cachedMethods:List<ObjJMethodHeaderDeclaration<*>>

    val classTypes:InferenceResult?
}