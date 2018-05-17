package cappuccino.ide.intellij.plugin.psi.interfaces

import com.intellij.psi.StubBasedPsiElement
import cappuccino.ide.intellij.plugin.psi.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.ObjJFormalParameterArg
import cappuccino.ide.intellij.plugin.psi.ObjJLastFormalParameterArg
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub

interface ObjJFunctionDeclarationElement<StubT: ObjJFunctionDeclarationElementStub<*>> : ObjJHasFunctionName, ObjJStubBasedElement<StubT>, ObjJCompositeElement, ObjJResolveableElement<StubT> {
    val paramNames: List<String>
    val returnType: String?

    val formalParameterArgList: List<ObjJFormalParameterArg>

    val lastFormalParameterArg: ObjJLastFormalParameterArg?

    val block: ObjJBlock?
}
