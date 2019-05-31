package cappuccino.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.NamedStubBase
import com.intellij.psi.stubs.StubElement
import com.intellij.openapi.util.Pair
import cappuccino.ide.intellij.plugin.psi.impl.ObjJVariableNameImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJVariableNameStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes

class ObjJVariableNameStubImpl(parent: StubElement<*>, override val variableName: String, override val containingBlockRanges: List<Pair<Int, Int>>, override val greatestContainingBlockRange: Pair<Int, Int>?, private val shouldResolve: Boolean) : NamedStubBase<ObjJVariableNameImpl>(parent, ObjJStubTypes.VARIABLE_NAME, variableName), ObjJVariableNameStub {
    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}
