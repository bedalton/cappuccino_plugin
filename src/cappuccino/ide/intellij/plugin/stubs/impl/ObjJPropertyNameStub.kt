package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJPropertyNameImpl
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement

interface ObjJPropertyNameStub : StubElement<ObjJPropertyNameImpl> {
    val key:String
    val namespacedName:String
    val namespaceComponents:List<String>
}


class ObjJPropertyNameStubImpl(parent:StubElement<*>, override val key:String, override val namespacedName: String) : ObjJStubBaseImpl<ObjJPropertyNameImpl>(parent, ObjJStubTypes.PROPERTY_NAME), ObjJPropertyNameStub {
    override val namespaceComponents: List<String> by lazy {
        namespacedName.split("\\s*\\.\\s*".toRegex())
    }
}