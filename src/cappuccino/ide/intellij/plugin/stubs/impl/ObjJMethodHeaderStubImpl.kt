package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJMethodHeaderImpl
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.getSelectorStringFromSelectorStrings
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJSelectorStruct
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement

class ObjJMethodHeaderStubImpl(parent: StubElement<*>?, className: String?, override val isStatic: Boolean, override val selectorStrings: List<String>, override val paramTypes: List<String>, override val explicitReturnType: String, override val isRequired: Boolean, private val shouldResolve: Boolean, override val ignored:Boolean, override val selectorStructs: List<ObjJSelectorStruct>) : ObjJStubBaseImpl<ObjJMethodHeaderImpl>(parent, ObjJStubTypes.METHOD_HEADER), ObjJMethodHeaderStub {
    override val containingClassName: String = className ?: ObjJClassType.UNDEF_CLASS_NAME
    override val selectorString: String = getSelectorStringFromSelectorStrings(selectorStrings)
    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}
