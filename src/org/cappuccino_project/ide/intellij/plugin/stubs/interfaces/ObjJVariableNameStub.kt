package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces

import com.intellij.psi.stubs.StubElement
import com.intellij.openapi.util.Pair
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJVariableNameImpl

interface ObjJVariableNameStub : StubElement<ObjJVariableNameImpl>, ObjJResolveableStub<ObjJVariableNameImpl> {
    val variableName: String
    val containingBlockRanges: List<Pair<Int, Int>>
    val greatestContainingBlockRange: Pair<Int, Int>?
}
