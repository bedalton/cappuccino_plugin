package cappuccino.ide.intellij.plugin.stubs.interfaces

import cappuccino.ide.intellij.plugin.psi.impl.ObjJVariableNameImpl
import com.intellij.openapi.util.Pair
import com.intellij.psi.stubs.StubElement

interface ObjJVariableNameStub : StubElement<ObjJVariableNameImpl>, ObjJResolveableStub<ObjJVariableNameImpl> {
    val variableName: String
    val containingBlockRanges: List<Pair<Int, Int>>
    val greatestContainingBlockRange: Pair<Int, Int>?
    val isAssignedTo:Boolean
    val indexInQualifiedReference:Int
}
