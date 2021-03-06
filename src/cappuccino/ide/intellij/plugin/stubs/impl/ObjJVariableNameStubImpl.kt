package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJVariableNameImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJVariableNameStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.openapi.util.Pair
import com.intellij.psi.stubs.NamedStubBase
import com.intellij.psi.stubs.StubElement

class ObjJVariableNameStubImpl(
        parent: StubElement<*>,
        override val variableName: String,
        override val isAssignedTo:Boolean,
        override val indexInQualifiedReference: Int,
        override val containingBlockRanges: List<Pair<Int, Int>>,
        override val greatestContainingBlockRange: Pair<Int, Int>?,
        private val shouldResolve: Boolean,
        override val hasContainingClass: Boolean
) : NamedStubBase<ObjJVariableNameImpl>(parent, ObjJStubTypes.VARIABLE_NAME, variableName), ObjJVariableNameStub {
    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}
