package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJFormalParameterArg
import cappuccino.ide.intellij.plugin.psi.ObjJLastFormalParameterArg
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionScope
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub

interface ObjJFunctionDeclarationElement<StubT: ObjJFunctionDeclarationElementStub<*>> : ObjJHasFunctionName, ObjJStubBasedElement<StubT>, ObjJCompositeElement, ObjJResolveableElement<StubT> {
    val paramNames: List<String>

    fun getReturnType(tag:Long): String?

    val formalParameterArgList: List<ObjJFormalParameterArg>

    val lastFormalParameterArg: ObjJLastFormalParameterArg?

    val block: ObjJBlock?

    val functionScope:ObjJFunctionScope
}
