package cappuccino.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImportStub
import com.intellij.psi.StubBasedPsiElement

class ObjJImportStubImpl<PsiT : StubBasedPsiElement<*>>(parent: StubElement<*>, elementType: IStubElementType<*, *>, override val framework: String?, override val fileName: String) : ObjJStubBaseImpl<PsiT>(parent, elementType), ObjJImportStub<PsiT>
