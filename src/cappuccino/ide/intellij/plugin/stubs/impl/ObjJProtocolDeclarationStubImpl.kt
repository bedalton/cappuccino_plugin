package cappuccino.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJProtocolDeclarationImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes

open class ObjJProtocolDeclarationStubImpl internal constructor(
        parent: StubElement<*>,
        override val className: String,
        override val inheritedProtocols: List<String>,
        private val shouldResolve: Boolean) : ObjJStubBaseImpl<ObjJProtocolDeclarationImpl>(parent, ObjJStubTypes.PROTOCOL), ObjJClassDeclarationStub<ObjJProtocolDeclarationImpl> {

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}