package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.utils.getSelectorStringFromSelectorStrings

interface ObjJHasMethodSelector : ObjJCompositeElement, ObjJHasContainingClass {

    val selectorString: String
        get() = getSelectorStringFromSelectorStrings(selectorStrings)

    val selectorList: List<ObjJSelector>

    val selectorStrings: List<String>
}
