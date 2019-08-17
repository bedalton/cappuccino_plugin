package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement

open class ObjJClassDeclarationStubImpl<PsiT : ObjJClassDeclarationElement<out ObjJClassDeclarationStub<*>>> internal constructor(
        parent: StubElement<*>,
        elementType: IStubElementType<*, *>,
        override val className: String,
        override val inheritedProtocols: List<String>,
        private val shouldResolve: Boolean) : ObjJStubBaseImpl<PsiT>(parent, elementType), ObjJClassDeclarationStub<PsiT> {

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}
