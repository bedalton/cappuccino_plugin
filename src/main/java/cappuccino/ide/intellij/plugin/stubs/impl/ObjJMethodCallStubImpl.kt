package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJMethodCallImpl
import cappuccino.ide.intellij.plugin.psi.utils.getSelectorStringFromSelectorStrings
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodCallStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement

class ObjJMethodCallStubImpl(parent: StubElement<*>, override val containingClassName: String?, override val callTarget: String, override val possibleCallTargetTypes: List<String>, override val selectorStrings: List<String>, private val shouldResolve: Boolean) : ObjJStubBaseImpl<ObjJMethodCallImpl>(parent, ObjJStubTypes.METHOD_CALL), ObjJMethodCallStub {
    override val selectorString: String = getSelectorStringFromSelectorStrings(this.selectorStrings)

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }

}
