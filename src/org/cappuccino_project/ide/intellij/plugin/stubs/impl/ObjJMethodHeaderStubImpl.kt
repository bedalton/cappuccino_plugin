package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJMethodHeaderImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils

class ObjJMethodHeaderStubImpl(parent: StubElement<*>, className: String?, override val isStatic: Boolean, override val selectorStrings: List<String>, override val paramTypes: List<String>, returnType: String?, override val isRequired: Boolean, private val shouldResolve: Boolean) : ObjJStubBaseImpl<ObjJMethodHeaderImpl>(parent, ObjJStubTypes.METHOD_HEADER), ObjJMethodHeaderStub {
    override val returnTypeAsString: String
    override val containingClassName: String
    override val selectorString: String

    override val returnType: ObjJClassType
        get() = ObjJClassType.getClassType(returnTypeAsString)

    init {
        this.selectorString = ObjJMethodPsiUtils.getSelectorStringFromSelectorStrings(selectorStrings)
        this.returnTypeAsString = returnType ?: ObjJClassType.UNDETERMINED
        this.containingClassName = className ?: ObjJClassType.UNDEF_CLASS_NAME
    }

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}
