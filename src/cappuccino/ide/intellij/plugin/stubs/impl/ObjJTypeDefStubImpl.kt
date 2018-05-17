package cappuccino.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJProtocolDeclarationImpl
import cappuccino.ide.intellij.plugin.psi.impl.ObjJTypeDefImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes

open class ObjJTypeDefStubImpl internal constructor(
        parent: StubElement<*>,
        override val className: String) : ObjJStubBaseImpl<ObjJTypeDefImpl>(parent, ObjJStubTypes.TYPE_DEF), ObjJClassDeclarationStub<ObjJTypeDefImpl> {

    override val inheritedProtocols: List<String> = listOf()
    override fun shouldResolve(): Boolean {
        return true
    }
}