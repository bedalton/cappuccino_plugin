package cappuccino.ide.intellij.plugin.stubs.types

import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub

abstract class ObjJClassDeclarationStubType<StubT : ObjJClassDeclarationStub<PsiT>, PsiT : ObjJClassDeclarationElement<*>> internal constructor(
        debugName: String,
        psiClass: Class<PsiT>) : ObjJStubElementType<StubT, PsiT>(debugName, psiClass)
