package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJImplementationDeclarationImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImplementationStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement

class ObjJImplementationStubImpl(
        parent: StubElement<*>, className: String, override val superClassName: String?, override val categoryName: String?, protocols: List<String>, shouldResolve: Boolean) : ObjJClassDeclarationStubImpl<ObjJImplementationDeclarationImpl>(parent, ObjJStubTypes.IMPLEMENTATION, className, protocols, shouldResolve), ObjJImplementationStub {

    override val isCategory: Boolean
        get() = categoryName != null
}
