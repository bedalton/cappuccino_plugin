package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils

interface ObjJHasMethodSelector : ObjJCompositeElement, ObjJHasContainingClass {

    val selectorString: String
        get() = ObjJMethodPsiUtils.getSelectorStringFromSelectorStrings(selectorStrings)

    val selectorList: List<ObjJSelector>

    val selectorStrings: List<String>
}
