package cappuccino.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.stubs.StubElement
import com.intellij.openapi.util.Pair
import cappuccino.ide.intellij.plugin.psi.impl.ObjJVariableNameImpl

interface ObjJVariableNameStub : StubElement<ObjJVariableNameImpl>, ObjJResolveableStub<ObjJVariableNameImpl> {
    val variableName: String
    val types:Set<String>
    val containingBlockRanges: List<Pair<Int, Int>>
    val greatestContainingBlockRange: Pair<Int, Int>?
}
