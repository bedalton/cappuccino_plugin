package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.psi.ObjJFormalParameterArg
import cappuccino.ide.intellij.plugin.psi.ObjJLastFormalParameterArg
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import cappuccino.ide.intellij.plugin.inference.SPLIT_JS_CLASS_TYPES_LIST_REGEX as SPLIT_JS_CLASS_TYPES_LIST_REGEX1

interface ObjJFunctionDeclarationElement<StubT: ObjJFunctionDeclarationElementStub<*>> : ObjJUniversalFunctionElement, ObjJHasFunctionName, ObjJStubBasedElement<StubT>, ObjJCompositeElement, ObjJResolveableElement<StubT>, ObjJHasTreeStructureElement {
    fun getCachedReturnType(tag:Long):InferenceResult?
    val formalParameterArgList: List<ObjJFormalParameterArg>

    val lastFormalParameterArg: ObjJLastFormalParameterArg?

    val block: ObjJBlock?

    val functionScope:ObjJFunctionScope
}
