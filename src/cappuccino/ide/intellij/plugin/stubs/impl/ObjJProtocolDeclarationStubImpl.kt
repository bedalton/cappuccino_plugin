package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJProtocolDeclarationImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJProtocolDeclarationStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement

open class ObjJProtocolDeclarationStubImpl internal constructor(
        parent: StubElement<*>,
        override val className: String,
        override val inheritedProtocols: List<String>,
        private val shouldResolve: Boolean) : ObjJStubBaseImpl<ObjJProtocolDeclarationImpl>(parent, ObjJStubTypes.PROTOCOL), ObjJProtocolDeclarationStub {

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}