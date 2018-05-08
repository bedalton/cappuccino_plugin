package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJMethodCallImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodCallStub
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils

class ObjJMethodCallStubImpl(parent: StubElement<*>, override val containingClassName: String?, override val callTarget: String, override val possibleCallTargetTypes: List<String>, override val selectorStrings: List<String>, private val shouldResolve: Boolean) : ObjJStubBaseImpl<ObjJMethodCallImpl>(parent, ObjJStubTypes.METHOD_CALL), ObjJMethodCallStub {
    private var selectorString: String? = null

    override fun getSelectorString(): String {
        if (selectorString == null) {
            selectorString = ObjJMethodPsiUtils.getSelectorStringFromSelectorStrings(this.selectorStrings)
        }
        return selectorString
    }

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }

}
